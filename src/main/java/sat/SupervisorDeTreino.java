package sat;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;
import static org.bytedeco.javacpp.opencv_video.*;

import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core.CvContour;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvScalar;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_features2d.SimpleBlobDetector;
import org.bytedeco.javacpp.opencv_features2d.SimpleBlobDetector.Params;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;
import org.bytedeco.javacpp.opencv_video.BackgroundSubtractor;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameConverter;

public class SupervisorDeTreino {

	private static final double LARGURA_JANELA = 640.0;

	private static final long THRESHOLD_COSTAS = 1500;
	private static final long TEMPO_MAXIMO_COSTAS = 6000;

	private CanvasFrame janela;
	private CanvasFrame janelaDebug;

	private int learningFrames;

	private long contadorDeCostas = 0;
	private long tempoUltimoQuadro;

	private String estado = "De costas";

	public static void main(String[] args) {
		new SupervisorDeTreino().inicia();
	}

	public void inicia() {
		Loader.load(opencv_objdetect.class);
		CvMemStorage mem = CvMemStorage.create();
		OpenCVFrameConverter.ToIplImage conversor = new OpenCVFrameConverter.ToIplImage();

		String pathFrontalFace = this.getClass().getClassLoader()
				.getResource("haarcascades/haarcascade_frontalface_alt2.xml")
				.getPath();
		CascadeClassifier classificadorFrente = new CascadeClassifier(
				pathFrontalFace);

		String pathProfileFace = this.getClass().getClassLoader()
				.getResource("haarcascades/haarcascade_profileface.xml")
				.getPath();
		CascadeClassifier classificadorPerfil = new CascadeClassifier(
				pathProfileFace);

		try {
			FrameGrabber webcam = FrameGrabber.createDefault(0);
			webcam.start();
			IplImage quadro = conversor.convert(webcam.grab());

			double gamma = CanvasFrame.getDefaultGamma() / webcam.getGamma();
			janela = criaJanela("Webcam", gamma, 1.0);
			janelaDebug = criaJanela("Debug", 1.0, 1.0);
			janelaDebug.setBounds((int) LARGURA_JANELA, 0, janela.getWidth(),
					janela.getHeight());

			double fatorDeEscala = LARGURA_JANELA / quadro.width();
			int width = (int) (quadro.width() * fatorDeEscala);
			int height = (int) (quadro.height() * fatorDeEscala);

			IplImage quadroReduzido = IplImage.create(width, height,
					quadro.depth(), quadro.nChannels());
			IplImage quadroCinza = IplImage.create(width, height,
					quadro.depth(), 1);

			Mat quadroFG = cvarrToMat(quadroReduzido.clone());
			BackgroundSubtractor extratorDeFundo = createBackgroundSubtractorMOG2(
					5000, 256, false);

			Params params = new Params();
			params.minDistBetweenBlobs(200.0f);
			params.filterByInertia(false);
			params.filterByConvexity(false);
			params.filterByColor(false);
			params.filterByCircularity(false);
			params.filterByArea(true);
			params.minArea(5000.0f);
			params.maxArea(4000000.0f);

			SimpleBlobDetector detectorDeBlob = SimpleBlobDetector
					.create(params);

			Mat kernel = getStructuringElement(MORPH_ELLIPSE, new Size(9, 9));
			Mat kernelDilate = getStructuringElement(MORPH_ELLIPSE, new Size(
					27, 27));

			learningFrames = 150;

			boolean finished = false;
			while (!finished) {
				cvClearMemStorage(mem);

				quadro = conversor.convert(webcam.grab());
				if (quadro == null) {
					finished = true;
					continue;
				}

				cvResize(quadro, quadroReduzido);
				cvCvtColor(quadroReduzido, quadroCinza, CV_BGR2GRAY);

				extraiFundo(quadroReduzido, quadroFG, extratorDeFundo, kernel,
						kernelDilate);

				cvEqualizeHist(quadroCinza, quadroCinza);

				detectaBlobs(quadroReduzido, quadroFG, detectorDeBlob);

				Mat quadroContorno = quadroFG.clone();
				CvSeq contour = extraiContornos(mem, conversor, quadroContorno);
				contour = trataContorno(mem, classificadorFrente,
						classificadorPerfil, quadroReduzido, quadroCinza,
						contour);

				Mat quadroFinal = cvarrToMat(quadroReduzido);
				putText(quadroFinal, estado, new Point(10, 32),
						FONT_HERSHEY_COMPLEX_SMALL, 1.5, Scalar.BLACK, 5,
						LINE_AA, false);
				Scalar cor = Scalar.GREEN;
				if (estado.equals("De lado")) {
					cor = Scalar.YELLOW;
				} else if (estado.equals("De costas")) {
					cor = Scalar.RED;
				}
				putText(quadroFinal, estado, new Point(10, 32),
						FONT_HERSHEY_COMPLEX_SMALL, 1.5, cor, 2, LINE_AA, false);

				janela.showImage(conversor.convert(quadroFinal));
				janelaDebug.showImage(conversor.convert(quadroFG));
			}

			janela.dispose();
			janelaDebug.dispose();
			webcam.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private CvSeq trataContorno(CvMemStorage mem,
			CascadeClassifier classificadorFrente,
			CascadeClassifier classificadorPerfil, IplImage quadroReduzido,
			IplImage quadroCinza, CvSeq contour) {
		while (contour != null && !contour.isNull()) {
			if (contour.elem_size() > 0) {
				CvSeq points = cvApproxPoly(contour,
						Loader.sizeof(CvContour.class), mem, CV_POLY_APPROX_DP,
						cvContourPerimeter(contour) * 0.0025, 0);
				cvDrawContours(quadroReduzido, points, CvScalar.BLUE,
						CvScalar.BLUE, -1, 3, CV_AA);

				if (points != null && points.total() > 0) {
					CvRect box = cvBoundingRect(points, 0);
					cvRectangle(
							quadroReduzido,
							new CvPoint(box.x(), box.y()),
							new CvPoint(box.x() + box.width(), box.y()
									+ box.height()), CvScalar.GREEN);

					if (learningFrames == 0) {
						cvSetImageROI(quadroCinza, box);
						RectVector faces = detectaRosto(mem,
								classificadorFrente, quadroCinza);
						if (faces.size() > 0) {
							estado = "De frente";
							contadorDeCostas = 0;
							desenhaRetangulos(quadroReduzido, box, faces);
						} else {
							faces = detectaRosto(mem, classificadorPerfil,
									quadroCinza);
							if (faces.size() > 0) {
								estado = "De lado";
								contadorDeCostas = 0;
								desenhaRetangulos(quadroReduzido, box, faces);
							} else {
								cvFlip(quadroCinza, quadroCinza, 1);
								faces = detectaRosto(mem, classificadorPerfil,
										quadroCinza);
								cvFlip(quadroCinza, quadroCinza, 1);
								if (faces.size() > 0) {
									estado = "De lado";
									contadorDeCostas = 0;
									cvFlip(quadroReduzido, quadroReduzido, 1);
									box.x(quadroReduzido.width() - box.x());
									desenhaRetangulos(quadroReduzido, box,
											faces);
									box.x(quadroReduzido.width() - box.x());
									cvFlip(quadroReduzido, quadroReduzido, 1);
								} else {
									contadorDeCostas += System
											.currentTimeMillis()
											- tempoUltimoQuadro;
									if (contadorDeCostas > THRESHOLD_COSTAS) {
										estado = "De costas";
									}
								}
							}
						}
						tempoUltimoQuadro = System.currentTimeMillis();

						if (contadorDeCostas > TEMPO_MAXIMO_COSTAS) {
							contadorDeCostas = 0;
							tocaAlarme();
						}
						System.out.println("Costas : " + contadorDeCostas);
						cvResetImageROI(quadroCinza);
					}
				}

			}
			contour = contour.h_next();
		}
		return contour;
	}

	private CvSeq extraiContornos(CvMemStorage mem,
			OpenCVFrameConverter.ToIplImage conversor, Mat quadroContorno) {
		CvSeq contour = new CvSeq(null);
		cvFindContours(conversor.convert(conversor.convert(quadroContorno)),
				mem, contour, Loader.sizeof(CvContour.class), CV_RETR_EXTERNAL,
				CV_CHAIN_APPROX_TC89_L1);
		return contour;
	}

	private void extraiFundo(IplImage quadroReduzido, Mat quadroFG,
			BackgroundSubtractor extratorDeFundo, Mat kernel, Mat kernelDilate) {
		if (learningFrames > 0) {
			System.out.println("Learning " + learningFrames);
			learningFrames--;
			extratorDeFundo.apply(cvarrToMat(quadroReduzido), quadroFG, 0.001);
		} else {
			extratorDeFundo.apply(cvarrToMat(quadroReduzido), quadroFG, 0);
		}
		morphologyEx(quadroFG, quadroFG, MORPH_OPEN, kernel);
		morphologyEx(quadroFG, quadroFG, MORPH_CLOSE, kernel);
		dilate(quadroFG, quadroFG, kernelDilate);
	}

	private KeyPointVector detectaBlobs(IplImage quadroReduzido, Mat quadroFG,
			SimpleBlobDetector detectorDeBlob) {
		KeyPointVector blobs = new KeyPointVector();
		detectorDeBlob.detect(quadroFG, blobs);

		for (int i = 0; i < blobs.size(); i++) {
			KeyPoint blob = blobs.get(i);
			int x = (int) blob.pt().x();
			int y = (int) blob.pt().y();

			cvCircle(quadroReduzido, cvPoint(x, y), 16, CvScalar.RED, 4, 8, 0);
		}
		return blobs;
	}

	private void desenhaRetangulos(IplImage quadroReduzido, CvRect box,
			RectVector faces) {
		for (int i = 0; i < faces.size(); i++) {
			Rect r = faces.get(i);
			int x = r.x() + box.x(), y = r.y() + box.y(), w = r.width(), h = r
					.height();
			cvRectangle(quadroReduzido, cvPoint(x, y), cvPoint(x + w, y + h),
					CvScalar.RED, 4, CV_AA, 0);
		}
	}

	private RectVector detectaRosto(CvMemStorage mem,
			CascadeClassifier classificador, IplImage quadroCinza) {
		RectVector faces = new RectVector();
		classificador.detectMultiScale(cvarrToMat(quadroCinza), faces, 1.1, 3,
				CV_HAAR_DO_CANNY_PRUNING, new Size(20, 20), new Size(0, 0));
		return faces;
	}

	public CanvasFrame criaJanela(String nome, double gamma, double escala) {
		CanvasFrame janela = new CanvasFrame(nome, gamma);
		janela.setCanvasScale(escala);
		janela.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		return janela;
	}

	public void tocaAlarme() {
		try {
			URL url = this.getClass().getClassLoader()
					.getResource("buzzer.wav");
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
			Clip clip = AudioSystem.getClip();
			clip.open(audioIn);
			clip.start();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
}

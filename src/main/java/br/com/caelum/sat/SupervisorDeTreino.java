package br.com.caelum.sat;

import static org.bytedeco.javacpp.opencv_core.FONT_HERSHEY_COMPLEX_SMALL;
import static org.bytedeco.javacpp.opencv_core.LINE_AA;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
import static org.bytedeco.javacpp.opencv_core.cvFlip;
import static org.bytedeco.javacpp.opencv_core.cvPoint;
import static org.bytedeco.javacpp.opencv_core.cvarrToMat;
import static org.bytedeco.javacpp.opencv_imgproc.CV_AA;
import static org.bytedeco.javacpp.opencv_imgproc.cvRectangle;
import static org.bytedeco.javacpp.opencv_imgproc.putText;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.JFrame;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvScalar;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.RectVector;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import br.com.caelum.sat.filtro.EqualizeFiltro;
import br.com.caelum.sat.filtro.ResizeFiltro;
import br.com.caelum.sat.filtro.WebCamFonte;
import br.com.caelum.sat.model.Aula;
import br.com.caelum.sat.processo.DetectorDePostura;
import br.com.caelum.sat.processo.Postura;

public class SupervisorDeTreino {

	private static final double LARGURA_JANELA = 640.0;

	private CanvasFrame janela;
	private CanvasFrame janelaDebug;

	private Aula aula;

	public static void main(String[] args) {
		new SupervisorDeTreino().inicia();
	}

	public void inicia() {
		long tempoAnterior = System.currentTimeMillis();
		long tempoAtual;

		this.aula = new Aula();
		Loader.load(opencv_objdetect.class);

		processaAudio();

		DetectorDePostura detectorDePostura = new DetectorDePostura(aula);

		CvMemStorage mem = CvMemStorage.create();
		OpenCVFrameConverter.ToIplImage conversor = new OpenCVFrameConverter.ToIplImage();

		WebCamFonte webcam = (WebCamFonte) detectorDePostura.get("Webcam");
		double gamma = CanvasFrame.getDefaultGamma() / webcam.getGamma();

		janela = criaJanela("Webcam", gamma, 1.0);
		janelaDebug = criaJanela("Debug", 1.0, 1.0);
		janelaDebug.setBounds((int) LARGURA_JANELA, 0, janela.getWidth(),
				janela.getHeight());

		boolean finished = false;
		while (!finished) {
			detectorDePostura.reseta();
			cvClearMemStorage(mem);

			EqualizeFiltro filtro = (EqualizeFiltro) detectorDePostura
					.get("Equalize");

			ResizeFiltro filtroResize = (ResizeFiltro) detectorDePostura
					.get("Resize");
			IplImage quadroReduzido = filtroResize.getOutput();

			Postura postura = detectorDePostura.getPostura();
			RectVector faces = detectorDePostura.getResultado();
			desenhaRetangulos(quadroReduzido, faces, postura);

			Mat quadroFinal = cvarrToMat(quadroReduzido);
			putText(quadroFinal, postura.name(), new Point(10, 32),
					FONT_HERSHEY_COMPLEX_SMALL, 1.5, Scalar.BLACK, 5, LINE_AA,
					false);

			tempoAtual = System.currentTimeMillis();
			long tempoCorrido = tempoAtual - tempoAnterior;
			detectorDePostura.atualiza(tempoCorrido);

			Scalar cor = Scalar.GREEN;
			if (postura == Postura.INDEFINIDO) {
				cor = Scalar.RED;
			} else if (postura == Postura.DIREITA
					|| postura == Postura.ESQUERDA) {
				cor = Scalar.YELLOW;
			}

			putText(quadroFinal, postura.name(), new Point(10, 32),
					FONT_HERSHEY_COMPLEX_SMALL, 1.5, cor, 2, LINE_AA, false);

			janela.showImage(conversor.convert(quadroFinal));
			janelaDebug.showImage(conversor.convert(filtro.getOutput()));
			tempoAnterior = tempoAtual;
		}

		janela.dispose();
		janelaDebug.dispose();
		detectorDePostura.finaliza();
	}

	private void processaAudio() {
		try {
			AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(1024, 0);
			dispatcher.addAudioProcessor(new AudioProcessor() {
				float threshold = -50;// dB

				/**
				 * Returns the dBSPL for a buffer.
				 */
				private double soundPressureLevel(final float[] buffer) {
					double power = 0.0D;
					for (float element : buffer) {
						power += element * element;
					}
					double value = Math.pow(power, 0.5) / buffer.length;
					return 20.0 * Math.log10(value);
				}

				public boolean process(AudioEvent audioEvent) {
					float[] buffer = audioEvent.getFloatBuffer();
					double level = soundPressureLevel(buffer);
					System.out.println("Level " + level);
					if (level > threshold) {
						System.out.println("Sound detected.");
					}
					return true;
				}

				public void processingFinished() {
				}
			});
			new Thread(dispatcher).start();

		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void desenhaRetangulos(IplImage imagem, RectVector rects,
			Postura postura) {
		if (postura == Postura.ESQUERDA) {
			cvFlip(imagem, imagem, 1);
		}

		for (int i = 0; i < rects.size(); i++) {
			Rect r = rects.get(i);
			int x = r.x(), y = r.y(), w = r.width(), h = r.height();
			cvRectangle(imagem, cvPoint(x, y), cvPoint(x + w, y + h),
					CvScalar.RED, 4, CV_AA, 0);
		}

		if (postura == Postura.ESQUERDA) {
			cvFlip(imagem, imagem, 1);
		}
	}

	public CanvasFrame criaJanela(String nome, double gamma, double escala) {
		CanvasFrame janela = new CanvasFrame(nome, gamma);
		janela.setCanvasScale(escala);
		janela.addWindowListener(aula);
		janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return janela;
	}

}

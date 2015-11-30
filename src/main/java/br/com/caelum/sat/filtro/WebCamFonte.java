package br.com.caelum.sat.filtro;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter.ToIplImage;
import org.bytedeco.javacv.OpenCVFrameGrabber;

public class WebCamFonte extends Fonte<IplImage> {

	private ToIplImage conversor;
	private FrameGrabber webcam;
	private int width;
	private int height;
	private double gamma;
	private IplImage output;

	public WebCamFonte() {
		try {
			webcam = OpenCVFrameGrabber.createDefault(0);
			webcam.start();
			conversor = new OpenCVFrameConverter.ToIplImage();
			IplImage amostra = conversor.convert(webcam.grab());
			this.width = amostra.width();
			this.height = amostra.height();
			this.gamma = webcam.getGamma();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public IplImage getOutput() {
		if (!pronto) {
			try {
				output = conversor.convert(webcam.grab());
				pronto = true;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return output;
	}

	public double getGamma() {
		return gamma;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public void finaliza() {
		try {
			webcam.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}

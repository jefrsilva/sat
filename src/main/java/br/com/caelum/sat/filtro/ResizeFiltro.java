package br.com.caelum.sat.filtro;

import static org.bytedeco.javacpp.opencv_imgproc.*;

import org.bytedeco.javacpp.opencv_core.IplImage;

public class ResizeFiltro extends Filtro<IplImage, IplImage> {
	private int width;
	private int height;
	private IplImage output;

	public ResizeFiltro(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public IplImage getOutput() {
		if (output == null) {
			IplImage input = getInput();
			output = IplImage.create(width, height, input.depth(),
					input.nChannels());
		} else if (!pronto) {
			IplImage input = getInput();
			cvResize(input, output);
			pronto = true;
		}
		return output;
	}
}

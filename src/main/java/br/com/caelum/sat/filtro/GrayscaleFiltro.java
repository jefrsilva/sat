package br.com.caelum.sat.filtro;

import static org.bytedeco.javacpp.opencv_imgproc.*;

import org.bytedeco.javacpp.opencv_core.IplImage;

public class GrayscaleFiltro extends Filtro<IplImage, IplImage> {

	private IplImage output;

	@Override
	public IplImage getOutput() {
		IplImage input = getInput();
		if (output == null) {
			output = IplImage.create(input.width(), input.height(),
					input.depth(), 1);
		}
		if (!pronto) {
			cvCvtColor(input, output, CV_BGR2GRAY);
			pronto = true;
		}
		return output;
	}

}

package br.com.caelum.sat.filtro;

import static org.bytedeco.javacpp.opencv_imgproc.*;

import org.bytedeco.javacpp.opencv_core.IplImage;

public class GrayscaleFiltro extends Filtro<IplImage, IplImage> {

	private IplImage output;

	@Override
	public IplImage getOutput() {
		if (output == null) {
			IplImage input = getInput();
			output = IplImage.create(input.width(), input.height(),
					input.depth(), 1);
		} else if (!pronto) {
			IplImage input = getInput();
			cvCvtColor(input, output, CV_BGR2GRAY);
			pronto = true;
		}
		return output;
	}

}

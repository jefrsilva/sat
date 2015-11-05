package br.com.caelum.sat.filtro;

import static org.bytedeco.javacpp.opencv_imgproc.*;

import org.bytedeco.javacpp.opencv_core.IplImage;

public class EqualizeFiltro extends Filtro<IplImage, IplImage> {

	private IplImage output; 
	
	@Override
	public IplImage getOutput() {
		IplImage input = getInput();
		if (output == null) {
			output = input.clone();
		}
		if (!pronto) {
			cvEqualizeHist(input, output);
			pronto = true;
		}
		return output;
	}
}

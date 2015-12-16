package br.com.caelum.sat.filtro;

import static org.bytedeco.javacpp.opencv_core.cvarrToMat;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;

public class EqualizeFiltro extends Filtro<IplImage, IplImage> {

	private IplImage output;

	@Override
	public IplImage getOutput() {
		IplImage input = getInput();
		if (!pronto) {
			output = input.clone();
			Mat mat = cvarrToMat(output);
			mat.convertTo(mat, -1, 1.4, 0);
			// cvEqualizeHist(input, output);
			pronto = true;
		}
		return output;
	}
}

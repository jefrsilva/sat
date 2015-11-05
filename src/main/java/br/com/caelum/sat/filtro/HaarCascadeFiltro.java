package br.com.caelum.sat.filtro;

import static org.bytedeco.javacpp.opencv_core.cvarrToMat;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.RectVector;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;

public class HaarCascadeFiltro extends Filtro<IplImage, RectVector> {

	private CascadeClassifier classificador;
	private RectVector output;
	
	public HaarCascadeFiltro(String cascadePath) {
		String fullPath = this
				.getClass()
				.getClassLoader()
				.getResource(cascadePath)
				.getPath();
		classificador = new CascadeClassifier(fullPath);
	}
	
	@Override
	public RectVector getOutput() {
		IplImage input = getInput();
		if (output == null) {
			output = new RectVector();
		}
		if (!pronto) {
			classificador.detectMultiScale(cvarrToMat(input), output, 1.1, 3,
					CV_HAAR_DO_CANNY_PRUNING, new Size(20, 20), new Size(0, 0));
			pronto = true;
		}
		return output;
	}

}

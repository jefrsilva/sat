package br.com.caelum.sat.filtro;

import static org.bytedeco.javacpp.opencv_imgproc.*;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;

public class MorphologyFiltro extends Filtro<Mat, Mat> {

	public enum Operacao {
		OPEN, CLOSE, ERODE, DILATE;
	}

	private Mat output;
	private int operacao;
	private Mat kernel;

	public MorphologyFiltro(Operacao operacao, int tamanhoKernel) {
		switch (operacao) {
		case OPEN:
			this.operacao = MORPH_OPEN;
			break;

		case CLOSE:
			this.operacao = MORPH_CLOSE;
			break;

		case ERODE:
			this.operacao = MORPH_ERODE;
			break;

		case DILATE:
			this.operacao = MORPH_DILATE;
			break;
		}

		kernel = getStructuringElement(MORPH_ELLIPSE, new Size(tamanhoKernel,
				tamanhoKernel));
	}

	@Override
	public Mat getOutput() {
		Mat input = getInput();
		if (output == null) {
			output = input.clone();
		}
		if (!pronto) {
			morphologyEx(input, output, operacao, kernel);
			pronto = true;
		}
		return output;
	}

}

package br.com.caelum.sat.filtro;

import static org.bytedeco.javacpp.opencv_core.cvFlip;

import org.bytedeco.javacpp.opencv_core.IplImage;

public class FlipFiltro extends Filtro<IplImage, IplImage> {

	public enum Orientacao {
		HORIZONTAL, VERTICAL
	}

	private int orientacao;
	private IplImage output;

	public FlipFiltro(Orientacao orientacao) {
		switch (orientacao) {
		case HORIZONTAL:
			this.orientacao = 1;
			break;

		case VERTICAL:
			this.orientacao = 0;
			break;
		}
	}

	@Override
	public IplImage getOutput() {
		IplImage input = getInput();
		if (output == null) {
			output = input.clone();
		}
		if (!pronto) {
			cvFlip(input, output, orientacao);
			pronto = true;
		}
		return output;
	}
}

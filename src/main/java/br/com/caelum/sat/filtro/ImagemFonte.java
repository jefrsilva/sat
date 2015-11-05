package br.com.caelum.sat.filtro;

import org.bytedeco.javacpp.opencv_core.IplImage;

public class ImagemFonte extends Fonte<IplImage> {

	private IplImage input;

	public ImagemFonte(IplImage imagem) {
		this.input = imagem;
	}
	
	@Override
	public IplImage getOutput() {
		return input;
	}

}

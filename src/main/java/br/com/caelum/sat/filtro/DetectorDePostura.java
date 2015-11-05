package br.com.caelum.sat.filtro;

import br.com.caelum.sat.filtro.MorphologyFiltro.Operacao;

public class DetectorDePostura extends Processo {
	private static final double LARGURA_JANELA = 640.0;

	public DetectorDePostura() {
		WebCamFonte webcam = new WebCamFonte();
		add("Webcam", webcam);

		double fatorDeEscala = LARGURA_JANELA / webcam.getWidth();
		int width = (int) (webcam.getWidth() * fatorDeEscala);
		int height = (int) (webcam.getHeight() * fatorDeEscala);
		
		ResizeFiltro filtroResize = new ResizeFiltro(width, height);
		webcam.conecta(filtroResize);
		add("Resize", filtroResize);
		
		GrayscaleFiltro filtroGrayscale = new GrayscaleFiltro();
		filtroResize.conecta(filtroGrayscale);
		add("Grayscale", filtroGrayscale);
		
		EqualizeFiltro filtroEqualize = new EqualizeFiltro();
		filtroGrayscale.conecta(filtroEqualize);
		add("Equalize", filtroEqualize);
		
		BackgroundSubFiltro filtroBGSub = new BackgroundSubFiltro(150, 5000, 256, false);
		filtroEqualize.conecta(filtroBGSub);
		add("BackgroundSub", filtroBGSub);
		
		MorphologyFiltro filtroMorphOpen = new MorphologyFiltro(Operacao.OPEN, 9);
		filtroBGSub.conecta(filtroMorphOpen);
		add("MorphOpen", filtroMorphOpen);

		MorphologyFiltro filtroMorphClose = new MorphologyFiltro(Operacao.CLOSE, 9);
		filtroMorphOpen.conecta(filtroMorphClose);
		add("MorphClose", filtroMorphClose);

		MorphologyFiltro filtroMorphDilate = new MorphologyFiltro(Operacao.DILATE, 27);
		filtroMorphClose.conecta(filtroMorphDilate);
		add("MorphDilate", filtroMorphDilate);
	}
}

package br.com.caelum.sat.processo;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.opencv_core.RectVector;

import br.com.caelum.sat.filtro.BackgroundSubFiltro;
import br.com.caelum.sat.filtro.EqualizeFiltro;
import br.com.caelum.sat.filtro.FlipFiltro;
import br.com.caelum.sat.filtro.FlipFiltro.Orientacao;
import br.com.caelum.sat.filtro.GrayscaleFiltro;
import br.com.caelum.sat.filtro.HaarCascadeFiltro;
import br.com.caelum.sat.filtro.MorphologyFiltro;
import br.com.caelum.sat.filtro.MorphologyFiltro.Operacao;
import br.com.caelum.sat.filtro.ResizeFiltro;
import br.com.caelum.sat.filtro.WebCamFonte;

public class DetectorDePostura extends Processo {
	private static final double LARGURA_QUADRO = 640.0;

	public enum Postura {
		FRENTE, ESQUERDA, DIREITA, INDEFINIDO
	}

	public class ParHaarPostura {
		public HaarCascadeFiltro filtro;
		public Postura postura;

		public ParHaarPostura(HaarCascadeFiltro filtro, Postura postura) {
			this.filtro = filtro;
			this.postura = postura;
		}
	}

	private Postura postura;
	private ParHaarPostura parFrente;
	private ParHaarPostura parEsquerda;
	private ParHaarPostura parDireita;
	private List<ParHaarPostura> ordemDeTeste;
	private RectVector resultado;

	public DetectorDePostura() {
		postura = Postura.FRENTE;
		ordemDeTeste = new ArrayList<ParHaarPostura>();

		WebCamFonte webcam = new WebCamFonte();
		add("Webcam", webcam);

		double fatorDeEscala = LARGURA_QUADRO / webcam.getWidth();
		int width = (int) (webcam.getWidth() * fatorDeEscala);
		int height = (int) (webcam.getHeight() * fatorDeEscala);
		ResizeFiltro resize = new ResizeFiltro(width, height);
		add("Resize", resize);

		GrayscaleFiltro grayscale = new GrayscaleFiltro();
		add("Grayscale", grayscale);

		EqualizeFiltro equalize = new EqualizeFiltro();
		add("Equalize", equalize);

		BackgroundSubFiltro bgSub = new BackgroundSubFiltro(150, 5000, 256,
				false);
		add("BackgroundSub", bgSub);

		MorphologyFiltro morphOpen = new MorphologyFiltro(Operacao.OPEN, 9);
		add("MorphOpen", morphOpen);

		MorphologyFiltro morphClose = new MorphologyFiltro(Operacao.CLOSE, 9);
		add("MorphClose", morphClose);

		MorphologyFiltro morphDilate = new MorphologyFiltro(Operacao.DILATE, 27);
		add("MorphDilate", morphDilate);

		HaarCascadeFiltro frontalFaceHaar = new HaarCascadeFiltro(
				"haarcascades/haarcascade_frontalface_default.xml");
		add("FrontalFaceHaar", frontalFaceHaar);
		parFrente = new ParHaarPostura(frontalFaceHaar, Postura.FRENTE);

		HaarCascadeFiltro profileFaceLeftHaar = new HaarCascadeFiltro(
				"haarcascades/haarcascade_profileface.xml");
		add("ProfileFaceLeftHaar", profileFaceLeftHaar);
		parEsquerda = new ParHaarPostura(profileFaceLeftHaar, Postura.ESQUERDA);

		HaarCascadeFiltro profileFaceRightHaar = new HaarCascadeFiltro(
				"haarcascades/haarcascade_profileface.xml");
		add("ProfileFaceRightHaar", profileFaceRightHaar);
		parDireita = new ParHaarPostura(profileFaceRightHaar, Postura.DIREITA);

		FlipFiltro flip = new FlipFiltro(Orientacao.HORIZONTAL);
		add("Flip", flip);

		webcam.conecta(resize);
		resize.conecta(grayscale);
		grayscale.conecta(equalize);
		equalize.conecta(bgSub);
		bgSub.conecta(morphOpen);
		morphOpen.conecta(morphClose);
		morphClose.conecta(morphDilate);
		equalize.conecta(frontalFaceHaar);
		equalize.conecta(profileFaceRightHaar);
		equalize.conecta(flip);
		flip.conecta(profileFaceLeftHaar);
	}
	
	public Postura getPostura() {
		ordemDeTeste.clear();
		if (postura == Postura.FRENTE || postura == Postura.INDEFINIDO) {
			ordemDeTeste.add(parFrente);
			ordemDeTeste.add(parDireita);
			ordemDeTeste.add(parEsquerda);
		} else if (postura == Postura.DIREITA) {
			ordemDeTeste.add(parDireita);
			ordemDeTeste.add(parFrente);
			ordemDeTeste.add(parEsquerda);
		} else if (postura == Postura.ESQUERDA) {
			ordemDeTeste.add(parEsquerda);
			ordemDeTeste.add(parFrente);
			ordemDeTeste.add(parDireita);
		}

		for (ParHaarPostura par : ordemDeTeste) {
			RectVector objetos = par.filtro.getOutput();
			if (objetos.size() > 0) {
				resultado = objetos;
				postura = par.postura;
				return par.postura;
			}
		}
		resultado = new RectVector();
		return Postura.INDEFINIDO;
	}
	
	public RectVector getResultado() {
		return resultado;
	}
}

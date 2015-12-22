package br.com.caelum.sat.processo;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

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
import br.com.caelum.sat.model.Aula;
import br.com.caelum.sat.model.Evento;
import br.com.caelum.sat.model.EventoAlertaPosturaIncorreta;
import br.com.caelum.sat.model.EventoTrocaDePostura;

public class DetectorDePostura extends Processo {
	private static final double LARGURA_QUADRO = 640.0;
	public static final long TEMPO_MAX_INDEFINIDO = 5000;

	private Postura postura = null;
	private Postura posturaAnterior = Postura.FRENTE;
	private ParHaarPostura parFrente;
	private ParHaarPostura parEsquerda;
	private ParHaarPostura parDireita;
	private List<ParHaarPostura> ordemDeTeste = new ArrayList<ParHaarPostura>();
	private RectVector resultado;
	private long tempoIndefinido = 0;

	public DetectorDePostura(Aula aula) {
		super(aula);
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
		if(postura == null){
			ordemDeTeste.clear();
			if (posturaAnterior == Postura.FRENTE || posturaAnterior == Postura.INDEFINIDO) {
				ordemDeTeste.add(parFrente);
				ordemDeTeste.add(parDireita);
				ordemDeTeste.add(parEsquerda);
			} else if (posturaAnterior == Postura.DIREITA) {
				ordemDeTeste.add(parDireita);
				ordemDeTeste.add(parFrente);
				ordemDeTeste.add(parEsquerda);
			} else if (posturaAnterior == Postura.ESQUERDA) {
				ordemDeTeste.add(parEsquerda);
				ordemDeTeste.add(parFrente);
				ordemDeTeste.add(parDireita);
			}
			
			for (ParHaarPostura par : ordemDeTeste) {
				RectVector objetos = par.filtro.getOutput();
				if (objetos.size() > 0) {
					resultado = objetos;
					postura = par.postura;
					return postura;
				}
			}
			resultado = new RectVector();
			postura = Postura.INDEFINIDO;
			return postura;
		}
		
		return postura;

	}
	
	public RectVector getResultado() {
		return resultado;
	}

	public void atualiza(long tempoCorrido) {
		Postura postura = getPostura();
		if(postura != posturaAnterior){
			EventoTrocaDePostura trocaDePostura = new EventoTrocaDePostura(postura);
			aula.addEvento(trocaDePostura);
		}
		if (postura == Postura.INDEFINIDO) {
			tempoIndefinido  += tempoCorrido;
			if(tempoIndefinido >= TEMPO_MAX_INDEFINIDO){
				tocaAlarme();
				tempoIndefinido = 0;
			}
			
		} else{
			tempoIndefinido = 0;
		}
		
	}
	
	@Override
	public void reseta() {
		posturaAnterior = postura;
		postura = null;
		super.reseta();
	}
	
	public void tocaAlarme() {
		Evento evento = new EventoAlertaPosturaIncorreta();
		aula.addEvento(evento);
		
		try {
			URL url = this.getClass().getClassLoader()
					.getResource("buzzer.wav");
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
			Clip clip = AudioSystem.getClip();
			clip.open(audioIn);
			clip.start();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
}

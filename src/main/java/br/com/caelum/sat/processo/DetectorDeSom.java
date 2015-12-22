package br.com.caelum.sat.processo;

import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import br.com.caelum.sat.model.Aula;
import br.com.caelum.sat.model.Evento;
import br.com.caelum.sat.model.EventoAlertaSilencio;

public class DetectorDeSom implements AudioProcessor{

	private Aula aula;
	private long tempoAnterior;
	private static final float SOM_MINIMO = -70; //dB
	public static final long TEMPO_MAXIMO_EM_SILENCIO = 2000;
	private long tempoEmSilencio = 0;

	public DetectorDeSom(Aula aula) {
		this.aula = aula;
		tempoAnterior = System.currentTimeMillis();
		inicializa();
	}
	
	private void inicializa() {
		try {
			AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(1024, 0);
			dispatcher.addAudioProcessor(this);
			new Thread(dispatcher).start();

		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the dBSPL for a buffer.
	 */
	private double soundPressureLevel(final float[] buffer) {
		double power = 0.0D;
		for (float element : buffer) {
			power += element * element;
		}
		double value = Math.pow(power, 0.5) / buffer.length;
		return 20.0 * Math.log10(value);
	}

	public boolean process(AudioEvent audioEvent) {
		long tempoAtual = System.currentTimeMillis();
		
		float[] buffer = audioEvent.getFloatBuffer();
		double level = soundPressureLevel(buffer);
		
		if (level < SOM_MINIMO ){
			tempoEmSilencio += tempoAtual - tempoAnterior;
			if(tempoEmSilencio > TEMPO_MAXIMO_EM_SILENCIO){
				tocaAlarme();
				tempoEmSilencio = 0;
			} 
		} else {
			tempoEmSilencio = 0;
		}
		
		
		tempoAnterior = tempoAtual;
		return true;
	}

	public void processingFinished() {
	}
	
	public void tocaAlarme() {
		Evento evento = new EventoAlertaSilencio();
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

package br.com.caelum.sat.model;

import br.com.caelum.sat.processo.DetectorDeSom;

public class EventoAlertaSilencio extends Evento {

	public EventoAlertaSilencio() {
		super("Silencio detectado por mais de "
				+ DetectorDeSom.TEMPO_MAXIMO_EM_SILENCIO / 1000 + " segundos.");
	}

}

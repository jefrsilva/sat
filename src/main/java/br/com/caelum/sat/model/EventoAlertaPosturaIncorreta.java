package br.com.caelum.sat.model;

import br.com.caelum.sat.processo.DetectorDePostura;

public class EventoAlertaPosturaIncorreta extends Evento {

	public EventoAlertaPosturaIncorreta() {
		super("Postura incorreta por mais de "
				+ DetectorDePostura.TEMPO_MAX_INDEFINIDO / 1000 + " segundos.");
	}

}

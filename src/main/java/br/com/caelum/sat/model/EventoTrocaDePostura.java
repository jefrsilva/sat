package br.com.caelum.sat.model;

import br.com.caelum.sat.processo.Postura;

public class EventoTrocaDePostura extends Evento {

	public EventoTrocaDePostura(Postura postura) {
		super("Troca de Postura para " + postura.name());
	}

}

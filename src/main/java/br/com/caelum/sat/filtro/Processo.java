package br.com.caelum.sat.filtro;

import java.util.HashMap;
import java.util.Map;

public class Processo {

	private Map<String, Filtro<?, ?>> filtros;

	public Processo() {
		filtros = new HashMap<String, Filtro<?, ?>>();
	}
	
	public void add(String nome, Filtro<?,?> filtro) {
		filtros.put(nome, filtro);
		filtro.setProcesso(this);
	}

	public Filtro<?,?> get(String nome) {
		return filtros.get(nome);
	}
	
	public void reseta() {
		for (Filtro<?, ?> filtro : filtros.values()) {
			filtro.reseta();
		}
	}

}

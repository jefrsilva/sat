package br.com.caelum.sat.processo;

import java.util.HashMap;
import java.util.Map;

import br.com.caelum.sat.filtro.Filtro;
import br.com.caelum.sat.model.Aula;

public class Processo {

	private Map<String, Filtro<?, ?>> filtros;
	protected Aula aula;

	public Processo(Aula aula) {
		this.aula = aula;
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
	
	public void finaliza() {
		for (Filtro<?, ?> filtro : filtros.values()) {
			filtro.finaliza();
		}
	}

}

package br.com.caelum.sat.filtro;

import br.com.caelum.sat.processo.Processo;

public abstract class Filtro<T1, T2> {

	protected boolean pronto;
	private Filtro<?, T1> dependencia;
	private Processo processo;

	public void conecta(Filtro<T2, ?> filtro) {
		filtro.adicionaDependencia(this);
	}

	private void adicionaDependencia(Filtro<?, T1> filtro) {
		this.dependencia = filtro;
	}

	public T1 getInput() {
		Filtro<?,T1> filtro = getDependencia();
		return filtro.getOutput();
	}
	
	public abstract T2 getOutput();

	public Filtro<?, T1> getDependencia() {
		return dependencia;
	}
	
	public void reseta() {
		pronto = false;
	}
	
	public void finaliza() {
		
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}
	
	public Processo getProcesso() {
		return processo;
	}

}

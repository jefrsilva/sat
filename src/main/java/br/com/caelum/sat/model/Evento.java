package br.com.caelum.sat.model;

public class Evento {
	
	protected String descricao;
	protected long timeStamp;
	
	public Evento(String descricao) {
		this.descricao = descricao;
		this.timeStamp = System.currentTimeMillis();
	}
	
	@Override
	public String toString() {
		return "[" + timeStamp + "] " + descricao;
	}
	
	

}

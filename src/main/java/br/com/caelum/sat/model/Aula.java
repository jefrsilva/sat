package br.com.caelum.sat.model;

public class Aula {

	private int indefinido = 0;
	private int esquerda = 0;
	private int direita = 0;
	
	public void addQtdEsquerda() {
		esquerda++;
	}

	public void addQtdIndefinido() {
		indefinido++;
	}

	public void addQtdDireita() {
		direita++;
	}

}

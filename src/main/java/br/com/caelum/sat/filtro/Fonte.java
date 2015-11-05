package br.com.caelum.sat.filtro;

public abstract class Fonte<T> extends Filtro<Void, T> {

	@Override
	public Void getInput() {
		return null;
	}

}

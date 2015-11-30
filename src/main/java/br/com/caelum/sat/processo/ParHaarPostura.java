package br.com.caelum.sat.processo;

import br.com.caelum.sat.filtro.HaarCascadeFiltro;

class ParHaarPostura {
	public HaarCascadeFiltro filtro;
	public Postura postura;

	public ParHaarPostura(HaarCascadeFiltro filtro, Postura postura) {
		this.filtro = filtro;
		this.postura = postura;
	}
}

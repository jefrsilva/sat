package br.com.caelum.sat.model;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.com.caelum.sat.processo.DetectorDePostura;

public class Aula implements WindowListener {
	
	private List<Evento> eventos = new ArrayList<Evento>();
	private SimpleDateFormat formatoDeData = new SimpleDateFormat("ddMMyy-HHmm");
	private String tempoDeInicio;
	
	public Aula() {
		Calendar calendar = Calendar.getInstance();
		this.tempoDeInicio = formatoDeData.format(calendar.getTime());
	}

	public void addEvento(Evento evento) {
		eventos.add(evento);
	}

	public void windowClosing(WindowEvent event) {
		if(eventos.isEmpty()){
			return;
		}
		
		StringBuilder builder = new StringBuilder();
		for (Evento evento : eventos) {
			builder.append(evento);
			builder.append("\n");
		}
		
		try {
			PrintStream stream = new PrintStream("./estatiscas_sat_" + tempoDeInicio +".txt");
			stream.print(builder.toString());
			stream.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void windowOpened(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
		
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

}

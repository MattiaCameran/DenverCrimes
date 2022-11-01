package it.polito.tdp.crimes.model;

public class TestModel {

	public static void main(String[] args) {
		
		Model m = new Model();
		m.creaGrafo("traffic-accident", 2);
		
		System.out.println(m.getArchiMaggioriPesoMedio());
	}

}

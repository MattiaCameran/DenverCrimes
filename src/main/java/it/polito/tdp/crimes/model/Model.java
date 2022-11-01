package it.polito.tdp.crimes.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.crimes.db.EventsDao;

public class Model {
	
	//Nel modello come al solito creo il grafo. I vertici sono tipi di reato che nella tabella del database si vede essere
	//VARCHAR, quindi essi saranno delle stringhe.
	private Graph<String, DefaultWeightedEdge> grafo;
	
	//Devo anche come sempre creare il riferimento al DAO.
	private EventsDao dao;
	
	private List<String> camminoMassimo;	//Mi creo la lista dove memorizzare il cammino del punto 2.
	
	public Model() {
		this.dao = new EventsDao();
	}
	
	//Il grafo lo creo come al solito in un metodo void
	public void creaGrafo(String categoria, int mese) {
		
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
	
		//Aggiunta vertici. Corrispondono a tutte le tipologie di una categoria avvenute in un determinato mese.
		Graphs.addAllVertices(this.grafo, dao.getVertici(categoria, mese));
		
		//Aggiunta archi. Dati due tipi di reato, il peso dell'arco eventuale è pari al numero di quartieri distinti in cui sono avvenuti nello stesso mese.
		for(Adiacenza a: this.dao.getArchi(categoria, mese)) {
			Graphs.addEdgeWithVertices(this.grafo, a.getV1(), a.getV2(), a.getPeso());
		}
		
		System.out.println("Grafo creato!");
		System.out.println("Numero vertici "+this.grafo.vertexSet().size());
		System.out.println("Numero archi "+this.grafo.edgeSet().size());
		
	}
	
	//Ora risolvo il punto d
	
	public List<Adiacenza> getArchiMaggioriPesoMedio(){
		
		//Scorro gli archi del grafo e calcolo il peso medio
		double pesoTot = 0.0;
		
		for(DefaultWeightedEdge e: this.grafo.edgeSet()) {
			pesoTot += this.grafo.getEdgeWeight(e);
		}
		double average = pesoTot / this.grafo.edgeSet().size();
		
		System.out.println("Peso medio: "+ average);
		//Riscorro tutti gli archi prendendo quelli con peso maggiore di avg
		List<Adiacenza> result = new ArrayList<Adiacenza>();
		
		for(DefaultWeightedEdge e: this.grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(e) > average) {
				result.add(new Adiacenza(this.grafo.getEdgeSource(e), this.grafo.getEdgeTarget(e), (int)(this.grafo.getEdgeWeight(e))));	//Inserisco l'adiacenza nella lista. Recupero il nodo sorgente, il nodo target e il peso tramite questi metodi.
			}
		}
		
		return result;
		
	}
	
	//Il punto 1 è fatto aldilà del collegamento al controller.
	
	//Il punto 2 mi chiede il cammino massimo. Posso farlo tramite algoritmo ricorsivo.


	public List<String> calcolaPercorso(String sorgente, String destinazione){
		
		camminoMassimo = new LinkedList<String>();	//Questa sarà la lista definitiva.
		
		List<String> parziale = new LinkedList<String>();
		
		parziale.add(sorgente);	//Io so già che parto dalla sorgente.
		
		//Ora non so che percorso sceglierò per arrivare a destinazione, quindi lancio la ricorsione.
		cerca(parziale, destinazione);
		
		return camminoMassimo;
	}

	private void cerca(List<String> parziale, String destinazione) {
		
		//Identifico subito la condizione di terminazione.
		//Mi fermo se arrivo a destinazione.
		if(parziale.get(parziale.size()-1).equals(destinazione)) {	//Se l'ultimo elemento della lista è destinazione
			
			//controllo. E' la soluzione migliore?
			if(parziale.size() > camminoMassimo.size()) {
				camminoMassimo = new LinkedList<>(parziale);	//Se è una soluzione migliore della precedente, sovrascrivo.
				
				return;											//Che sia la migliore o no, io sono comunque arrivato a destinazione. Finisco.
				
			}
		//STATO NORMALE:
				
			//Esploro i percorsi che vanno verso tutti gli adiacenti, poi faccio backtracking.
			//Scorro i vicini dell'ultimo inserito e provo le varie "strade".
			
			for(String v : Graphs.neighborListOf(this.grafo, parziale.get(parziale.size()-1))){			//Per ogni vertice appartenente alla lista dei vicini rispetto all'ultimo vertice inserito nella lista.
				
				if(!parziale.contains(v))	//se la lista non contiene v, lo aggiungo. Se non mettessi questa condizione andrei incontro ad un ciclo. Avrei ricorsione infinita.
				
				parziale.add(v);			//Aggiungo il vertice alla lista e provo la ricorsione su questo. Prima o poi arriverò a condizione terminale e otterrò un cammino massimo.
											//Poi tornerò indietro e genererò altri percorsi fino a condizione terminale e così via. Alla fine della mia ricorsione avrò il cammino massimo.
				cerca(parziale, destinazione);
				parziale.remove(parziale.size()-1);
			}
		}
		
	}
}

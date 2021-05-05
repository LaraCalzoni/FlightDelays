package it.polito.tdp.extflightdelays.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.*;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;
public class Model {

	private SimpleWeightedGraph <Airport, DefaultWeightedEdge> grafo;
	private ExtFlightDelaysDAO dao;
	private Map <Integer, Airport> idMap;
	private Map <Airport, Airport> visita ;
	
	public Model() {
		
		dao = new ExtFlightDelaysDAO ();
		idMap = new HashMap <>();
		//visto che è idMap la vado a riempire con tutti aeroporti presenti nel db
		dao.loadAllAirports(idMap);
		
	}
	
	public void creaGrafo(int x) {
		grafo = new SimpleWeightedGraph(DefaultWeightedEdge.class);
		
		// non va bene Graphs.addAllVertices(grafo, idMap.values());
		//perchè non devo prendere tutti aeroporti ma devo filtrarli--> faccio una query
		//aggiungo vertici "filtrati"
		Graphs.addAllVertices(grafo, dao.getVertici(x, idMap));
		
		//aggiungo gli archi
		
		for(Rotta r : dao.getRotte(idMap)) {
			//devo controllare se rotta è relativa al mio grafo (quindi solo se grafo contiene
			// i due aeroporti in questione)
			if(this.grafo.containsVertex(r.getA1()) && this.grafo.containsVertex(r.getA2())) {
				DefaultWeightedEdge e = this.grafo.getEdge(r.getA1(), r.getA2()); //se è grafo non pesato,
				// nel getEdge() non è importante ordine dei due parametri che gli passo
				
				if(e== null ) { //vuol dire che non esiste ancora arco tra i due vertici
				Graphs.addEdgeWithVertices(grafo,r.getA1(),r.getA2(),r.getN());
				
				}
				else { //vuol dire che c'era già arco e sto facendo rotta inversa
					
				double pesoVecchio = this.grafo.getEdgeWeight(e);
				double pesoNuovo = pesoVecchio + r.getN();
				this.grafo.setEdgeWeight(e, pesoNuovo);
				}
			}
		}
		
		System.out.println("GRAFO CREATO");
		System.out.println("# Vertici: "+grafo.vertexSet().size());
		System.out.println("# Archi: "+grafo.edgeSet().size());
	}

	public Set <Airport> getVertici() {
		return this.grafo.vertexSet();
	}
	
	public List <Airport> trovaPercorso(Airport a1, Airport a2){
		List <Airport> percorso = new LinkedList <>();
		
		//creo iteratore per fare la visita
		BreadthFirstIterator <Airport, DefaultWeightedEdge> it = new BreadthFirstIterator <>(grafo, a1);
		
		visita = new HashMap<>();
		visita.put(a1, null); //nodo di partenza è radice e quindi è associato a null
		
		it.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>(){

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> e) {
				// TODO Auto-generated method stub
				Airport airport1 = grafo.getEdgeSource(e.getEdge());
				Airport airport2 = grafo.getEdgeTarget(e.getEdge());
				
				if(visita.containsKey(airport1) && !visita.containsKey(airport2)) {
					//vuol dire che a1 lo avevo già visitato in precedenza e quindi
					//a1 sarà padre di a2
					visita.put(airport2, airport1);
				}
				else if(visita.containsKey(airport2) && !visita.containsKey(airport1)) {
				visita.put(airport1, airport2);
				}
				
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}});
		
		//finchè iteratore ha prossimo nodo da visitare, lo visito
		while(it.hasNext()) {
			it.next();
		}
		
		percorso.add(a2);
		//devo andare all'indietro --> aggiungo destinazione e poi risalgo questa mappa
		
		Airport step = a2;
		while(visita.get(step)!=null) {
			step = visita.get(step);
			percorso.add(step);
		}
		
	
		return percorso;
	}
	
}

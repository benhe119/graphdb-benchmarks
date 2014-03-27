package eu.socialsensor.insert;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

public class OrientMassiveInsertion implements Insertion {
	
	private OrientGraphNoTx orientGraph = null;
	Index<OrientVertex> vetrices = null;
			
	public OrientMassiveInsertion(OrientGraphNoTx orientGraph, Index<OrientVertex> vertices) {
		this.orientGraph = orientGraph;
		this.vetrices = vertices;
	}
	
	public void createGraph(String datasetDir) {
		System.out.println("Loading data in massive mode in OrientDB database");
		orientGraph.getRawGraph().declareIntent(new OIntentMassiveInsert());
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(datasetDir)));
			String line;
			int lineCounter = 1;
			OrientVertex srcVertex, dstVertex;
			Iterable<OrientVertex> cache;
			while((line = reader.readLine()) != null) {
				if(lineCounter > 4) {
					String[] parts = line.split("\t");
					cache = vetrices.get("nodeId", parts[0]);
					if(cache.iterator().hasNext()) {
						srcVertex = cache.iterator().next();
					}
					else {
						srcVertex = orientGraph.addVertex(null, "nodeId", parts[0] );
						vetrices.put("nodeId", parts[0], srcVertex);
					}

					cache = vetrices.get("nodeId", parts[1]);
					if(cache.iterator().hasNext()) {
						dstVertex = cache.iterator().next();
					}
					else {
						dstVertex = orientGraph.addVertex(null, "nodeId", parts[1] );
						vetrices.put("nodeId", parts[1], dstVertex);
					}
					
					if(parts[0].equals(parts[1])) {
						dstVertex = srcVertex;
					}
					
					orientGraph.addEdge(null, srcVertex, dstVertex, "similar");
				}
				lineCounter++;
			}			
			reader.close();
			orientGraph.getRawGraph().declareIntent(null);
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}

}

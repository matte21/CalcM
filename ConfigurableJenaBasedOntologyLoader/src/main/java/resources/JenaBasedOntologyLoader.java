/**
 * This class works with jena libraries correctly imported into namespace. Libraries are not directly included
 * for reasosns of dimension and because of restrictions on directly including Jena liraries into external projects.
 * Jena libraries can be downloaded from the official Jena libraries website
 * @author Alfredo D'Elia
 *
 */

package resources;

import java.util.Vector;

import sofia_kp.KPICore;
import sofia_kp.SIBResponse;
import sofia_kp.SSAP_XMLTools;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;



public class JenaBasedOntologyLoader {

	private int 					sibPort;
	private String 					sibHost;
	private String 					sibNameSpace;
	private String 					ontologyPath;
	private KPICore 				kp;
	private SIBResponse 			resp;
	private Vector<Vector<String>> 	literalTriples;
	private Vector<Vector<String>> 	objectTriples;
	
	
	public JenaBasedOntologyLoader(int sibPort, String sibHost, String sibNameSpace, String ontologyPath) {
		this.sibPort 		= sibPort;
		this.sibHost 		= sibHost;
		this.sibNameSpace 	= sibNameSpace;
		this.ontologyPath 	= ontologyPath;
		this.resp 			= new SIBResponse();
		this.literalTriples = new Vector<Vector<String>>();
		this.objectTriples 	= new Vector<Vector<String>>();
	}

	public void LoadOntologyIntoSIB (String ontologyPath) {
		OntModel model = ModelFactory.createOntologyModel();
		model.read(ontologyPath);
		StmtIterator it = model.listStatements();
		//System.out.println("***" + it.toList().size());
		Statement st;
		while (it.hasNext()) {
			st = it.next();
			if(st.getObject().isLiteral()) {
				addLiteralTriple(st);
			}
			else {
				addObjectTriple(st);
			}
		}

		boolean ok = bufferedInsert();
//		String ontString = "";
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		model.write(baos);
//		
//		kp = new KPICore(sibHost, sibPort, sibNameSpace);
//		kp.join();
//		ontString = baos.toString();
//		System.out.println("*****************" + ontString);
//		resp = kp.insert_rdf_xml(ontString);//.replace("\n", "").replace("\r", ""));
		if(ok) {
			System.out.println("Ontology correctly inserted");
		}
		else {
			System.out.println("Ontology not inserted");
		}
	}
	
	public void LoadOntologyIntoSIB () {
		this.LoadOntologyIntoSIB(this.ontologyPath);
	}
	
	public void addLiteralTriple(Statement st) {
		Vector<String> temp = new Vector<String>();
		SSAP_XMLTools  ssap = new SSAP_XMLTools();
		temp = ssap.newTriple(st.getSubject().getURI(), st.getPredicate().getURI(), 
							  st.getObject().asLiteral().getString(), "uri", "literal");
		literalTriples.add(temp);
	}
	
	public void addObjectTriple(Statement st) {
		Vector<String> temp = new Vector<String>();
		SSAP_XMLTools  ssap = new SSAP_XMLTools();
		temp = ssap.newTriple(st.getSubject().getURI(), st.getPredicate().getURI(), st.getObject().asResource().getURI(), "uri", "uri");
	    objectTriples.add(temp);
	}
	
	// Problem with this method: If inserting a triple fails, there's no cleanup as for the triples
	// that were already inserted, AKA only a part of the ontology is loaded.
	// This could happen, for instance, if there were more than 100 triples to insert. The first
	// 100 triples would be successfully inserted, while the second group would not (i.e. resp.isConfirmed() 
	// returns false.
	public boolean bufferedInsert() {
		kp = new KPICore(sibHost, sibPort, sibNameSpace);
		kp.join();
		Vector<Vector<String>> triples;
		while((objectTriples.size()>0) || (literalTriples.size()>0)) {
			triples = new Vector<Vector<String>>();
			while(triples.size()<100 && ((objectTriples.size()>0) || (literalTriples.size()>0))) {
				if(objectTriples.size()>0) {
					triples.add(objectTriples.firstElement());
					objectTriples.remove(0);
				}
				if(literalTriples.size()>0) {
					triples.add(literalTriples.firstElement());
					literalTriples.remove(0);
				}
			}
			resp = kp.insert(triples);
			if(!resp.isConfirmed()) {
				return false;
			}
		}
		return true;	
	}

}

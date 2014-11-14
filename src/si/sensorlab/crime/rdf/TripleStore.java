package si.sensorlab.crime.rdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;

import si.sensorlab.crime.config.Module;
import si.sensorlab.crime.config.Primitive;
import si.sensorlab.crime.config.Stack;
import si.sensorlab.crime.exceptions.CRimeException;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;


public class TripleStore {
	private Repository myRepository;
	private RepositoryConnection con = null;
	
	static String queryModulesStr = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX cpan: <http://downlode.org/rdf/cpan/0.1/cpan.rdf#> " +
			"PREFIX crime: <http://localhost/owl/crime.owl#> " +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			"SELECT ?name ?category ?description " 
			+ "WHERE { "
				+ "?name rdf:type cpan:Module ."
				+ "?name crime:hasScope ?category ."
				+ "?name rdfs:comment ?description ."
				+ "}";
	static String queryRequiredParamStr = 
			"PREFIX crime: <http://localhost/owl/crime.owl#> " +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			"SELECT  ?name "
			+ "WHERE { "
			+ "module crime:hasParameter ?name . "
			+ "?name crime:isUserSetBy module ."
			+ "}";
	static String queryOptionalParamStr = 
			"PREFIX crime: <http://localhost/owl/crime.owl#> " +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			"SELECT  ?name "
			+ "WHERE { "
			+ "module crime:hasParameter ?name . "
			+ "?name crime:isUserSetByOptional module ."
			+ "}";
	static String queryInputsStr = 
			"PREFIX crime: <http://localhost/owl/crime.owl#> " +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"SELECT ?name "
			+ "WHERE { "
			+ "module crime:defines ?name ."
			+ "?name crime:implements crime:top_interface ."
			+ "}";
	static String queryOutputsStr =
			"PREFIX crime: <http://localhost/owl/crime.owl#> " +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"SELECT ?name "
			+ "WHERE { "
			+ "module crime:defines ?name ."
			+ "?name crime:implements crime:bottom_interface ."
			+ "}";
	static String queryPrimitivesStr =
			"PREFIX crime: <http://localhost/owl/crime.owl#> " +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"SELECT ?type ?name "
			+ "WHERE { "
			+ "module crime:defines ?name ."
			+ "?name rdf:type ?type ."
			+ "}";
	static String queryTopInterfaceStr = 
			"PREFIX crime: <http://localhost/owl/crime.owl#> " +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"SELECT ?type "
			+ "WHERE { "
			+ " ?name rdf:type ?type ."
			+ "module crime:defines ?name ."
			+ "?name crime:implements crime:top_interface ."
			+ "}";
	static String queryBottomInterfaceStr =
			"PREFIX crime: <http://localhost/owl/crime.owl#> " +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"SELECT ?type "
			+ "WHERE { "
			+ " ?name rdf:type ?type ."
			+ "module crime:defines ?name ."
			+ "?name crime:implements crime:bottom_interface ."
			+ "}";

	public TripleStore(String ontologyFNm) {

		String sesameServer = "http://localhost:8090/openrdf-sesame";
		String repositoryID = "crime";

		myRepository = new HTTPRepository(sesameServer, repositoryID);
                
		try {
			myRepository.initialize();
			importOntology(ontologyFNm);
		} catch (RepositoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	};
	
	public void getConnection () {
		try {
			con = myRepository.getConnection();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void closeConnection(){
		try {
			con.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createRDFModule(String subject, String object) {
		ValueFactory f = myRepository.getValueFactory();

		// create some resources and literals to make statements out of
		URI subjectURI = f.createURI("http://example.org/crime/" + subject);
		URI objectURI = f.createURI("http://example.org/ontology/" + object);

		try {
			RepositoryConnection con = myRepository.getConnection();
			try {
				// subject is a module
				con.add(subjectURI, RDF.TYPE, objectURI);
			} finally {
				con.close();
			}
		} catch (OpenRDFException e) {
			// handle exception
		}
	}
	
	public void importOntology(String FileName) {
		File file = new File(FileName);
		String baseURI = "http://localhost/owl/crime.owl";

		try {
		   RepositoryConnection con = myRepository.getConnection();
		   try {
		      con.add(file, baseURI, RDFFormat.RDFXML);
		   }
		   finally {
		      con.close();
		   }
		} catch (OpenRDFException e) {
		   // handle exception
		} catch (IOException e) {
		   // handle io exception
		}
	}

	public void writeTriple(String triple) {

		String[] partsV = {"", "", ""};
		partsV[0] = triple.substring(0, triple.indexOf(' '));
		triple = triple.substring(triple.indexOf(' ') + 1);
		partsV[1] = triple.substring(0, triple.indexOf(' '));
		triple = triple.substring(triple.indexOf(' ') + 1);
		partsV[2] = triple;
		String[] typeV = {"", "", ""};

		for (int i = 0; i < 3; i++) {
			if (partsV[i].indexOf("^^") > 0) {
				typeV[i] = "literal";
				partsV[i] = partsV[i].substring(0, partsV[i].indexOf("^^"));
			} else if (partsV[i].indexOf(":") > 0) {
				typeV[i] = "uri";
				String abbr = partsV[i].substring(0, partsV[i].indexOf(':') + 1);
				if (abbr != "") {
					System.out.println(partsV[i]);
					partsV[i] = partsV[i].replaceFirst(abbr,
							RuleParser.getPrefix(abbr));
				}
			} else {
				typeV[i] = "literal";
			}

		}
		
		System.out.println(partsV[0] + " " + partsV[1] + " " + partsV[2]);
		try {
			ValueFactory f = myRepository.getValueFactory();
			if (typeV[0].equals("uri") && typeV[1].equals("uri") && typeV[2].equals("uri")){
				URI subjectURI = f.createURI(partsV[0]);
				URI predicateURI = f.createURI(partsV[1]);
				URI objectURI = f.createURI(partsV[2]);
				con.add(subjectURI, predicateURI, objectURI);
				if (partsV[0].contains("echo_")){
					System.out.println(partsV[0] + " " + partsV[1] + " " + partsV[2]);
				}
			} else if (typeV[0].equals("uri") && typeV[1].equals("uri") && typeV[2].equals("literal")){
				URI subjectURI = f.createURI(partsV[0]);
				URI predicateURI = f.createURI(partsV[1]);
				Literal objectLiteral = f.createLiteral(partsV[2]);
				con.add(subjectURI, predicateURI, objectLiteral);
			}
		} catch (OpenRDFException e) {
			// handle exception
		}
	}
	public JSONObject getCrimeLayersLang() {
		HashMap<String, String> p1 = new HashMap<>();
		p1.put("type", "text");
		p1.put("name", "name");
		p1.put("label", "Stack Name");
		p1.put("typeInvite", "Enter the name of the stack");
		p1.put("cols", "30");
		p1.put("rows", "2");

		HashMap<String, String> p2 = new HashMap<>();
		p2.put("type", "text");
		p2.put("name", "description");
		p2.put("label", "Description");
		p2.put("cols", "30");
		
		HashMap<String, Object> p3 = new HashMap<>();
		p3.put("type", "select");
		p3.put("name", "nodeid");
		p3.put("label", "NodeId");
		JSONArray array = new JSONArray();
		array.put("0.0"); array.put("0.1"); array.put("Auto");
		p3.put("selectValues", array);

		JSONObject crimeLayersLang = new JSONObject();
		try {
			crimeLayersLang.append("languageName","crimeLayers");
			crimeLayersLang.append("propertiesFields", p1);
			crimeLayersLang.append("propertiesFields", p2);
			crimeLayersLang.append("propertiesFields", p3);
			
			ArrayList<HashMap<String, Object>> resList = queryRepository(queryModulesStr);
			String modNm = null;
			for (HashMap<String, Object> map : resList) {
				HashMap<String, Object> containerHm = new HashMap<>();
				containerHm.put("xtype", "LayerContainer");
				JSONObject container = new JSONObject(containerHm);

				modNm = (String) map.get("name");
				container.put("icon", "/images/" + modNm + ".png");
				String tmpQueryContainerStr = queryRequiredParamStr;
				tmpQueryContainerStr = tmpQueryContainerStr.replaceAll("module", "crime:" + modNm);
				resList = queryRepository(tmpQueryContainerStr);
				for (HashMap<String, Object> hmap : resList) {
					HashMap<String, Object> inputParams = new HashMap<>();
					Object obj = hmap.get("name");
					hmap.put("label", obj);
					hmap.put("required", true);
					inputParams.put("inputParams", hmap);
					container.append("fields", inputParams);
				}	
				tmpQueryContainerStr = queryOptionalParamStr;
				tmpQueryContainerStr = tmpQueryContainerStr.replaceAll("module", "crime:" + modNm);
				resList = queryRepository(tmpQueryContainerStr);
				for (HashMap<String, Object> hmap : resList) {
					//HashMap<String, Object> inputParams = new HashMap<>();
					Object obj = hmap.get("name");
					hmap.put("label", obj);
					if (obj.toString().equalsIgnoreCase("time_trigger_flg")) {
						hmap.put("type", "boolean");
					} else {
						hmap.put("required", false);
					}
					//inputParams.put("inputParams", hmap);
					//container.append("fields", inputParams);
					container.append("fields", hmap);
				}
				
				String tmpQueryInputsStr = queryInputsStr;
				tmpQueryInputsStr = tmpQueryInputsStr.replaceAll("module", "<http://localhost/owl/crime.owl#" + modNm + ">");
				resList = queryRepository(tmpQueryInputsStr);
				JSONObject terminals = new JSONObject();
				//for (HashMap<String, Object> hmap : resList) {
				if (resList.size() > 0) {
					HashMap<String, Object> terminal = new HashMap<>();
					terminal.put("name", "_INPUT");
					array = new JSONArray();
					array.put(-1); array.put(0);
					terminal.put("direction", array);
					HashMap<String, Object> offsetPosition = new HashMap<>();
					offsetPosition.put("left", "15");
					offsetPosition.put("top", "-20");
					terminal.put("offsetPosition", offsetPosition);
					HashMap<String, Object> type = new HashMap<>();
					type.put("type", "input");
					JSONObject allowedTypes = new JSONObject(type);
					allowedTypes.append("allowedTypes", "output");
					terminal.put("ddConfig", allowedTypes);
					terminal.put("nMaxWires", "1");
					terminals = new JSONObject(terminal);
					container.append("terminals", terminals);
				}
				terminals = new JSONObject();
				String tmpQueryOutputsStr = queryOutputsStr;
				tmpQueryOutputsStr = tmpQueryOutputsStr.replaceAll("module", "<http://localhost/owl/crime.owl#" + modNm + ">");
				resList = queryRepository(tmpQueryOutputsStr);
				if ((resList.size() > 0) && (!modNm.equalsIgnoreCase("c_channel"))) {
					HashMap<String, Object> terminal = new HashMap<>();
					terminal.put("name", "_OUTPUT");
					array = new JSONArray();
					array.put(1); array.put(0);
					terminal.put("direction", array);
					HashMap<String, Object> offsetPosition = new HashMap<>();
					offsetPosition.put("left", "15");
					offsetPosition.put("bottom", "-20");
					terminal.put("offsetPosition", offsetPosition);
					HashMap<String, Object> type = new HashMap<>();
					type.put("type", "output");
					JSONObject allowedTypes = new JSONObject(type);
					allowedTypes.append("allowedTypes", "input");
					terminal.put("ddConfig", allowedTypes);
					terminals = new JSONObject(terminal);
					container.append("terminals", terminals);
				}
				
				map.put("container", container);
				crimeLayersLang.append("modules", map);
			}
			
			return crimeLayersLang;
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}
	
	public ArrayList<Primitive> getPrimitives(String moduleNm) {
		String tmpQueryString = queryPrimitivesStr.replaceAll("module", "crime:" + moduleNm);
		ArrayList<HashMap<String, Object>> resList = this.queryRepository(tmpQueryString);
		ArrayList<Primitive> primitiveV = new ArrayList<Primitive>();
		for (HashMap<String, Object> hmap : resList) {
			if (!hmap.get("type").toString().equalsIgnoreCase("NamedIndividual")) {
				primitiveV.add(new Primitive((String)hmap.get("type"), (String)hmap.get("name")));
			}
		}
		return primitiveV;
	}
	
	public boolean isRequired (String modNm, String paramNm) {
		String tmpQueryContainerStr = queryRequiredParamStr;
		tmpQueryContainerStr = tmpQueryContainerStr.replaceAll("module", "crime:" + modNm);
		ArrayList<HashMap<String, Object>> resList = queryRepository(tmpQueryContainerStr);
		for (HashMap<String, Object> hmap : resList) {
			String obj = (String) hmap.get("name");
			if (obj.equalsIgnoreCase(paramNm)) return true;
		}
		return false;
	}
	
	public ArrayList<HashMap<String, Object>> queryRepository(String queryStr) {
		ArrayList<HashMap<String, Object>> resList = new ArrayList<>();
		try{
            RepositoryConnection con = myRepository.getConnection();
            try {
                TupleQuery query = 
                    con.prepareTupleQuery(
                    org.openrdf.query.QueryLanguage.SPARQL, queryStr);
                TupleQueryResult qres = query.evaluate();
                while (qres.hasNext()) {
                    BindingSet b = qres.next();
                    Set<String> names = b.getBindingNames();
                    HashMap<String, Object> hm = new HashMap<>();
                    for (Object n : names) {
                    	Value value = b.getValue((String) n);
                    	String strValue = value.stringValue();
                    	if (strValue.indexOf('#') >= 0) {
                    		strValue = strValue.substring(strValue.indexOf('#') + 1);
                    	}
                        hm.put((String) n, strValue);
                    }
                    resList.add(hm);
                }
                return resList;
            } finally {
                con.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
	public void checkConsistency(Stack protocolStack) throws CRimeException {
		int modno = protocolStack.getModuleNo();
		for (int m = 0; m < modno - 1; m++) {
			Module bottom = protocolStack.getModule(m);
			Module top = protocolStack.getModule(m + 1);
			String tmpQueryTopInterfaceStr = queryTopInterfaceStr.replaceAll("module", "crime:" + bottom.getModuleNm());
			ArrayList<HashMap<String, Object>> topInterface = queryRepository(tmpQueryTopInterfaceStr);
			String tmpQueryBottomInterfaceStr = queryBottomInterfaceStr.replaceAll("module", "crime:" + top.getModuleNm());
			ArrayList<HashMap<String, Object>> bottomInterface = queryRepository(tmpQueryBottomInterfaceStr);
			if (!bottomInterface.containsAll(topInterface)) {
				throw new CRimeException(top.getModuleNm() + " is incompatible with "
						+ bottom.getModuleNm() + " please rewire or replace modules!");
			}
		}
	}
}

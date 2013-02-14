package si.sensorlab.crime.config;

import java.util.ArrayList;
import java.util.List;

import si.sensorlab.crime.exceptions.CRimeException;
import si.sensorlab.crime.rdf.TripleStore;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;


public class Settings {
	protected final String stackName;	
	protected final String nodeAddress;		
	protected final List<Stack> protocolStackV;	
	private TripleStore trStore;
			
	public Settings(JSONObject recJSon, TripleStore trStore) throws CRimeException {		
		this.trStore = trStore;
		protocolStackV = new ArrayList<Stack>();
		nodeAddress = "0.0";							
		
		try {
			stackName = recJSon.getJSONObject("params").getString("name");
			String working = recJSon.getJSONObject("params").getString("working");
			JSONObject stackJSon = new JSONObject(working);
			parseJSon(stackJSon);			
		} catch (Exception e) {
		throw new CRimeException("Error parsing JSon document: " + e.getMessage());
		}
	}					
	
	public void parseJSon (JSONObject modulesJSon) throws CRimeException {		
		List<Module> moduleV = new ArrayList<>();
		WireList wireList = new WireList();
		int startModuleId = -1;
		List<Integer> endModulesIdV = new ArrayList<>();
		try {			
			//parse modules
			JSONArray moduleJSonV = modulesJSon.getJSONArray("modules");						
			for (int i = 0; i < moduleJSonV.length(); i++){				
				JSONObject moduleJSon = (JSONObject) moduleJSonV.get(i);
				String moduleNm = moduleJSon.getString("name");				
										
					if (moduleNm.equalsIgnoreCase("c_channel")) {
						endModulesIdV.add(i);
					} else if (moduleNm.contains("_app")) {
						startModuleId = i;
					}
					List<Primitive> primitiveV = trStore.getPrimitives(moduleNm);
								
					JSONObject paramsJSon = moduleJSon.getJSONObject("value");				
					List<Parameter> parameterV = parseParams(moduleNm, paramsJSon);
				
					Module stackModule = new Module(moduleNm, primitiveV, parameterV);	
					moduleV.add(stackModule);						
					}				
			//parse wires
			JSONArray wiresJSonV = modulesJSon.getJSONArray("wires");
			for (int wireIdx = 0; wireIdx < wiresJSonV.length(); wireIdx++){
				wireList.addWire(((JSONObject) wiresJSonV.get(wireIdx)), startModuleId);								
			}		
			ArrayList<Integer> SplitModuleV = wireList.getSplitModuleIdV();
			
			for (int stackId = 0; stackId < endModulesIdV.size(); stackId++){
				Stack protocolStack = new Stack();
				int stackExtracted = 0; int tgtId = 0, srcId = 0;
				tgtId = endModulesIdV.get(stackId);
				
				Module m = null;
				while (stackExtracted == 0) {	
					m = moduleV.get(tgtId);
					protocolStack.addModule(m);
					srcId = wireList.getSrcId(tgtId);
					if (srcId == -1) { throw new CRimeException("Please rewire, direction top-down!"); }
					wireList.remove(tgtId, srcId);
					if ((srcId != -1) && (!SplitModuleV.contains(srcId)) && !wireList.isEmpty()) {
						tgtId = srcId;					
					} else {
						stackExtracted = 1;
						m = moduleV.get(srcId);
						protocolStack.addModule(m);
						SplitModuleV.remove((Integer)srcId);
						List<Integer> childModuleIdV = wireList.removeWireGetChildModuleId(srcId, tgtId);
						setParent(childModuleIdV, moduleV, stackId, protocolStack.getModuleNo() - 1);
					}
				}				
				trStore.checkConsistency(protocolStack);
				protocolStack.setStackName(moduleV.get(moduleV.size() - 2).getModuleNm());
				protocolStackV.add(protocolStack);	
			}
						
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public List<Parameter> parseParams (String modNm, JSONObject paramsJSon) throws CRimeException {						
		List<Parameter> parameterV = new ArrayList<Parameter>();
		String paramsStr = paramsJSon.toString();
		if (paramsStr.equalsIgnoreCase("{}")) {return parameterV;}
		paramsStr = paramsStr.replaceAll("\"", "");
		paramsStr = paramsStr.substring(1, paramsStr.length()-1);
		boolean triggerFlag = false;
		if (paramsStr.indexOf("time_trigger_flg:true") >=0 ){
			triggerFlag = true;
			parameterV.add(new Parameter("time_trigger_flg", "1"));
			paramsStr = paramsStr.replaceAll("time_trigger_flg:true,", "");
		} else if (paramsStr.indexOf("time_trigger_flg:false") >=0 ) {
			parameterV.add(new Parameter("time_trigger_flg", "0"));
			paramsStr = paramsStr.replaceAll("time_trigger_flg:false,", "");
		}
		
		String[] paramV = paramsStr.split(",");		
		for (int i = 0; i < paramV.length; i++){
			String key = paramV[i].substring(0, paramV[i].indexOf(':'));
			String val = paramV[i].substring(paramV[i].indexOf(':') + 1);			
			if (trStore.isRequired(modNm, key) && val.isEmpty()){				
				throw new CRimeException("Please provide all the required parameters: " + key.toUpperCase() + " missing value.");
			}						
			if (triggerFlag && (key.indexOf("trigger") >= 0) && val.isEmpty()) {
				throw new CRimeException("Please provide all the required parameters: " + key.toUpperCase() + " missing value.");
			}
			parameterV.add(new Parameter(key, val));			
		}	
		return parameterV;
	}
	
	public List<Stack> getStackList() {return protocolStackV; }
	
	public void setParent(List<Integer> childModuleIdV, List<Module> moduleV, int stackId, int srcId) {
		for (Integer moduleId : childModuleIdV) {
			moduleV.get(moduleId).setParent(stackId, srcId);
		}
	}
}

/*
 * public void readConfigFile(File xmlFile) throws Exception {		
		// open XML file
		org.w3c.dom.Document xmlDoc = openXml(xmlFile);
		// go over the document and load it into object model
		try {
			// get document ID
			Element topElt = (Element) xmlDoc.getElementsByTagName("node")
					.item(0);
			Element addressElt = (Element) topElt.getElementsByTagName(
					"address").item(0);
			nodeAddress = addressElt.getTextContent();
			// get rest of the document
			parseDoc(xmlDoc);
		} catch (Exception e) {
			throw new Exception("Error loading XML document "
					+ xmlFile.getName(), e);
		}
	}
	
	private org.w3c.dom.Document openXml(File xmlFile) throws Exception {
		// parse XML document
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new Exception("Error creating DocumentBuilder.", e);
		}
		org.w3c.dom.Document xmlDoc;
		try {
			xmlDoc = docBuilder.parse(xmlFile);
		} catch (SAXException e) {
			throw new Exception("Error parsing XML document "
					+ xmlFile.getName(), e);
		} catch (IOException e) {
			throw new Exception("Error reading XML document "
					+ xmlFile.getName(), e);
		}
		return xmlDoc;
	}

	private void parseDoc(org.w3c.dom.Document xmlDoc) throws ParseException,
			Exception {
		Stack protocolStack = new Stack();
		NodeList stackV = xmlDoc.getElementsByTagName("stack");
		for (int stackId = 0; stackId < stackV.getLength(); stackId++) {
			NodeList moduleV = ((Element) stackV.item(stackId))
					.getElementsByTagName("module");
			String moduleNm;
			List<String> primitiveNmV;
			List<Parameter> parameterNmV = new ArrayList<Parameter>();
			for (int moduleId = 0; moduleId < moduleV.getLength(); moduleId++) {
				Element moduleElt = (Element) moduleV.item(moduleId);
				Element nameElt = (Element) moduleElt.getElementsByTagName(
						"name").item(0);
				moduleNm = nameElt.getTextContent();
				primitiveNmV = parsePrimitives((Element) moduleElt
						.getElementsByTagName("primitives").item(0));
				parameterNmV = parseParameters((Element) moduleElt
						.getElementsByTagName("parameters").item(0));
				Module stackModule = new Module(moduleNm, primitiveNmV,
						parameterNmV);
				protocolStack.addModule(stackModule);
			}
			if (((Element) stackV.item(stackId)).getElementsByTagName("module")
					.getLength() > 0) {
				System.out.print(((Element) stackV.item(stackId))
						.getElementsByTagName("module").getLength());
			}
		}
		protocolStackV.add(protocolStack);
	}

	private List<String> parsePrimitives(Element primitivesElt) {
		List<String> primitiveNmV = new ArrayList<String>();
		if (primitivesElt != null) {
			Element primitiveElt = (Element) primitivesElt
					.getElementsByTagName("c_close").item(0);
			if (primitiveElt != null) {
				String primitiveNm = primitiveElt.getTextContent();
				if (primitiveNm != "") {
					primitiveNmV.add(primitiveNm);
				}
			}
			primitiveElt = (Element) primitivesElt.getElementsByTagName(
					"c_open").item(0);
			if (primitiveElt != null) {
				String primitiveNm = primitiveElt.getTextContent();
				if (primitiveNm != "") {
					primitiveNmV.add(primitiveNm);
				}
			}
			primitiveElt = (Element) primitivesElt.getElementsByTagName(
					"c_recv").item(0);
			if (primitiveElt != null) {
				String primitiveNm = primitiveElt.getTextContent();
				if (primitiveNm != "") {
					primitiveNmV.add(primitiveNm);
				}
			}
			primitiveElt = (Element) primitivesElt.getElementsByTagName(
					"c_send").item(0);
			if (primitiveElt != null) {
				String primitiveNm = primitiveElt.getTextContent();
				if (primitiveNm != "") {
					primitiveNmV.add(primitiveNm);
				}
			}
			primitiveElt = (Element) primitivesElt.getElementsByTagName(
					"c_sent").item(0);
			if (primitiveElt != null) {
				String primitiveNm = primitiveElt.getTextContent();
				if (primitiveNm != "") {
					primitiveNmV.add(primitiveNm);
				}
			}
			primitiveElt = (Element) primitivesElt.getElementsByTagName(
					"c_dropped").item(0);
			if (primitiveElt != null) {
				String primitiveNm = primitiveElt.getTextContent();
				if (primitiveNm != "") {
					primitiveNmV.add(primitiveNm);
				}
			}
		}
		return primitiveNmV;
	}

	private List<Parameter> parseParameters(Element parametersElt) {
		List<Parameter> parameterNmV = new ArrayList<Parameter>();
		if (parametersElt != null) {
			Element primitiveElt = (Element) parametersElt
					.getElementsByTagName("channelno").item(0);
			if (primitiveElt != null) {
				String parameterVal = primitiveElt.getTextContent();
				if (parameterVal != "") {
					parameterNmV.add(parameterVal);
				}
			}
			primitiveElt = (Element) parametersElt.getElementsByTagName(
					"sender").item(0);
			if (primitiveElt != null) {
				String parameterVal = primitiveElt.getTextContent();
				if (parameterVal != "") {
					parameterNmV.add(parameterVal);
				}
			}
			primitiveElt = (Element) parametersElt.getElementsByTagName(
					"hdrsize").item(0);
			if (primitiveElt != null) {
				String parameterVal = primitiveElt.getTextContent();
				if (parameterVal != "") {
					parameterNmV.add(parameterVal);
				}
			}
			primitiveElt = (Element) parametersElt.getElementsByTagName(
					"time_trigger_flg").item(0);
			if (primitiveElt != null) {
				String parameterVal = primitiveElt.getTextContent();
				if (parameterVal != "") {
					parameterNmV.add(parameterVal);
				}
			}
			primitiveElt = (Element) parametersElt.getElementsByTagName(
					"trigger_init_flg").item(0);
			if (primitiveElt != null) {
				String parameterVal = primitiveElt.getTextContent();
				if (parameterVal != "") {
					parameterNmV.add(parameterVal);
				}
			}
			primitiveElt = (Element) parametersElt.getElementsByTagName(
					"trigger_interval").item(0);
			if (primitiveElt != null) {
				String parameterVal = primitiveElt.getTextContent();
				if (parameterVal != "") {
					parameterNmV.add(parameterVal);
				}
			}
			primitiveElt = (Element) parametersElt.getElementsByTagName(
					"duplicates").item(0);
			if (primitiveElt != null) {
				String parameterVal = primitiveElt.getTextContent();
				if (parameterVal != "") {
					parameterNmV.add(parameterVal);
				}
			}
			primitiveElt = (Element) parametersElt.getElementsByTagName(
					"hop_no").item(0);
			if (primitiveElt != null) {
				String parameterVal = primitiveElt.getTextContent();
				if (parameterVal != "") {
					parameterNmV.add(parameterVal);
				}
			}
			primitiveElt = (Element) parametersElt.getElementsByTagName(
					"seq_no").item(0);
			if (primitiveElt != null) {
				String parameterVal = primitiveElt.getTextContent();
				if (parameterVal != "") {
					parameterNmV.add(parameterVal);
				}
			}
		}
		return parameterNmV;
	}
 */

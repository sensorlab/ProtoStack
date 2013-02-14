package si.sensorlab.crime.config;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class WireList {		
	private class Wire {
		//private String key;
		private int tgt;
		private int src;	
		public Wire(int tgt, int src){	
			this.src = src;
			this.tgt = tgt;						
		}			
		public int getTgt() { return tgt; }
		public int getSrc() { return src; }
	}

	private List<Wire> wireList;
	
	public WireList(){
		wireList = new ArrayList<Wire>();
	}
	
	public void addWire(JSONObject wire, int startModuleId){
		try {
			JSONObject src = (JSONObject) wire.get("src");		
			String srcStr = src.toString();
			String[] tmpV = srcStr.split(",");
			String[] modIdV = tmpV[0].split(":");		
			int tmpSrc = Integer.parseInt(modIdV[1]);
			String[] type1V = tmpV[1].split(":");
			type1V[1] = type1V[1].replaceAll("\"", "");
			
			JSONObject tgt = (JSONObject) wire.get("tgt");
			String tgtStr = tgt.toString();
			tmpV = tgtStr.split(",");
			modIdV = tmpV[0].split(":");		
			int tmpTgt = Integer.parseInt(modIdV[1]);
			String[] type2V = tmpV[1].split(":");	
			type2V[1] = type2V[1].replaceAll("\"", "");
			
			if ((type1V[1].compareToIgnoreCase("_INPUT") > 0) && 
					(type2V[1].compareToIgnoreCase("_OUTPUT") > 0)) {
				int temp = tmpSrc;
				tmpSrc = tmpTgt;
				tmpTgt = temp;
			}
			
			if (tmpSrc > startModuleId) {
				tmpSrc--; 
			} 
			if (tmpTgt > startModuleId) {tmpTgt--; }
			wireList.add(new Wire(tmpTgt, tmpSrc));								
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}									
	}
	
	public int getSrcId(int tgtId) {
		for(Wire w : wireList){
			if (w.getTgt() == tgtId) {return w.getSrc(); }
		}
		return -1;
	}
	
	public List<Integer> getTgtId(int srcId) {
		List<Integer> tgtIdV = new ArrayList<>();
		for(Wire w : wireList){
			if (w.getSrc() == srcId) { tgtIdV.add(w.getTgt()); }
		}
		return tgtIdV;
	}
	
	public void remove(int tgtId, int srcId){
		Wire tmpWire = null;
		for(Wire w : wireList){
			if ((w.getTgt() == tgtId) && (w.getSrc() == srcId)) { tmpWire = w; }
		}
		if (tmpWire != null) {
			wireList.remove(tmpWire);
		}
	}
	
	public List<Integer> removeWireGetChildModuleId(int srcId, int tgtId){
		ArrayList<Wire> tmpWire = new ArrayList<>();
		List<Integer> childModuleIdV = new ArrayList<>();
		for(Wire w : wireList){
			if ((w.getSrc() == srcId)) { 
				tmpWire.add(w);
				if (w.getTgt() != tgtId) {					
					childModuleIdV.add(w.getTgt());
				}		
			}
		}		
		for (Wire w : tmpWire) {
			wireList.remove(w);
		}		
		return childModuleIdV;
	}
	
	public ArrayList<Integer> getSplitModuleIdV() {
		ArrayList<Integer> SplitModuleV = new ArrayList<>();
		for (Wire w1 : wireList) {
			for (Wire w2 : wireList) {
				if ((w1 != w2) && (w1.src == w2.src) && (!SplitModuleV.contains(w1.src))) {
					SplitModuleV.add(w1.src);
				}
			}
		}		
		return SplitModuleV;
	}
	
	public boolean isEmpty(){
		if (wireList.size() > 0) {
			return false;
		} else {return true;}
	}
}

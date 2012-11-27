package si.sensorlab.crime.config;

import java.util.ArrayList;
import java.util.List;

public class PrimitiveList {
	private List<String> primitiveList;
	
	public PrimitiveList(){
		primitiveList = new ArrayList<String>();
	}
	
	public void addPrimitive(String primitiveNm){
		primitiveList.add(primitiveNm);
	}
	public String getPrimitive(int idx){
		return primitiveList.get(idx);
	}
	public int getPrimitiveNo(){
		return primitiveList.size();
	}
}

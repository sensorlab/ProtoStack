package si.sensorlab.crime.config;

import java.util.List;

public class Module {
	private String moduleNm;	
	private int parentStackId;
	private int parentModuleId;
	private List<Primitive> primitiveV;
	private List<Parameter> parameterV;
	
	public Module(String name, List<Primitive> primitiveV, List<Parameter> parameterV){		
		moduleNm = name;
		parentStackId = -1;
		parentModuleId = -1;
		this.primitiveV = primitiveV;
		this.parameterV = parameterV;
	}

	public void setParent(int parentStackId, int parentModuleId) {
		this.parentStackId = parentStackId;
		this.parentModuleId = parentModuleId;
	}
	public String getModuleNm(){
		return moduleNm;
	}
	public int getParentStackId(){
		return parentStackId;
	}
	public int getParentModuleId(){
		return parentModuleId;
	}
	public String getParentId(){
		return moduleNm;
	}
	public int getPrimitiveNo(){
		return primitiveV.size();
	}
	public int getParameterNo(){
		return parameterV.size();
	}
	public String getPrimitiveKey(int prIdx){
		return primitiveV.get(prIdx).getKey();
	}
	public String getPrimitiveVal(int prIdx){
		return primitiveV.get(prIdx).getVal();
	}
	public String getParameterKey(int prIdx){
		return parameterV.get(prIdx).getKey();
	}
	public String getParameterVal(int prIdx){
		return parameterV.get(prIdx).getVal();
	}	
	
	List<Primitive> getPrimitives() {return primitiveV; }		
}

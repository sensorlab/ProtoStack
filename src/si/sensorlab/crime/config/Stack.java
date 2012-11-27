package si.sensorlab.crime.config;

import java.util.ArrayList;
import java.util.List;

public class Stack {
	protected String stackNm;
	protected int channelNo;
	protected List<Module> moduleV;
	
	public Stack(){
		moduleV = new ArrayList<Module>();
	}
	public void addModule(Module module){
		moduleV.add(module);		
	}
	public void setStackName(String stackNm){
		this.stackNm = stackNm;
	}
	public void setChannelNo(int channelNo){
		this.channelNo = channelNo;
	}
	public int getModuleNo(){
		return moduleV.size();
	}
	public Module getModule(int moduleIdx){
		return moduleV.get(moduleIdx);		
	}
	public String getStackNm(){
		return stackNm;		
	}
	public int getChannelNo(){
		return channelNo;		
	}
}

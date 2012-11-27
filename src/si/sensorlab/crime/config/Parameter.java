package si.sensorlab.crime.config;

public class Parameter {
	private String key;
	private String val;
	public Parameter(String key, String val){
		this.key = key; this.val = val;
	}	
	public String getKey() { return key; }
	public String getVal() { return val; }
}

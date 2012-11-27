package si.sensorlab.crime.rdf;

public class Prefix {	
	private String key;
	private String val;
	public Prefix(String _key, String _val){
		key = _key; val = _val;
	}
	public Prefix(String lnStr) {
		String left = lnStr.substring(0, lnStr.indexOf(' '));
		//left = left.replace(":", "");
		String right = lnStr.substring(lnStr.indexOf(' '));
		right = right.substring(2, right.length() - 3);
		key = left; val = right;
	}
	public String getKey() { return key; }
	public String getVal() { return val; }
}

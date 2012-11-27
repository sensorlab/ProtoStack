package si.sensorlab.crime.exceptions;

public class CRimeException extends Exception {
	private static final long serialVersionUID = 1L;
	String message = "";
	public CRimeException(String exceptionString){
		message = exceptionString;
		System.out.println(exceptionString);
	}
	public String getMessage() {
		return message;
	}
}

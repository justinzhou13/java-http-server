package edu.upenn.cis.cis455;

public enum HttpRequestMethod {
	GET ("GET"),
	POST ("POST"),
	PUT ("PUT"),
	HEAD ("HEAD"),
	OPTIONS ("OPTIONS"),
	DELETE ("DELETE"),
	TRACE ("TRACE");
	
	private final String value;
	
	HttpRequestMethod(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
}

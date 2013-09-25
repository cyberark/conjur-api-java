package net.conjur.api.authn;

public class Token {
	private String data;
	private String signature;
	private String rawTimestamp;
	private String key;
	
	public String getData() {
		return data;
	}
	public String getSignature() {
		return signature;
	}
	public String getRawTimestamp() {
		return rawTimestamp;
	}
	public String getKey() {
		return key;
	}
	
	
}

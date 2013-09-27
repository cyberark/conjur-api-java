package net.conjur.api.authn;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.auth.AUTH;
import org.apache.http.message.BasicHeader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Represents a Conjur API token.
 * 
 * This implementation doesn't support creation of tokens directly, only deserialization
 * from JSON.  This is because we don't include implementations of signature and timestamping
 * algorithms.
 */
public class Token {

	// no-arg constructor to make Gson happy
	private Token(){}
	
	// can't make these final, Gson needs to write them after constructing
	// object.
	private String data;
	private String signature;
	private String key;
	private String timestamp;
	
	public String getData() {
		return data;
	}

	/**
	 * Alias for getData to clarify that the data is the username.
	 * @return the Conjur username for this token
	 */
	public String getUsername(){
		return getData();
	}
	
	public String getSignature() {
		return signature;
	}

	public String getKey() {
		return key;
	}

	public String getTimestamp() {
		return timestamp;
	}
	
	public String toJson(){
		return new Gson().toJson(this);
	}
	
	public byte[] toJsonBytes(){
		return toJson().getBytes();
	}
	
	public byte[] toBase64(){
		// the magic formula seems to be unchunked (obviously, since its going
		// in a header), and url unsafe (so that it's padded -- conjur services exepect this)
		return Base64.encodeBase64(toJsonBytes(), false, false);
	}
	
	public String toBase64String(){
		return new String(toBase64());
	}
	
	public String toHeader(){
		return "Token token=\"" + toBase64String() + "\"";
	}
	
	public Header toHttpHeader(String name){
		return new BasicHeader(name, toHeader());
	}
	
	public Header toHttpHeader(){
		return toHttpHeader(AUTH.WWW_AUTH_RESP);
	}
	
	public static Token fromBase64(String base64){
		return fromJson(Base64.decodeBase64(base64));
	}
	
	public static Token fromBase64(byte[] base64){
		return fromJson(Base64.decodeBase64(base64));
	}
	
	public static Token fromJson(JsonElement json){
		return new Gson().fromJson(json, Token.class);
	}
	
	public static Token fromJson(String json){
		return new Gson().fromJson(json, Token.class);
	}
	
	public static Token fromJson(byte[] bytes){
		return fromJson(new String(bytes));
	}
	
}

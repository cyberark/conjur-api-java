package net.conjur.api.authn;

import net.conjur.util.TextUtils;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.auth.AUTH;
import org.apache.http.message.BasicHeader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.http.util.CharsetUtils;

import java.nio.charset.Charset;

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

	public String toBase64(){
		return Base64.encodeBase64URLSafeString(toJson().getBytes());
	}

	public String toHeaderValue(){
        return new StringBuilder()
                .append("Token token=\"")
                .append(toBase64())
                .append("\"").toString();
	}
	
	public Header toHeader(String name){
		return new BasicHeader(name, toHeaderValue());
	}
	
	public Header toHeader(){
		return toHeader(AUTH.WWW_AUTH_RESP);
	}
}

package net.conjur.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AUTH;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BufferedHeader;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EncodingUtils;

public class HttpHelpers {
	public static final String DEFAULT_CHARSET = "UTF-8";
	
	// Yay generate orthogonal methods!
	public static Header basicAuthHeader(String username, String password, boolean isProxy){
		return basicAuthHeader(username, password, DEFAULT_CHARSET, isProxy);
	}
	
	public static Header basicAuthHeader(String username, String password, String charset){
		return basicAuthHeader(username, password, charset, false);
	}
	
	public static Header basicAuthHeader(String username, String password){
		return basicAuthHeader(username, password, DEFAULT_CHARSET, false);
	}
	
	public static Header basicAuthHeader(String username){
		return basicAuthHeader(username, null);
	}
	
	// HttpClient provides facilities to do this, but they're rather inconvenient, so we reproduce 
	// them here.
	/**
	 * Create a Basic Auth header for the given credentials and charset.
	 * @param username Basic auth username, must not be <code>null</code>
	 * @param password Basic auth password, may be <code>null</code>
	 * @param charset Charset for user/password, uses UTF-8 if null.
	 * @param isProxy Whether this is a proxy auth header
	 * @return A {@link Header} providing basic authentication
	 */
	public static Header basicAuthHeader(
			final String username, 
			final String password, 
			final String charset, 
			final boolean isProxy){
		
		Args.notNull(username, "Username");
		
		final StringBuilder tmp = new StringBuilder();
		
		tmp.append(username)
		   .append(":")
		   .append(password == null ? "null" : password);
		

        final byte[] base64password = Base64.encodeBase64(
                EncodingUtils.getBytes(tmp.toString(), charset == null ? DEFAULT_CHARSET : charset), false);
        
        final CharArrayBuffer buffer = new CharArrayBuffer(32);
        buffer.append(isProxy ? AUTH.PROXY_AUTH_RESP : AUTH.WWW_AUTH_RESP); 
        buffer.append(": Basic ");
        buffer.append(base64password, 0, base64password.length);
        
        return new BufferedHeader(buffer);
	}
	
	/**
	 * Create a text/plain {@link HttpEntity}
	 * @param content The content to send
	 * @return An {@link HttpEntity} with content type text/plain and the given content.
	 */
	public static HttpEntity stringEntity(String content){
		return new StringEntity(content, ContentType.TEXT_PLAIN);
	}
}

package net.conjur.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AUTH;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.apache.http.message.BufferedHeader;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EncodingUtils;

import java.util.Locale;

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
     * Get a reason phrase for the given status code.  If a bogus status code is given,
     * return a String like "Bogus HTTP status code: -53".
     * @param statusCode An HTTP status code
     * @return A reason phrase
     */
    public static String getReasonPhrase(int statusCode){
        String reason = null;
        if(statusCode >= 100 && statusCode < 600){
            reason = EnglishReasonPhraseCatalog.INSTANCE.getReason(statusCode, Locale.ENGLISH);
        }

        if(reason == null){
            reason = String.format(UNKNOWN_STATUS_CODE_MESSAGE, statusCode);
        }

        return reason;
    }

    private static final String UNKNOWN_STATUS_CODE_MESSAGE = "Bogus HTTP status code: %d";
}

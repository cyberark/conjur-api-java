package net.conjur.api.authn;

import net.conjur.util.JsonSupport;
import net.conjur.util.TextUtils;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.nio.charset.Charset;

/**
 * Represents a Conjur API authentication token.
 */
public class Token {
    public static final int DEFAULT_LIFESPAN_SECONDS = 8 * 60;
    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            // tokens use dates like 2013-10-01 18:48:32 UTC
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss ZZZ");

	private String data;
	private String signature;
	private String key;
    @JsonProperty("timestamp")
	private String rawTimestamp;
    @JsonProperty("expiration") // TODO get this to conform to api
    private String rawExpiration;

    @JsonIgnore private DateTime timestamp;
    @JsonIgnore private DateTime expiration;

    public String getData() {
		return data;
	}
	
	public String getSignature() {
		return signature;
	}

	public String getKey() {
		return key;
	}

    @JsonIgnore
	public DateTime getTimestamp() {
		if(timestamp == null){
            parseTimestamps();
        }
        return timestamp;
	}

    @JsonIgnore
    public DateTime getExpiration(){
        // check timestamp, because expiration can be null after we parse the timestamps
        // if the token didn't include it.
        if(timestamp == null){
            parseTimestamps();
        }
        if(expiration == null){
            expiration = timestamp.plusSeconds(DEFAULT_LIFESPAN_SECONDS);
        }
        return expiration;
    }

    public boolean willExpireWithin(int seconds){
        return DateTime.now().plusSeconds(seconds).isAfter(getExpiration());
    }

    @JsonIgnore
    public boolean isExpired(){
        return willExpireWithin(0);
    }


    public String toString(){
        return toJson();
    }

	public String toJson(){
		return JsonSupport.toJson(this);
	}

    public static Token fromJson(String json){
        return JsonSupport.fromJson(json, Token.class);
    }

	public String toBase64(){
        // NB url safe mode *does not* work
		return Base64.encodeBase64String(toJson().getBytes());
	}

	public String toHeader(){
        return new StringBuilder()
                .append("Token authenticate=\"")
                .append(toBase64())
                .append("\"").toString();
	}

    private void parseTimestamps(){
        if(rawTimestamp  != null){
            timestamp = DATE_TIME_FORMATTER.parseDateTime(rawTimestamp);
        }
        if(rawExpiration != null){
            expiration = DATE_TIME_FORMATTER.parseDateTime(rawExpiration);
        }
    }

}

package net.conjur.api.authn;

import net.conjur.util.JsonSupport;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Represents a Conjur API authentication token.
 */
public class Token {
    public static final int DEFAULT_LIFESPAN_SECONDS = 8 * 60;
    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            // tokens use dates like 2013-10-01 18:48:32 UTC
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss ZZZ");

    // Hold our fields in here to facilitate json serialization/deserialization
    private static class Fields {
        public String data;
        public String signature;
        public String key;
        public String timestamp;
        public String expiration;
    }

    private Fields fields;
    private final String json;
    private DateTime timestamp;
    private DateTime expiration;

    Token(String json){
        this.json = json;
    }

    private Fields fields(){
        if(fields == null){
            fields = JsonSupport.fromJson(json, Fields.class);
        }
        return fields;
    }

    public String getData() {
		return fields().data;
	}
	
	public String getSignature() {
		return fields().signature;
	}

	public String getKey() {
		return fields().key;
	}

    public DateTime getTimestamp(){
        if(timestamp == null){
            timestamp = DATE_TIME_FORMATTER.parseDateTime(fields().timestamp);
        }
        return timestamp;
    }

    public DateTime getExpiration(){
        if(expiration == null){
            if(fields().expiration == null){
                expiration = getTimestamp().plusSeconds(DEFAULT_LIFESPAN_SECONDS);
            }else{
                expiration = DATE_TIME_FORMATTER.parseDateTime(fields().expiration);
            }
        }
        return expiration;
    }

    public boolean willExpireWithin(int seconds){
        return DateTime.now().plusSeconds(seconds).isAfter(getExpiration());
    }

    public boolean isExpired(){
        return willExpireWithin(0);
    }


    public String toString(){
        return toJson();
    }

	public String toJson(){
		return json;
	}

    public static Token fromJson(String json){
        return new Token(json);
    }

	public String toBase64(){
        // NB url safe mode *does not* work
		return Base64.encodeBase64String(toJson().getBytes());
	}

	public String toHeader(){
        return new StringBuilder()
                .append("Token token=\"")
                .append(toBase64())
                .append("\"").toString();
	}
}

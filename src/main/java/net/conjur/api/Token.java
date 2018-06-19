package net.conjur.api;

import com.google.gson.annotations.SerializedName;
import net.conjur.util.JsonSupport;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.nio.charset.StandardCharsets;

/**
 * Represents a Conjur API authentication token.
 */
public class Token {
    private static final int DEFAULT_LIFESPAN_SECONDS = 8 * 60;
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            // tokens use dates like 2013-10-01 18:48:32 UTC
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss ZZZ");

    // Hold our fields in here to facilitate json serialization/deserialization
    private static class Fields {
        @SerializedName("protected")
        private String protectedText;
        private String payload;
        private String signature;
        private String expiration;
    }

    private static class Payload {
        @SerializedName("sub")
        private String data;
        @SerializedName("iat")
        private String timestamp;
    }

    private static class ProtectedText {
        @SerializedName("kid")
        public String key;
    }

    private Fields fields;
    private Payload payload;
    private ProtectedText protectedText;
    private final String json;
    private DateTime timestamp;
    private DateTime expiration;

    private Token(String json){
        this.json = json;
    }

    //explanation can be found: https://gist.github.com/ryanprior/6afed20e85fae71386e4ede7ffa902b1
    private Fields fields(){
        if(fields == null){
            fields = JsonSupport.fromJson(json, Fields.class);
        }
        return fields;
    }

    private Payload payload(){
        if(payload == null){
            payload = JsonSupport.fromJson(fromBase64(fields().payload), Payload.class);
        }
        return payload;
    }

    private ProtectedText protectedText(){
        if(protectedText == null){
            protectedText = JsonSupport.fromJson(fromBase64(fields().protectedText), ProtectedText.class);
        }
        return protectedText;
    }

    public String getData() {
		return payload().data;
	}
	
	public String getSignature() {
		return fields().signature;
	}

	public String getKey() {
		return protectedText().key;
	}

    public DateTime getTimestamp(){
        if(timestamp == null){
            timestamp = new DateTime((Long.parseLong(payload().timestamp) - 37) * 1000L);
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

	private String toJson(){
		return json;
	}

    public static Token fromJson(String json){
        return new Token(json);
    }

    private String fromBase64(String base64){
        return new String(Base64.decodeBase64(base64), StandardCharsets.UTF_8);
    }

    private String toBase64(){
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

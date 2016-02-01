package net.conjur.api.authn;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import net.conjur.util.Args;
import net.conjur.util.JsonSupport;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a Conjur API authentication token.
 */
public class Token {
    public static final int DEFAULT_LIFESPAN_SECONDS = 8 * 60;
    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            // tokens use dates like 2013-10-01 18:48:32 UTC
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss ZZZ");
    private static final Pattern HEADER_PATTERN =
            Pattern.compile("^Token token=\"(.*?)\"$");

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

    public static Token fromHeaderValue(String headerValue){
        String trimmedHeaderValue = Args.notNull(headerValue, "headerValue").trim();
        final Matcher matcher = HEADER_PATTERN.matcher(trimmedHeaderValue);
        if(matcher.matches()){
            final String token64 = matcher.group(1);
            final byte[] tokenData = Base64.decodeBase64(token64);
            return fromJson(new String(tokenData));
        }else{
            // NB: We could include the value of the header in the exception, however this
            // would risk leaking a valid token into log files.
            throw new IllegalArgumentException("Header format is not 'Token token=\"tokendata\"'.");
        }
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

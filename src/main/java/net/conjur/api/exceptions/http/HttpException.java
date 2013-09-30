package net.conjur.api.exceptions.http;

import net.conjur.api.exceptions.ConjurApiException;

import net.conjur.util.HttpHelpers;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;

import java.lang.reflect.InvocationTargetException;

/**
 * Thrown when an HTTP request returns an error (>= 400) status.  Some specific error codes
 * (404, 401, etc.) throw exceptions found in the net.conjur.api.exceptions.http package,
 * which are all subclasses of this exception.
 */
@SuppressWarnings("serial")
public class HttpException extends ConjurApiException {
	
	private final int statusCode;
    private Throwable cause;
    private String statusText;
    private String message;

    public HttpException(int statusCode){
        this.statusCode = statusCode;
    }

    public HttpException(int statusCode, Throwable cause){
        this.cause = cause;
        this.statusCode = statusCode;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    void setCause(Throwable cause){
        this.cause = cause;
    }

    /**
	 * @return The HTTP status code that caused this exception to be raised.
	 */
	public int getStatusCode() {
		return statusCode;
	}

    public String getStatusText(){
        if(statusText == null)
            statusText = getStatusText(getStatusCode());
        return statusText;
    }

    public static HttpException fromStatusCode(int statusCode){
        switch (statusCode){
            case BadRequestException.STATUS_CODE: return new BadRequestException();
            case UnauthorizedException.STATUS_CODE: return new UnauthorizedException();
            case ForbiddenException.STATUS_CODE: return new ForbiddenException();
            case NotFoundException.STATUS_CODE: return new NotFoundException();
            case ConflictException.STATUS_CODE: return new ConflictException();
            case InternalServerErrorException.STATUS_CODE: return new InternalServerErrorException();
            default: return new HttpException(statusCode);
        }
    }

    public static HttpException fromHttpResponseException(HttpResponseException e){
        HttpException ex = fromStatusCode(e.getStatusCode());
        ex.setCause(e);
        return ex;
    }



	public static void throwErrors(HttpResponse response) throws HttpException{
		int code = response.getStatusLine().getStatusCode();
		
		if(code < 400)
			return;

		throw fromStatusCode(code);
	}
	
	@Override
	public String getMessage() {
		if(message == null){
			message = getStatusText() + " " + getStatusCode();
		}
		return message;
	}

    private static String getStatusText(int statusCode){
        try{
            return HttpHelpers.getReasonPhrase(statusCode);
        }catch(Throwable e){
            // Don't throw anything here, because this is called by toString() when
            // we're trying to print an exception!
            System.err.format("Couldn't get a reason phrase for %d!\n", statusCode);
            e.printStackTrace();
            return "Error!";
        }
    }
}

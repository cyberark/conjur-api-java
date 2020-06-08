
package com.cyberark.conjur.util.rs;

import com.google.gson.Gson;
import edu.emory.mathcs.backport.java.util.Collections;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * Can be registered (or provided during the JAXRS scanning phase) to process Json responses.
 */
@Provider
public class JsonBodyReader implements MessageBodyReader<Object> {
    private final Set<Class<?>> readableClasses =
            (Set<Class<?>>) Collections.synchronizedSet(new HashSet<Class<?>>());

    public JsonBodyReader registerClass(Class<?> klass){
        readableClasses.add(klass);
        return this;
    }

    public boolean isReadable(Class<?> klass,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        return MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)
                && isReadable(klass);
    }

    public Object readFrom(Class<Object> klass,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType,
                            MultivaluedMap<String, String> httpHeaders,
                            InputStream bodyInputStream) throws IOException, WebApplicationException {
        final Reader reader = new BufferedReader(new InputStreamReader(bodyInputStream));
        return new Gson().fromJson(reader, klass);
    }

    /**
     * Check if the class is registered or a JsonReadable element is in Annotations.  Register the class if such an
     * annotation is found.
     * @param klass the class we're interested in deserializing
     * @return whether we should can read this class.
     */
    private boolean isReadable(Class<?> klass){
        if(readableClasses.contains(klass)) return true;
        if(klass.getAnnotation(JsonReadable.class) != null){
            readableClasses.add(klass);
            return true;
        }
        return false;
    }


}

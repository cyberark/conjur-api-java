package net.conjur.api;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JacksonInject;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;

import static net.conjur.util.EncodeUriComponent.encodeUriComponent;

/**
 *
 */
public class Variable extends Resource {
    public static class NeedShowPermission extends RuntimeException {
        private static final String MESSAGE_FORMAT =
                "The attributes for variable '%s' are not available without 'show' permission on the variable.  " +
                        "You may still be able to read or write the variable's value.";

        NeedShowPermission(String variableId){
            super(messageFor(variableId));
        }

        NeedShowPermission(String variableId, Throwable cause){
            super(messageFor(variableId), cause);
        }

        private static String messageFor(String variableId){
            return String.format(MESSAGE_FORMAT, variableId);
        }
    }


    public static class Attributes {
        // redundant annotations are included to clarify which properties
        // are from json
        @JsonProperty("id")
        private String id;

        @JsonProperty("mime_type")
        private String mimeType;

        @JsonProperty("kind")
        private String kind;

        @JsonProperty("version_count")
        private Integer versionCount;

        @JsonProperty("ownerid")
        private String ownerId;

        @JsonProperty("userid")
        private String userId;

        @JsonProperty("resource_identifier")
        private String resourceIdentifier;

        public String getId() {
            return id;
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getKind() {
            return kind;
        }

        public Integer getVersionCount() {
            return versionCount;
        }

        public String getOwnerId() {
            return ownerId;
        }

        public String getUserId() {
            return userId;
        }

        public String getResourceIdentifier() {
            return resourceIdentifier;
        }

        @Override
        public String toString() {
            return "Attributes{" +
                    "id='" + id + '\'' +
                    ", mimeType='" + mimeType + '\'' +
                    ", kind='" + kind + '\'' +
                    ", versionCount=" + versionCount +
                    ", ownerId='" + ownerId + '\'' +
                    ", userId='" + userId + '\'' +
                    ", resourceIdentifier='" + resourceIdentifier + '\'' +
                    '}';
        }

        Attributes(){
        }
    }



    private WebTarget target;

    private boolean invalidated = false;

    private String id;

    private Attributes attributes;

    // constructor injects a Resource from which we can initialize our client, auth providers, etc.
    @JsonCreator
    Variable(
            @JacksonInject final Resource resource,
            @JsonProperty("id") final String id){
        super(resource);
        this.id = id;
        buildTargets();
    }

    public Attributes getAttributes(){
        if(attributes == null){
            fetchAttributes();
        }
        return attributes;
    }

    public String getId(){
        return id;
    }

    public int getVersionCount(){
        return getAttributes().getVersionCount();
    }

    public String getMimeType() {
        return getAttributes().getMimeType();
    }

    public String getKind() {
        return getAttributes().getKind();
    }

    public String getOwnerId() {
        return getAttributes().getOwnerId();
    }

    public String getUserId() {
        return getAttributes().getUserId();
    }

    public String getResourceIdentifier() {
        return getAttributes().getResourceIdentifier();
    }

    public <T> T getValue(Class<T> type){
        return target.path("value").request().get(type);
    }

    public <T> T getValue(int version, Class<T> type){
        return target.path("value").queryParam("version", String.valueOf(version))
                .request().get(type);
    }

    public String getValue(){
        return getValue(String.class);
    }

    public String getValue(int version){
        return getValue(version, String.class);
    }

    public String addValue(String value){
        attributes = null;
        Form form = new Form("value", value);
        return target.path("values").request().post(Entity.form(form), String.class);
    }

    public boolean exists(){
        try{
            fetchAttributes();
            return true;
        }catch(NotFoundException e){
            return false;
        }catch(ForbiddenException e){
            return true;
        }
    }

    private void fetchAttributes(){
        try{
            attributes = target.request(MediaType.APPLICATION_JSON_TYPE).get(Attributes.class);
        }catch(ForbiddenException e){
            throw new NeedShowPermission(getId(), e);
        }
    }

    private void buildTargets(){
        target = target(getEndpoints().getDirectoryUri()).path("variables").path(encodeUriComponent(id));
    }

    @Override
    public String toString() {
        final String attributesString = attributes == null ?
                "<not fetched>" : attributes.toString();
        return "Variable{attrs=" + attributesString + ", id='" + id + '\'' + '}';
    }
}

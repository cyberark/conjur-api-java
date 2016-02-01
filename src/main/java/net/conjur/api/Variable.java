package net.conjur.api;

import com.google.gson.annotations.SerializedName;
import net.conjur.util.rs.JsonReadable;

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
@JsonReadable
public class Variable extends RestResource {
    public static class NeedShowPermission extends RuntimeException {
        private static final String MESSAGE_FORMAT =
                "The attributes for variable '%s' are not available without 'read' permission on the variable.  " +
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


    @JsonReadable
    public static class Attributes {
        private String id;

        @SerializedName("mime_type")
        private String mimeType;

        private String kind;

        @SerializedName("version_count")
        private Integer versionCount;

        @SerializedName("ownerid")
        private String ownerId;

        @SerializedName("userid")
        private String userId;

        @SerializedName("resource_identifier")
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

    @SerializedName("id")
    private String id;

    private Attributes attributes;

    Variable(final RestResource restResource, final String id){
        super(restResource);
        this.id = id;
        buildTargets();
    }

    Variable(){
        /** deserializing ctor */
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
        return target.path("value").request("text/plain").get(type);
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
        return getTarget().path("values").request().post(Entity.form(form), String.class);
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
            attributes = getTarget().request(MediaType.APPLICATION_JSON_TYPE).get(Attributes.class);
        }catch(ForbiddenException e){
            throw new NeedShowPermission(getId(), e);
        }
    }

    private WebTarget getTarget(){
        if(target == null){
            buildTargets();
        }
        return target;
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

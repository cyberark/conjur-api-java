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
    // redundant annotations are included to clarify which properties
    // are from json
    @JsonProperty("id")
    private String id;

    @JsonProperty("mime_type")
    private String mimeType;

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("version_count")
    private int versionCount;

    @JsonProperty("ownerid")
    private String ownerId;

    @JsonProperty("userid")
    private String userId;

    @JsonProperty("resource_identifier")
    private String resourceIdentifier;

    private WebTarget target;

    private boolean invalidated = false;


    // constructor injects a Resource from which we can initialize our client, auth providers, etc.
    @JsonCreator
    Variable(
            @JacksonInject final Resource resource,
            @JsonProperty("id") final String id){
        super(resource);
        this.id = id;
        buildTargets();
    }

    public String getId(){
        return id;
    }

    public int getVersionCount(){
        if(invalidated)
            update();
        return versionCount;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getKind() {
        return kind;
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

    public <T> T getValue(Class<T> type){
        return target.path("value").request(mimeType).get(type);
    }

    public <T> T getValue(int version, Class<T> type){
        return target.path("value").queryParam("version", String.valueOf(version))
                .request(mimeType).get(type);
    }

    public String getValue(){
        return getValue(String.class);
    }

    public String getValue(int version){
        return getValue(version, String.class);
    }

    public String addValue(String value){
        invalidated = true;
        Form form = new Form("value", value);
        return target.path("values").request().post(Entity.form(form), String.class);
    }

    public boolean exists(){
        try{
            update();
            return true;
        }catch(NotFoundException e){
            return false;
        }catch(ForbiddenException e){
            return true;
        }
    }

    Variable update(){
        final Variable v = target.request(MediaType.APPLICATION_JSON_TYPE).get(Variable.class);
        id = v.id;
        versionCount = v.versionCount;
        mimeType = v.mimeType;
        kind = v.kind;
        ownerId = v.ownerId;
        userId = v.userId;
        resourceIdentifier = v.resourceIdentifier;
        invalidated = false;
        return this;
    }

    private void buildTargets(){
        target = target(getEndpoints().getDirectoryUri()).path("variables").path(encodeUriComponent(id));
    }

    @Override
    public String toString() {
        return "Variable{" +
                "resourceIdentifier='" + resourceIdentifier + '\'' +
                ", userId='" + userId + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", versionCount=" + versionCount +
                ", kind='" + kind + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}

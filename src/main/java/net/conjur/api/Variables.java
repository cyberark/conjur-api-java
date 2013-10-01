package net.conjur.api;


import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;

/**
 *
 */
public class Variables  extends Resource {
    private WebTarget variables;

    public Variables(Resource relative) {
        super(relative);
    }

    public Variable get(String id){
        return new Variable(this, id).update();
    }

    public boolean exists(String id){
        return new Variable(this, id).exists();
    }

    public Variable create(String kind, String mimeType, String id){
        return create(new Form("kind", kind).param("mime_type", mimeType).param("id", id));
    }

    public Variable create(String kind, String mimeType){
        return create(new Form("kind", kind).param("mime_type", mimeType));
    }

    public Variable create(String kind){
        return create(kind, "text/plain");
    }

    public String createId(){
        return create("unique-id").getId();
    }

    private void init(){
        variables = target(getEndpoints().getDirectoryUri()).path("variables");
    }

    private Variable create(Form params){
        return variables.request("application/json").post(Entity.form(params), Variable.class);
    }

    private WebTarget variable(String id){
        return variables.path(id);
    }
}

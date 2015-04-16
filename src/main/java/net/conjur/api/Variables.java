package net.conjur.api;


import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;

import static net.conjur.util.EncodeUriComponent.encodeUriComponent;

/**
 *
 */
public class Variables  extends Resource {
    private WebTarget variables;

    Variables(Resource relative) {
        super(relative);
        init();
    }

    public Variable get(String id){
        // NB: you need show permission to call update, but not to fetch the variable value.
        // Since a common use case is for a minimally provisioned identity to be able to
        // only fetch the value, we can't call update here.
        return new Variable(this, id);
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
        return variables.path(encodeUriComponent(id));
    }
}

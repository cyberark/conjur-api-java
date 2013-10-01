package net.conjur.api;


import org.codehaus.jackson.annotate.JsonProperty;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlElement;

public class User {
    /*package*/ User(){}

    /*
     * Users#create returns json like
     * {
     "login": "gft5g0-alice",
     "userid": "admin",
     "ownerid": "ci:user:gft5g0-alice",
     "uidnumber": 2494,
     "roleid": "ci:user:gft5g0-alice",
     "resource_identifier": "ci:user:gft5g0-alice",
     "api_key": "320mpt8tqfjgsfecsvngks0x31hgv4zv1yea1tc29svf1pfc0vbc"
     }
     */
    private String login;
    @JsonProperty("userid")
    private String userId;
    @JsonProperty("ownerid")
    private String ownerId;
    @JsonProperty("uidnumber")
    private int uidNumber;
    @JsonProperty("roleid")
    private String roleId;
    @JsonProperty("resource_identifier")
    private String resourceIdentifier;
    @JsonProperty("api_key")
    private String apiKey;

    public String getUserId() {
        return userId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public int getUidNumber() {
        return uidNumber;
    }

    public String getRoleId() {
        return roleId;
    }

    public String getResourceIdentifier() {
        return resourceIdentifier;
    }

    public String getLogin() {
        return login;
    }

    public String getApiKey() {
        return apiKey;
    }

    @Override
    public String toString() {
        return "User{" +
                "login='" + login + '\'' +
                ", userId='" + userId + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", uidNumber=" + uidNumber +
                ", roleId='" + roleId + '\'' +
                ", resourceIdentifier='" + resourceIdentifier + '\'' +
                ", apiKey='" + apiKey + '\'' +
                '}';
    }
}

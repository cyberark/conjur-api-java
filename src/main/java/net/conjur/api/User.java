package net.conjur.api;


import org.codehaus.jackson.annotate.JsonProperty;

public class User {

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


    /**
     * @return The login of the user that created this user
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @return The id of the role that owns this user (for a User, this will 
     * always be their own role id). 
     */
    public String getOwnerId() {
        return ownerId;
    }

    /**
     * @return An internally used id
     */
    public int getUidNumber() {
        return uidNumber;
    }

    /**
     * The full conjur role id of the role representing this user.  A conjur role id is of the form
     * {@code "account:kind:identifier"}, for example {@code "sandbox:user:gft5g0-alice"}.  A User
     * is related to exactly one role.
     * @return the conjur role id of the role representing this user.
     */
    public String getRoleId() {
        return roleId;
    }

    
    /**
     * The full conjur resource id of the resource representing this user.  A conjur resource id is of the form
     * {@code "account:kind:identifier"}, for example {@code "sandbox:user:gft5g0-alice"}.  A User
     * is related to exactly one resource.
     * @return the conjur resource id of the role representing this user.
     */
    public String getResourceIdentifier() {
        return resourceIdentifier;
    }

    /**
     * The login or username for this user.
     */
    public String getLogin() {
        return login;
    }

    /**
     * Gets the user's api key.  This property is only set on newly created users.
     * @return The user's api key.  
     */
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

package net.conjur.api;

import net.conjur.api.authn.AuthnClient;
import net.conjur.api.authn.Token;
import net.conjur.api.directory.DirectoryClient;
import net.conjur.api.directory.User;
import net.conjur.api.directory.Variable;
import net.conjur.api.directory.VariableDelagateMethods;
import net.conjur.util.Args;

/**
 * Facade for accessing the conjur services.
 */
public class Conjur implements VariableDelagateMethods {
    private final Endpoints endpoints;
    private Credentials credentials;
    private Token  token;
    private AuthnClient authnClient;
    private DirectoryClient directoryClient;

    Conjur(Endpoints endpoints, Credentials credentials){
        this.endpoints = Args.notNull(endpoints,"endpoints");
        this.credentials = Args.notNull(credentials, "credentials");
    }

    public static Conjur create(Credentials credentials, Endpoints endpoints){
        return new Conjur(endpoints == null ? Endpoints.getDefault() : endpoints, credentials);
    }

    public static Conjur create(Credentials credentials){
        return create(credentials, null);
    }

    public static Conjur fromPassword(String login, String password){
        return create(Credentials.fromPassword(login,password));
    }

    public static Conjur fromKey(String login, String apiKey){
        return create(Credentials.fromKey(login, apiKey));
    }

    public Conjur asUser(String login, String apiKey){
        return create(Credentials.fromKey(login, apiKey), endpoints);
    }

    public Conjur asUser(Credentials credentials){
        return create(credentials, endpoints);
    }

    public Conjur asUser(User user){
        return asUser(user.getCredentials());
    }

    /**
     * Get the API key for our credentials.  If it was not present, the client will call {@link #login()}
     * to fetch it.
     * @return the api key
     */
    public String getApiKey(){
        if(credentials.getApiKey() == null){
            login();
        }
        return credentials.getApiKey();
    }

    /**
     * Exchange our password for an API key, and update this instance's credentials.
     * Does nothing if we already have an API key.
     * @return this
     */
    public Conjur login(){
        if(credentials.getApiKey() != null)
            return this;
        String apiKey = getAuthnClient().login(credentials.getLogin(), credentials.getPassword());
        credentials = credentials.withApiKey(apiKey);
        return this;
    }

    /**
     * Request a new authentication token from conjur.  This method is called automatically
     * when making requests that require authentication if the client is not currently authenticated
     * or the current token will expire soon.
     *
     * @return the new token
     * @throws net.conjur.api.exceptions.http.UnauthorizedException if the credentials passed to the
     *  constructor are invalid.
     * @throws net.conjur.api.exceptions.ConjurApiException when other errors occur
     */
    public Token authenticate(){
        return token = getAuthnClient().authenticate(getLogin(), getApiKey());
    }

    /**
     * Get the API token currently in use by this instance.  If the token is not set or
     * will expire soon (within 1 minute), the client will retrieve a new token by calling
     * {@link #authenticate()}.
     *
     * @return an API token
     */
    public Token getToken(){
        if(needNewToken())
            authenticate();
        return token;
    }

    public User createUser(String login) {
        return getDirectoryClient().createUser(login);
    }


    public User createUser(String username, String password){
        return getDirectoryClient().createUser(username, password);
    }

    public User getUser(String login) {
        return getDirectoryClient().getUser(login);
    }

    public User tryGetUser(String login) {
        return getDirectoryClient().tryGetUser(login);
    }

    public boolean userExists(String login) {
        return getDirectoryClient().userExists(login);
    }

    public String createUniqueId(){
        return createVariable("unique-id").getId();
    }

    public Variable createVariable(String kind) {
        return getDirectoryClient().createVariable(kind).withDelegate(this);
    }

    public Variable createVariable(String kind, String mimeType) {
        return getDirectoryClient().createVariable(kind, mimeType).withDelegate(this);
    }

    public Variable createVariable(String kind, String mimeType, String id) {
        return getDirectoryClient().createVariable(kind, mimeType, id).withDelegate(this);
    }

    public boolean variableExists(String id) {
        return getDirectoryClient().variableExists(id);
    }

    public Variable getVariable(String id) {
        return getDirectoryClient().getVariable(id).withDelegate(this);
    }

    public Variable tryGetVariable(String id) {
        return getDirectoryClient().tryGetVariable(id);
    }

    public void addVariableValue(String variableId, String value) {
        getDirectoryClient().addVariableValue(variableId, value);
    }

    public String getVariableValue(String variableId) {
        return getDirectoryClient().getVariableValue(variableId);
    }

    public String getVariableValue(String variableId, int version) {
        return getDirectoryClient().getVariableValue(variableId, version);
    }

    private boolean needNewToken(){
        if(token == null)
            return true;
        // TODO check expiry
        return false;
    }

    private AuthnClient getAuthnClient() {
        // Clients are stateless so we just create them on every call
        return new AuthnClient(endpoints);
    }

    private DirectoryClient getDirectoryClient() {
        // Clients are stateless so we just create them on every call
        return new DirectoryClient(endpoints, getToken());
    }

    public String getLogin() {
        return credentials.getLogin();
    }
}

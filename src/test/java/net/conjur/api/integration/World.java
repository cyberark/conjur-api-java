package net.conjur.api.integration;

import cucumber.api.Scenario;
import cucumber.api.junit.Cucumber;
import net.conjur.api.Conjur;
import net.conjur.api.directory.User;
import net.conjur.api.directory.Variable;
import net.conjur.api.exceptions.http.HttpException;
import net.conjur.api.test.Support;
import net.conjur.util.Callable;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class World {
    private static Conjur adminClient;
    private static String namespace;
    private static final Map<String, User> users = new HashMap<String, User>();
    private static HttpException lastHttpException;
    private static Conjur currentClient;
    private static Variable currentVariable;
    private static User currentUser;

    public static void reset(){
        adminClient = null;
        namespace = null;
        lastHttpException = null;
        currentClient = null;
        currentUser = null;
        currentVariable = null;
        users.clear();
    }

    public static <T> T captureHttpExceptions(Callable<T> callable){
        try{
            lastHttpException = null;
            return callable.call();
        }catch(HttpException e){
            lastHttpException = e;
            return null;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static HttpException getLastHttpException(){
        return lastHttpException;
    }

    public static String getNamespace(){
        return namespace == null ?
                namespace = getAdminClient().createUniqueId() : namespace;
    }

    public static String withNamespacePrefix(String value){
        String ns = getNamespace();
        info("prefixing %s with %s", value, ns);
        if(ns != null && !value.startsWith(ns))
            return ns + ":" + value;
        return value;
    }

    public static Conjur getAdminClient(){
        return adminClient == null ? adminClient = Conjur.create(Support.getSystemCredentials()) : adminClient;
    }

    public static User getUserNamed(String name){
        return currentUser = users.get(withNamespacePrefix(name));
    }

    public static void putUserNamed(String name, User user){
        users.put(withNamespacePrefix(name), user);
    }

    public static User ensureUserNamed(String name){
        return getUserNamed(name) == null ? createUserNamed(name) : getUserNamed(name);
    }

    public static Conjur getCurrentClient(){
        return currentClient == null ? getAdminClient() : currentClient;
    }

    public static void  setCurrentClient(Conjur client){
        currentClient = client == null ? getAdminClient() : client;
    }

    public static User createUserNamed(String name){
        User user = currentUser = getCurrentClient().createUser(withNamespacePrefix(name));
        putUserNamed(name, user);
        return user;
    }

    public static Conjur getClientFor(String name){
        return getAdminClient().asUser(getUserNamed(name));
    }

    public static void info(String fmt, Object...args){
        System.out.format("[CUKES] " + fmt + "\n", args);
    }

}

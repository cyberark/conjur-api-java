package net.conjur.api.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import net.conjur.api.Conjur;
import net.conjur.api.Endpoints;
import net.conjur.api.authn.AuthnClient;
import net.conjur.api.authn.Token;
import net.conjur.api.directory.DirectoryClient;
import net.conjur.api.directory.User;
import net.conjur.api.directory.Variable;
import net.conjur.api.exceptions.ConjurApiException;
import net.conjur.api.exceptions.http.ForbiddenException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/*
 * This example shows how to create an Endpoint configuration, login as an existing
 * user, create a new user, and login as that user.
 * 
 * To run it, you will have to, at minimum, change USERNAME and PASSWORD to
 * the credentials provided for your conjur account.  By default, this will connect to
 * the conjur sandbox account, but you can configure the example to connect to a different
 * account by changing ACCOUNT and STACK to the appropriate values.
 * 
 * IMPORTANT: This example modifies conjur resources and should NOT be used on a "real" account.
 */
public class ConjurUsage {
    public static String username;
    public static String password;

    public static void configureFromArg(String authn){
        String[] parts = authn.split(":", 2);

        if(parts.length < 2){
            die("authn must contain :");
        }

        username = parts[0];
        password = parts[1];
    }

    public static void main(String[] args){
        try{
            mainWithExceptions(args);
        }catch(Throwable e){
            System.err.println("ConjurUsage threw an exception: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    // We want to print exceptions ourselves, so we wrap this method in the real main()
    public static void mainWithExceptions(String[] args) throws ConjurApiException, IOException {

        if(args.length == 1){
            configureFromArg(args[0]);
        }else{
            die("You must provide one argument");
        }

        // create a Conjur instance from the credentials provided in the args
        Conjur admin = Conjur.fromPassword(username, password);

        fmt("Using %s", admin);

        // Generate a unique id to use a namespace
        String ns = admin.createUniqueId();
        fmt("Using ns=%s",ns);

        // Create a user named alice
        String aliceLogin = ns + ":alice";
        fmt("Creating user alice as %s", aliceLogin);
        User alice = admin.createUser(aliceLogin);
        fmt("Created alice");

        // Now we make a client to perform actions as Alice
        Conjur aliceClient = admin.asUser(alice);

        fmt("Creating a variable for her secrets");
        // Create a JSON variable to hold her secret info
        Variable secrets = aliceClient.createVariable("secrets", "application/json");
        fmt("Created %s", secrets);

        // Initialize it with her secret question.  In real life this would be a model object
        // and could be serialized however you want.
        JsonObject secretsJson = new JsonObject();
        secretsJson.addProperty("firstName", "Alice");
        secretsJson.addProperty("lastName", "Example");
        secretsJson.addProperty("ssn4", "1234");
        secretsJson.addProperty("securityQuestion", "What is the meaning of life?");
        secretsJson.addProperty("securityAnswer", "42");
        String secretsJsonString = new Gson().toJson(secretsJson);

        fmt("Storing JSON %s", secretsJsonString);
        // Store it in the secrets variable
        secrets.addValue(secretsJsonString);
        fmt("Stored!");

        puts("Alice should be able to retrieve her secrets");
        try{
            fmt("She can: %s", aliceClient.getVariable(secrets.getId()).getValue());
        }catch(ConjurApiException e){
            die("Wut?" + e);
        }

        String bobLogin = ns + ":bob";
        fmt("Creating an evil user with login %s", bobLogin);
        User bob = admin.createUser(bobLogin);
        fmt("Evil user %s created >:-)", bob.getLogin());
        // Create a client to act as bob
        Conjur bobClient = admin.asUser(bob);
        try{
            puts("Bob is trying to access alice's secret question so he can steal her PHI!");
            String uhOh = bobClient.getVariable(secrets.getId()).getValue();
            fmt("Oh Noes! Bob can see alice's secret question!  Her PHI is his! The secret is '%s'", uhOh);
        }catch(ForbiddenException e){
            fmt("Denied!  Evil prevented!");
        }


    }

    // Some sugar
    public static void die(String msg){
        System.err.println(msg);
        System.exit(1);
    }
    public static void puts(String msg){
        System.out.println(msg);
    }
    public static void fmt(String msg, Object...args){
        System.out.println(String.format(msg, args));
    }
}

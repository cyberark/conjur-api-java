package net.conjur.api.examples;

import net.conjur.api.Conjur;
import net.conjur.api.Credentials;
import net.conjur.api.User;
import net.conjur.api.Variable;

import javax.ws.rs.ForbiddenException;

/**
 *
 */
public class BasicUsage {
    public static void main(String[] argv){
        // mvn exec doesn't print stack traces, so we wrap the "real"
        // main method to do so.
        try{
            realMain(argv);
        }catch(Throwable e){
            qq("Exception in main: %s", e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void realMain(String[] argv) throws Throwable {
        // First we need some credentials.  For tests, you can just read these
        // from system properties, although in production this is bad science.
        final Credentials credentials = Credentials.fromSystemProperties();

        // Next we can create a Conjur instance from the credentials.  This instance
        // will use Endpoints.getDefault(), but you could also pass an Endpoints
        // object to the constructor.
        final Conjur conjur = new Conjur(credentials);

        // Do everything in a namespace.  There's nothing particularly special about
        // using createId to get it, you could also generate a big random string.
        begin("Creating a unique id");
        final String ns = conjur.variables().createId();
        ok();

        // Create a user named alice.  We ue the client's "users" resource to do so.
        begin("Creating a user alice");
        final User alice = conjur.users().create(ns + "alice");
        ok();
        pp("alice=%s", alice);

        // Create a client that authenticates as alice.
        final Conjur aliceClient = new Conjur(alice.getLogin(), alice.getApiKey());

        // Alice will store her secret question in a variable called "alice-secret".  Variables
        // are created with a kind, mimeType, and optional identifier.
        begin("Creating a variable for her secret question");
        final Variable aliceSecret = aliceClient.variables().create("secret-question", "application/json", ns + "alice-secret");
        ok();
        pp("aliceSecret=%s", aliceSecret);

        // Put a json object in the variable.  Variables are versioned, so we don't set values, we add them.
       begin("Adding a value to her secret");
       aliceSecret.addValue("{\"question\":\"What is your favorite color?\", \"answer\":\"blue\"}");
       ok();

        // Alice should be able to read her secret question.
        begin("Fetching the value of the secret");
        final String fetchedValue = aliceClient.variables().get(ns + "alice-secret").getValue();
        ok();
        pp("fetchedValue=%s", fetchedValue);

        // Now we'll create a user named bob, and ensure that he can't access alice's secrets
        begin("Creating an evil user named bob");
        final User bob = conjur.users().create(ns + "bob");
        ok();
        pp("bob=%s", bob);
        final Conjur bobClient = new Conjur(bob.getLogin(), bob.getApiKey());

        // Bob is up to no good!
        pp("Bob wants to know alice's secret O.o");
        try{
            final String stolenSecret = bobClient.variables().get(ns + "alice-secret").getValue();
            qq("Yikes! He got it!");
        }catch(ForbiddenException e){
            pp("Denied! Evil is prevented!");
        }
    }

    // sugary stuff
    public static void pp(String fmt, Object...args){
        System.out.format(fmt + "\n", args);
    }
    public static void qq(String fmt, Object...args){
        System.err.format("[ERROR] " + fmt + "\n", args);
    }
    public static void begin(String what, Object...args){
        System.out.format(what + "...", args);
    }
    public static void ok(){
        System.out.println("OK!");
    }
}

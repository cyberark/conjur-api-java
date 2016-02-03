package net.conjur.api.examples;

import net.conjur.api.*;

import java.util.Collection;

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
        final String applianceUrl = System.getenv("CONJUR_APPLIANCE_URL");
        final String login = System.getenv("CONJUR_AUTHN_LOGIN");
        final String passwd = System.getenv("CONJUR_AUTHN_API_KEY");

        final Endpoints endpoints = Endpoints.getApplianceEndpoints(applianceUrl);
        final Credentials credentials = new Credentials(login, passwd);
        // Next we can create a Conjur instance from the credentials.  This instance
        // will use Endpoints.getDefault(), but you could also pass an Endpoints
        // object to the constructor.
        final Conjur conjur = new Conjur(credentials, endpoints);

        pp("conjur has account: %s", conjur.getAccount());

        final Role role = conjur.authorization().getRole("user:emilia.calvo");
        pp("made a role %s", role.getRoleId());
        pp("exists? %s", role.exists());

        final Collection<Role> memberships = role.getMemberships();
        pp("memberships of %s", role);
        for(Role r : memberships){
            pp("    %s", r);
        }

        pp("can she fry bacon? %s", role.isPermitted("food:bacon", "fry"));
        pp("can she eat bacon? %s", role.isPermitted("food:bacon", "eat"));

        Role me = conjur.authorization().getCurrentRole();
        pp("Who am I? %s", me);
        pp("I bet I can eat that bacon!");
        pp(me.isPermitted("food:bacon", "eat") ? "I CAN!" : "I CANNOT :-(");
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

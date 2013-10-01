import cucumber.api.Scenario;
import net.conjur.api.Conjur;
import net.conjur.api.Credentials;
import net.conjur.api.User;
import net.conjur.api.Users;
import net.conjur.api.authn.AuthnClient;

import java.util.HashMap;

public class BaseSteps {
    // TODO get these somewhere smarter

    public static class World{
        public Credentials adminCredentials = Credentials.fromSystemProperties();
        public Conjur conjur;
        public User theUser;
        private String namespace;

        public String namespace(String name){
            if(namespace == null)
                namespace = adminClient().variables().createId();
            if(name != null && !name.startsWith(namespace + "-")){
                name = namespace + "-" + name;
            }
            return name;
        }

        public Conjur adminClient(){
            return new Conjur(adminCredentials);
        }
    }

    public Scenario current;

    public World world;

    public void useCredentials(Credentials credentials){
        world.conjur = new Conjur(new AuthnClient(credentials));
    }

    public void init(Scenario scenario){
        current = scenario;

        // reset state
        world = new World();

        useCredentials(world.adminCredentials);
    }

    public void log(String msg, Object...args){
        if(current != null){
            current.write(String.format("[LOG] " + msg, args));
        }else{
            System.err.println(String.format("[CUKES] " + msg + "\n", args));
        }
    }
}

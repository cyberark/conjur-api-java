import net.conjur.api.Conjur;

public class App {
    public static void main(String[] args) {
        String password = "this is weak!";
        System.out.println(password);

//        Conjur conjur = new Conjur();
        // Create and load policy with name test-policy that creates a secret called `secret-password`
//        String retrievedSecret = conjur.variables().retrieveSecret("test-policy/secret-password");
//        System.out.println(retrievedSecret);

    }
}

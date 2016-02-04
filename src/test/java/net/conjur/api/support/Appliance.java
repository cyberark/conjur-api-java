package net.conjur.api.support;

import net.conjur.api.Conjur;
import net.conjur.api.Credentials;
import net.conjur.api.Endpoints;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.omg.CORBA.CharSeqHelper;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import scala.collection.script.End;

import static net.conjur.util.TextUtils.isEmpty;

public class Appliance implements TestRule {
    private static boolean availabilityExplained = false;


    private String applianceUrl = System.getenv("CONJUR_APPLIANCE_URL");
    private String login = System.getenv("CONJUR_AUTHN_LOGIN");
    private String apiKey = System.getenv("CONJUR_AUTHN_API_KEY");
    private boolean enabled = isPresent(System.getenv("CONJUR_JUNIT_APPLIANCE_AVAILABLE"));
    private Conjur conjur;

    public Conjur getConjur(){
        if(conjur == null) {
            final Endpoints endpoints = Endpoints.getApplianceEndpoints(applianceUrl);
            final Credentials creds = new Credentials(login, apiKey);
            conjur = new Conjur(creds, endpoints);
        }
        return conjur;
    }

    public boolean isAvailable(){
        boolean result = enabled &&
                isPresent(applianceUrl) &&
                isPresent(login) &&
                isPresent(apiKey);

        if(!result){
            explainAvailability(this);
        }

        return result;
    }

    public Statement apply(final Statement base, final Description description) {

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if(isAvailable()){
                    base.evaluate();
                }else{
                    System.err.printf("skipping %s because appliance is not available\n", description);
                }
            }
        };
    }

    private static boolean isPresent(CharSequence s){
        return !isEmpty(s);
    }

    private static void explainAvailability(Appliance subject){
        if(!availabilityExplained){
            availabilityExplained = true;
            System.err.println("Conjur appliance is unavailable:");
            System.err.printf("\tapplianceUrl=%s\n", subject.applianceUrl);
            System.err.printf("\tlogin=%s\n", subject.login);
            System.err.printf("\tapiKey=%s\n", subject.apiKey);
            System.err.printf("\tenabled=%s\n", subject.enabled);
            System.err.printf("\t$CONJUR_JUNIT_APPLIANCE_AVAILABLE=%s", System.getenv("CONJUR_JUNIT_APPLIANCE_AVAILABLE"));
        }
    }
}

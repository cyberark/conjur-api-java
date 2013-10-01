import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.conjur.api.Credentials;
import net.conjur.api.authn.AuthnClient;
import net.conjur.api.authn.Token;

import javax.ws.rs.NotAuthorizedException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

/**
 *
 */
public class AuthnSteps extends BaseSteps{
    AuthnClient client;
    Token token;
    Throwable thrown;

    @Before
    public void init(Scenario scenario){
        super.init(scenario);
    }

    @Given("^valid credentials$")
    public void valid_credentials(){
        client = new AuthnClient(Credentials.fromSystemProperties());
    }


    @Then("^I can fetch an api token$")
    public void I_can_fetch_an_api_token() throws Throwable {
        token = client.authenticate();
    }


    @Then("^I can fetch an api key$")
    public void I_can_fetch_an_api_key() throws Throwable {
       client.login();
    }

    @Given("^bogus credentials$")
    public void bogus_credentials() throws Throwable {
        client = new AuthnClient("bogus", "shmufogus");
    }


    @When("^I try to authenticate$")
    public void I_try_to_authenticate() throws Throwable {
        thrown = null;
        try{
            I_can_fetch_an_api_token();
        }catch(Throwable t){
            thrown = t;
        }
    }


    @When("^I try to login$")
    public void I_try_to_login() throws Throwable {
        thrown = null;
        try{
            I_can_fetch_an_api_key();
        }catch(Throwable t){
            thrown = t;
        }
    }


    @Then("^it throws not authorized$")
    public void it_throws_not_authorized() throws Throwable {
        assertThat(thrown, notNullValue());
        assertThat(thrown, instanceOf(NotAuthorizedException.class));
    }

    @And("^I show the token$")
    public void I_show_the_token() throws Throwable {
        log("token=%s", token);
    }
}

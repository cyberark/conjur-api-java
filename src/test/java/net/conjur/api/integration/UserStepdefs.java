package net.conjur.api.integration;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import static junit.framework.Assert.*;

import net.conjur.api.Conjur;
import net.conjur.api.directory.DirectoryClient;
import net.conjur.api.directory.User;
import net.conjur.api.exceptions.ConjurApiException;
import net.conjur.api.exceptions.http.HttpException;
import net.conjur.api.test.Support;
import net.conjur.util.Callable;

import static net.conjur.api.integration.World.*;

public class UserStepdefs extends BaseSteps{
    @Before
    public void doBefore(Scenario s){ before(s);}
    @After
    public void doAfter(){after();}

    @When("^I create a user named (.+)$")
    public void i_create_a_user_named(String username){
        String id = namespaced(username);
        debug("creating user %s as %s", username, id);
        User user = getCurrentClient().createUser(id);
        putUserNamed(username, user);
        debug("created: %s %s as %s", username, getUserNamed(username), id);
    }

    @Then("^I can retrieve a token$")
    public void I_can_retrieve_a_token() throws Throwable {
        debug("retrieving token for %s", getCurrentClient().getLogin());
        assertNotNull(getCurrentClient().getToken());
    }

    @Given("^a user named (.+)$")
    public void a_user_named(String name){
        i_create_a_user_named(name);
    }
}

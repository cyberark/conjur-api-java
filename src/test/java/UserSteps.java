import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.conjur.api.Conjur;
import net.conjur.api.Credentials;
import net.conjur.api.User;
import net.conjur.api.authn.AuthnClient;
import org.junit.Assert;

import javax.ws.rs.ForbiddenException;
import java.util.Random;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class UserSteps extends BaseSteps{
    String password = "bizbangbam";

    @Before
    public void init(Scenario s){
        super.init(s);
        world.conjur = world.adminClient();
        password = "bizbangbam";
    }
    @When("^I (?:can )?create a user named (.+)$")
    public void I_create_a_user_named(String username) throws Throwable {
        world.theUser = world.conjur.users().create(world.namespace(username));
    }


    @Then("^I (?:can )?authenticate with the password$")
    public void I_can_authenticate_with_the_password(){
        final Credentials creds = new Credentials(world.theUser.getLogin(), password);
        world.conjur = new Conjur(creds);
        world.conjur.getAuthn().authenticate();
    }

    @Then("^I (?:can )?log\\s*in as the user$")
    public void I_can_login_as_the_user() throws Throwable {
        final User user = world.theUser;
        assertThat(user, notNullValue());
        assertThat(user.getApiKey(), notNullValue());
        world.conjur = new Conjur(new Credentials(user.getLogin(), user.getApiKey()));
        world.conjur.getAuthn().authenticate(false);
    }

    @Then("^user (.+) (?:(exists)|(does not exist))$")
    public void user_exists(String username, String shouldExist, String shouldNotExist) throws Throwable {
        final boolean expected = shouldNotExist == null;
        username = world.namespace(username);
        final boolean exists = world.conjur.users().exists(username);
        assertEquals("user " + username + " should exist", expected, exists);
    }

    @When("^I create a user with a password$")
    public void I_create_a_user_with_a_password() throws Throwable {
        world.theUser = world.conjur.users().create(world.namespace("user-with-password"), password);
    }


    @And("^I change the user's password$")
    public void I_change_the_users_password() throws Throwable {
        AuthnClient authnClient = (AuthnClient)world.conjur.getAuthn();
        authnClient.updatePassword("foobar");
        password = "foobar";
    }


    @Given("^a user named \"([^\"]*)\"( with a password)?$")
    public void a_user_named_with_a_password(String login, boolean withPassword) throws Throwable {
        Conjur client =world.adminClient();
        if(withPassword){
            world.theUser = client.users().create(world.namespace(login), password);
        }else{
            world.theUser = client.users().create(world.namespace(login));
        }
    }

}

import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.conjur.api.Conjur;
import net.conjur.api.Credentials;
import net.conjur.api.User;

import java.util.Random;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class UserSteps extends BaseSteps{
    @Before
    public void init(Scenario s){
        super.init(s);
    }
    @When("^I (?:can )?create a user named (.+)$")
    public void I_create_a_user_named(String username) throws Throwable {
        world.theUser = world.conjur.users().create(world.namespace(username));
    }



    @Then("^I (?:can )?login as the user$")
    public void I_can_login_as_the_user() throws Throwable {
        final User user = world.theUser;
        assertThat(user, notNullValue());
        assertThat(user.getApiKey(), notNullValue());
        world.conjur = new Conjur(new Credentials(user.getLogin(), user.getApiKey()));
        world.conjur.getAuthn().authenticate(false);
    }
}

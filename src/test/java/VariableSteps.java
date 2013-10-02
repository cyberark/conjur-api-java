import cucumber.api.PendingException;
import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import junit.framework.Assert;
import net.conjur.api.Variable;
import net.conjur.util.Callable;
import org.hamcrest.CoreMatchers;

import javax.ws.rs.NotFoundException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
/**
 *
 */
public class VariableSteps extends BaseSteps {
    Throwable thrown;
    String value;
    Variable variable;

    @Before
    public void init(Scenario s){
        thrown = null;
        value = null;
        variable = null;
    }

    @When("^I try to get the value of \"([^\"]*)\"$")
                            public void I_try_to_get_the_value_of(final String variable) throws Throwable {
        thrown = null;
        try{
            I_get_the_value_of(variable);
        }catch(Throwable t){
            thrown = t;
        }
    }

    @When("^I get the value of \"([^\"]*)\"")
    public void I_get_the_value_of(String id){
        value = world.conjur.variables().get(id).getValue();
    }

    @Then("^it throws not found$")
    public void it_throws_not_found() throws Throwable {
        assertThat(thrown, is(instanceOf(NotFoundException.class)));
    }

    @Given("^I create a variable \"([^\"]*)\"$")
    public void I_create_a_variable(String id) throws Throwable {
        variable = world.conjur.variables().create("test", "text/plain", world.namespace(id));
    }

    @When("^I try to get the value$")
    public void I_try_to_get_the_value() throws Throwable {
       thrown = null;
        try{
            I_get_the_value();
        }catch(Throwable t){
            thrown = t;
        }
    }


    @When("^I add the value \"([^\"]*)\"$")
    public void I_add_the_value(String value) throws Throwable {
        variable.addValue(value);
    }

    @When("^I get the value$")
    public void I_get_the_value() throws Throwable {
        value = variable.getValue();
    }

    @Then("^the value should be \"([^\"]*)\"$")
    public void the_value_should_be(String expected) throws Throwable {
        assertEquals(expected, value);
    }

    @Then("^it should have (\\d+) version(?:s)?$")
    public void it_should_have_versions(int versionCount) throws Throwable {
       assertEquals(versionCount, variable.getVersionCount());
    }

    @When("^I get the value of version (\\d+)$")
    public void I_get_the_value_of_version(int version) throws Throwable {
        value = variable.getValue(version);
    }

    @When("^I try to get the value of version (\\d+)$")
    public void I_try_to_get_the_value_of_version(final int version) throws Throwable {
        thrown = null;
        try{
            I_get_the_value_of_version(version);
        }catch(Throwable t){
            thrown = t;
        }
    }

    @Then("^variable \"([^\"]*)\" should (not )?exist$")
    public void variable_should_exist(String id, String not) throws Throwable {
        final boolean shouldExist = not == null;
        final boolean exists = world.conjur.variables().exists(world.namespace(id));
        if(!shouldExist && exists){
            fail("variable should not exist, but it does!");
        }
        if(shouldExist && !exists){
            fail("variable should exist but it doesn't!");
        }
    }
}

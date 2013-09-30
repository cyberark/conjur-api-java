package net.conjur.api.integration;

import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.*;
import net.conjur.api.directory.Variable;
import net.conjur.api.exceptions.ConjurApiException;
import net.conjur.api.exceptions.http.ForbiddenException;
import net.conjur.util.Callable;

import static junit.framework.Assert.*;
import static net.conjur.api.integration.World.*;

/**
 *
 */
public class VariableStepdefs extends BaseSteps{
    private String variableId;
    private Variable variable;
    private String variableValue;
    private Throwable failure;

    @Before
    public void doBefore(Scenario s){
       variable = null; variableId = null; variableValue = null;
       before(s);
    }

    @When("^I create a variable (?:of kind (.+) )?(?:with id (.+))?$")
    public void I_create_a_variable_with_id(String kind, String id) throws Throwable {
       String nsid = namespaced(id);
       debug("creating a variable for %s as %s", id, nsid);
       variable = null;
       if(kind == null)
           kind = "test";

       variable = getCurrentClient().createVariable(kind, "text/plain", nsid);
       variableId = variable.getId();
       debug("created the variable with id=%s: %s", variableId, variable);
    }

    @When("^I create a variable (?:\"([^\"]*)\" )?with value \"([^\"]*)\"")
    public void I_create_a_variable_with_value(String id, String value) throws Throwable{
        I_create_a_variable_with_id(null, id);
        I_add_a_value_to_the_variable(value);

    }

    @When("^I get the value of variable \"([^\"]*)\"$")
    public void I_get_the_value_of_variable(final String id) throws Throwable {
        variable = getCurrentClient().getVariable(namespaced(id));
        variableValue = variable.getValue();
    }

    @When("^I add a value \"([^\"]*)\"")
    public void I_add_a_value_to_the_variable(final String value){
        assertFalse("no variable in use!", variable == null && variableId == null);
        if(variable == null){
            variable = getCurrentClient().getVariable(variableId);
            debug("fetching variable %s", variableId);
        }
        debug("adding value to %s", variable);
        variable.addValue(value);
    }

    @Then("^the value should be \"([^\"]*)\"$")
    public void the_value_should_be(String expectedValue) throws Throwable {
        assertEquals(variableValue, expectedValue);
    }


    @Given("^([^\\s]+) has a variable named \"([^\"]*)\"(?: holding \"([^\"]*)\")?$")
    public void user_has_a_variable_named(String user, String variable, String value) throws Throwable {
        setCurrentClient(getClientFor(user));
        I_create_a_variable_with_id(null, variable);
        I_add_a_value_to_the_variable(value);
    }

    @When("^I try to get the value of variable \"([^\"]*)\"$")
    public void I_try_to_get_the_value_of_variable(String id) throws Throwable {
        try{
            getCurrentClient().getVariable(namespaced(id)).getValue();
            failure = null;
        }catch(ConjurApiException e){
            failure = e;
        }
    }

    @Then("^permission is denied$")
    public void permission_is_denied() throws Throwable {
        Throwable f = failure;
        failure = null;
        if(!(f instanceof ForbiddenException))
            fail("expected forbidden, was " + f);
    }

    @Then("^it should have (\\d+) version(?:s)?$")
    public void it_should_have_versions(int num) throws Throwable {
        assertNotNull(variable);
        assertEquals(num, variable.refresh().getVersionCount());
    }

    @Then("^the value of version (\\d+) is \"([^\"]*)\"$")
    public void the_value_of_version_is(int version, String value) throws Throwable {
        assertEquals(value, getCurrentClient().getVariable(namespaced(variableId)).getValue(version));
    }
}

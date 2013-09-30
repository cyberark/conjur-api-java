package net.conjur.api.integration;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.conjur.api.directory.User;
import net.conjur.api.exceptions.http.ForbiddenException;
import net.conjur.api.exceptions.http.HttpException;

import static junit.framework.Assert.*;
import static net.conjur.api.integration.World.*;

/**
 *
 */
public class GeneralStepdefs {

    @Then("it should have worked")
    public void it_should_have_worked(){
        assertNull("It didn't work: " + getLastHttpException(), getLastHttpException());
    }


    @When("I am an admin")
    public void when_i_am_an_admin(){
        setCurrentClient(getAdminClient());
    }

    @When("^I log in as (.+)$")
    public void i_log_in_as(String name){
        setCurrentClient(getAdminClient().asUser(getUserNamed(name)));
    }

    @Given("^I am logged in as \"?(.+)\"?$")
    public void given_i_am_logged_in_as(String username){
        User me = getUserNamed(username);
        assertNotNull(me);
        setCurrentClient(getAdminClient().asUser(me));
    }

    @And("^I create a namespace$")
    public void I_create_a_namespace() throws Throwable {
        info("Created namespace %s", getNamespace());
    }
}

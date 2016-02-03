package net.conjur.api;

import net.conjur.api.support.Appliance;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class TestAccount {

    // Only run when a Conjur appliance is available.
    @Rule
    public Appliance appliance = new Appliance();

    Conjur conjur;
    private String account;

    @Before
    public void setUp(){
        conjur = appliance.getConjur();
        String prop = System.getenv("CONJUR_ACCOUNT");
        account = prop == null ? "cucumber" : prop;
    }

    @Test
    public void testGetAccount(){
        assertEquals("account should be as given",account,conjur.getAccount());
    }
}

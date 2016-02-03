package net.conjur.api;

import net.conjur.api.support.Appliance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestRole {
    // Only run when a Conjur appliance is available.
    @Rule
    public Appliance appliance = new Appliance();

    Conjur conjur;
    Role cat;
    Resource outside;
    String namespace;
    Role cats;

    @Before
    public void setUp(){
        conjur = appliance.getConjur();
        namespace = conjur.variables().createId();
        cat = conjur.authorization().getRole("cat:" + namespace + "/möüse");
        outside = conjur.authorization().getResource("place:" + namespace + "/outside");
        outside.createIfNotFound();
        cats = conjur.authorization().getRole("animal:" + namespace + "/cats");
    }

    @Test
    public void testCreateRole(){
        assertFalse(cat.exists());
        cat.create();
        assertTrue(cat.exists());
    }

    @Test
    public void testIsPermitted(){
        cat.createIfNotExists();
        assertFalse(cat.isPermitted(outside, "go"));
        outside.permit(cat, "go");
        assertTrue(cat.isPermitted(outside,"go"));
    }

    @Test
    public void testGetCurrentRole(){
        final Role currentRole = conjur.getCurrentRole();
        assertEquals("should be a user", "user", currentRole.getRoleId().getKind());
        assertEquals("should be me", conjur.getAuthn().getUsername(), currentRole.getRoleId().getId());
        assertEquals("should have the right account", conjur.getAccount(), currentRole.getRoleId().getAccount());
    }

    @Test
    public void testGrant(){

    }
}

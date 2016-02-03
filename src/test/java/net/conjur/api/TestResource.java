package net.conjur.api;


import net.conjur.api.support.Appliance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestResource {
    // Only run when a Conjur appliance is available.
    @Rule
    public Appliance appliance = new Appliance();

    Conjur conjur;
    String namespace;
    Resource subject;

    @Before
    public void setUp(){
        conjur = appliance.getConjur();
        namespace = conjur.variables().createId();
        subject = conjur.authorization().getResource("food:" + namespace + "/bacon");
        System.err.printf("Using namespace %s\n", namespace);
    }

    @Test
    public void testCreateAndExists(){
        assertFalse("subject shouldn't exist yet",subject.exists());
        subject.create();
        assertTrue(subject.exists());
    }

    @Test
    public void testPermit(){
        subject.createIfNotFound();
        assertTrue("subject should exist", subject.exists());
        Role role = conjur.authorization().getRole("user:" + namespace + "/françois");
        role.createIfNotExists();
        assertTrue("role should exist", role.exists());
        assertFalse("role can't eat bacon", role.isPermitted(subject, "eat"));
        subject.permit(role, "eat");
        assertTrue("subject can eat bacon after permitted", role.isPermitted(subject, "eat"));
    }

    @Test
    public void testDeny(){
        subject.createIfNotFound();
        assertTrue("subject should exist", subject.exists());
        Role role = conjur.authorization().getRole("user:" + namespace + "/françois");
        role.createIfNotExists();
        assertTrue("role should exist", role.exists());
        assertFalse("role can't eat bacon", role.isPermitted(subject, "eat"));
        subject.permit(role, "eat");
        assertTrue("role can eat bacon after permitted", role.isPermitted(subject, "eat"));
        subject.deny(role, "eat");
        assertFalse("after deny role cannot eat bacon", role.isPermitted(subject, "eat"));
    }
}

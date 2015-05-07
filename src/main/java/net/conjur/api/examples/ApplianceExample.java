package net.conjur.api.examples;

import net.conjur.api.*;

/**
 * This example shows how to configure the Conjur API to talk to an
 * appliance.
 *
 * We'll pretend that the appliance has a url "https://conjur.yourcompany.com"
 */
public class ApplianceExample {
    public static void main(String[] args){

        /*
         * Configure these appropriately
         */
        final String applianceUrl = "https://10.0.3.97/api";
        final String login = "admin";
        final String password = "password";



        final Endpoints endpoints = Endpoints.getApplianceEndpoints(applianceUrl);

        puts("authnUrl=" + endpoints.getAuthnUri());
        puts("authzUrl=" + endpoints.getAuthzUri());
        puts("directoryUri=" + endpoints.getDirectoryUri());



        final Credentials credentials = new Credentials(login, password);
        final Conjur conjur = new Conjur(credentials, endpoints);

        final Variable v = conjur.variables().create("test");
        puts(v.getId() + ", " + v.getKind() + ", " + v.getMimeType() + ", " + v.getVersionCount());
        v.addValue("hello");
        puts("value=" + v.getValue() + " (" + v.getVersionCount() + ")");

        final User user = conjur.users().create("foasdf", "pass");
        puts("created " + user);
    }

    static void puts(String s){
        System.out.println(s);
    }
}

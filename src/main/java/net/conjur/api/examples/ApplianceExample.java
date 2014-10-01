package net.conjur.api.examples;

import net.conjur.api.Conjur;
import net.conjur.api.Credentials;
import net.conjur.api.Endpoints;

/**
 * This example shows how to configure the Conjur API to talk to an
 * appliance.
 *
 * We'll pretend that the appliance has a url "https://conjur.yourcompany.com"
 */
public class ApplianceExample {
    public static void main(String[] args){
        // The appliance URL
        final String applianceUrl = "https://ec2-107-21-87-192.compute-1.amazonaws.com/api";
        final Endpoints endpoints = Endpoints.getApplianceEndpoints(applianceUrl);

        System.out.println("authnUrl=" + endpoints.getAuthnUri());
        System.out.println("authzUrl=" + endpoints.getAuthzUri());
        System.out.println("directoryUri=" + endpoints.getDirectoryUri());


        final String login = "admin";
        final String password = "2xbwrtd9rfdb43e274jfqhtry6";
        Credentials credentials = new Credentials(login, password);
        Conjur conjur = new Conjur(credentials, endpoints);

        conjur.variables().create("test");
    }
}

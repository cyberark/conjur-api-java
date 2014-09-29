package net.conjur.api.examples;

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
        final String applianceUrl = "https://conjur.yourcompany.com";
        final Endpoints endpoints = Endpoints.getApplianceEndpoints(applianceUrl);

        System.out.println("authnUrl=" + endpoints.getAuthnUri());
        System.out.println("authzUrl=" + endpoints.getAuthzUri());
        System.out.println("directoryUri=" + endpoints.getDirectoryUri());
    }
}

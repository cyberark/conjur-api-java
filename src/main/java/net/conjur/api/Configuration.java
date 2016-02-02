package net.conjur.api;

import net.conjur.util.Args;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Conjur configuration, typically loaded from .conjurrc files and/or environment
 * variables and system properties.
 */
public class Configuration {
    // ~ static accessors
    private static Configuration defaultConfiguration =
            new Configuration();

    public static Configuration getDefault() {
        return defaultConfiguration;
    }

    public static void setDefault(@Nonnull Configuration configuration){
        defaultConfiguration = Args.notNull(configuration, "configuration");
    }

    // ~ instance fields
    private URI applianceUrl;
    private String certificatePath;

    // ~ properties
    public URI getApplianceUrl(){
        return applianceUrl;
    }

    public Configuration setApplianceUrl(String applianceUrl){
        return setApplianceUrl(URI.create(applianceUrl));
    }

    public Configuration setApplianceUrl(URI applianceUrl){
        this.applianceUrl = Args.notNull(applianceUrl, "applianceUrl");
        return this;
    }

    public String getCertificatePath(){
        return certificatePath;
    }

    public Configuration setCertificatePath(String certificatePath){
        this.certificatePath = certificatePath;
        return this;
    }

}

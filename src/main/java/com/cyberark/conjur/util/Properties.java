package com.cyberark.conjur.util;

/**
 * Properties helpers
 */
public class Properties {

    public static String getMandatoryProperty(String name) {
        return getMandatoryProperty(name, null);
    }

    /**
    * Retrieve properties for system properties if not 
    * found then retrieve from environment variables.
    * If still not found and default value is null 
    * throw IllegalArgumentException
    @param name the name of the configuration property
    @param def the definition of the property
    @return the properties for system properties
    */

    public static String getMandatoryProperty(String name, String def) {
        String value = System.getProperty(name, System.getenv(name));
        if(value == null) { 
            value = def; 
        }
        if (value == null) {
            throw new IllegalArgumentException(String.format("Conjur config property '%s' was not provided", name));
        }
        return value;
    }

}


package net.conjur.util;

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


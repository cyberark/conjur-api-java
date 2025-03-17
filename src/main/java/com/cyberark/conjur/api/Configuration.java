package com.cyberark.conjur.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Configuration {

    private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());

    private static final String DEFAULT_INTEGRATION_NAME = "SecretsManagerJava API";
    private static final String DEFAULT_INTEGRATION_TYPE = "cybr-secretsmanager-java-api";
    private static final String DEFAULT_VENDOR_NAME = "CyberArk";

    private String integrationName;
    private String integrationType;
    private String integrationVersion;
    private String vendorName;

    private String telemetryHeader = null;

    /**
     * Default constructor that initializes the configuration with default or environment variable values.
     */
    public Configuration() {
        this.integrationName = System.getenv().getOrDefault("INTEGRATION_NAME", DEFAULT_INTEGRATION_NAME);
        this.integrationType = System.getenv().getOrDefault("INTEGRATION_TYPE", DEFAULT_INTEGRATION_TYPE);
        this.integrationVersion = getSDKVersion();
        this.vendorName = System.getenv().getOrDefault("VENDOR_NAME", DEFAULT_VENDOR_NAME);
    }

    /**
     * Gets the integration name.
     * 
     * @return the integration name
     */
    public String getIntegrationName() {
        return integrationName;
    }

    /**
     * Sets the integration name and invalidates the cached telemetry header.
     * 
     * @param integrationName the integration name to set
     */
    public void setIntegrationName(String integrationName) {
        this.integrationName = (integrationName != null && !integrationName.isEmpty())
                               ? integrationName : DEFAULT_INTEGRATION_NAME;
        telemetryHeader = null;
    }

    /**
     * Gets the integration type.
     * 
     * @return the integration type
     */
    public String getIntegrationType() {
        return integrationType;
    }

    /**
     * Sets the integration type and invalidates the cached telemetry header.
     * 
     * @param integrationType the integration type to set
     */
    public void setIntegrationType(String integrationType) {
        this.integrationType = (integrationType != null && !integrationType.isEmpty())
                               ? integrationType : DEFAULT_INTEGRATION_TYPE;
        telemetryHeader = null;
    }

    /**
     * Gets the integration version.
     * 
     * @return the integration version
     */
    public String getIntegrationVersion() {
        return integrationVersion;
    }

    /**
     * Sets the integration version and invalidates the cached telemetry header.
     * 
     * @param integrationVersion the integration version to set
     */
    public void setIntegrationVersion(String integrationVersion) {
        this.integrationVersion = (integrationVersion != null && !integrationVersion.isEmpty())
                                  ? integrationVersion : getSDKVersion();
        telemetryHeader = null;
    }

    /**
     * Gets the vendor name.
     * 
     * @return the vendor name
     */
    public String getVendorName() {
        return vendorName;
    }

    /**
     * Sets the vendor name and invalidates the cached telemetry header.
     * 
     * @param vendorName the vendor name to set
     */
    public void setVendorName(String vendorName) {
        this.vendorName = (vendorName != null && !vendorName.isEmpty())
                          ? vendorName : DEFAULT_VENDOR_NAME;
        telemetryHeader = null;
    }

    /**
     * Gets the telemetry header, regenerating it if it's null.
     * 
     * @return the telemetry header as a string
     */
    public String getTelemetryHeader() {
        if (telemetryHeader == null) {
            telemetryHeader = generateTelemetryHeader(integrationName, integrationType, integrationVersion, vendorName);
        }
        return telemetryHeader;
    }

    /**
     * Generates a telemetry header based on the provided integration details.
     * 
     * @param integrationName the integration name
     * @param integrationType the integration type
     * @param integrationVersion the integration version
     * @param vendorName the vendor name
     * @return the encoded telemetry header
     */
    private String generateTelemetryHeader(String integrationName, String integrationType,
                                           String integrationVersion, String vendorName) {
        String fieldValuePairs = "in=" + integrationName
                                 + "&it=" + integrationType
                                 + "&iv=" + integrationVersion
                                 + "&vn=" + vendorName;
        return Base64.getUrlEncoder().encodeToString(fieldValuePairs.getBytes());
    }

    /**
     * Reads the SDK version from the VERSION file in the current directory.
     * 
     * @return the SDK version or "unknown" if the VERSION file is not found or is empty
     */
    public static String getSDKVersion() {
        try {
            Path rootDir = Paths.get(System.getProperty("user.dir"));
            Path versionFile = rootDir.resolve("VERSION");

            if (Files.exists(versionFile)) {
                String version = new String(Files.readAllBytes(versionFile)).trim();

                if (version.isEmpty()) {
                    LOGGER.warning("VERSION file is empty.");
                    return "unknown";
                }

                String versionWithoutSnapshot = version.split("-")[0];
                return versionWithoutSnapshot;
            } else {
                LOGGER.warning("VERSION file not found at: " + versionFile);
                return "unknown";
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading VERSION file: " + e.getMessage(), e);
            return "unknown";
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error occurred: " + e.getMessage(), e);
            return "unknown";
        }
    }
}
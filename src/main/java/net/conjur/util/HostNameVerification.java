package net.conjur.util;

import javax.net.ssl.*;
import javax.ws.rs.client.ClientBuilder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Don't use this in production!!
 */
public class HostNameVerification {

    // hostname verification can be disabled with a system property
    public static final String DISABLE_HOSTNAME_VERIFICATION_PROPERTY =
            "net.conjur.api.disableHostnameVerification";
    // or with an environment variable
    public static final String DISABLE_HOSTNAME_VERIFICATION_KEY =
            "CONJUR_JAVA_API_DISABLE_HOSTNAME_VERIFICATION";


    private static final HostNameVerification instance = new HostNameVerification();

    public static HostNameVerification getInstance(){
        return instance;
    }

    private boolean hostNameVerificationDisabled = false;
    private boolean warned = false;

    private HostNameVerification(){
        initializeFromEnvironment();
    }

    private void initializeFromEnvironment(){
        final String property = System.getProperty(DISABLE_HOSTNAME_VERIFICATION_PROPERTY);
        final String env = System.getenv(DISABLE_HOSTNAME_VERIFICATION_KEY);

        if("true".equals(property) || "true".equals(env)){
            hostNameVerificationDisabled = true;
        }
    }



    public boolean isHostNameVerificationDisabled(){
        return hostNameVerificationDisabled;
    }

    public ClientBuilder updateClientBuilder(ClientBuilder clientBuilder){
        if(isHostNameVerificationDisabled()){
            showWarning();
            return clientBuilder.sslContext(createGullibleSSLContext());
        }
        return clientBuilder;
    }

    private void showWarning(){
        if(!warned){
            warned = true;
            System.err.print("*********************************************************************\n" +
                    "   WARNING!!! \n" +
                    "   You have disabled hostname verification by setting the " +
                        DISABLE_HOSTNAME_VERIFICATION_PROPERTY + " system property\n" +
                    "   or the " + DISABLE_HOSTNAME_VERIFICATION_KEY + " environment variable" +
                    "   YOU SHOULD NOT DO THIS IN PRODUCTION!!!!\n" +
                    "*********************************************************************\n");
        }
    }

    private SSLContext createGullibleSSLContext(){
        try {
            final SSLContext context = SSLContext.getInstance("SSL");
            context.init(null, new TrustManager[]{ new GullibleTrustManager() }, new SecureRandom());
            return context;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("failed to disable hostname verification", e);
        } catch (KeyManagementException e) {
            throw new RuntimeException("failed to disable hostname verification", e);
        }

    }

    private class GullibleTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            // Do nothing to trust it
        }

        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            // Do nothing to trust it
        }

        public X509Certificate[] getAcceptedIssuers() {
            // return null to trust everything
            return null;
        }
    }
}

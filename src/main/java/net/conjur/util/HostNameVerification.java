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

    public static final String DISABLE_HOSTNAME_VERIFICATION_PROPERTY =
            "net.conjur.api.disableHostnameVerification";


    private static final HostNameVerification instance = new HostNameVerification();

    public static HostNameVerification getInstance(){
        return instance;
    }


    private Boolean hostNameVerificationDisabled = null;

    public boolean isHostNameVerificationDisabled(){
        if(hostNameVerificationDisabled == null){
            final String prop = System.getProperty(DISABLE_HOSTNAME_VERIFICATION_PROPERTY);
            if(prop != null && prop.equals("true")){
                hostNameVerificationDisabled = true;
            }else{
                hostNameVerificationDisabled = false;
            }
        }
        return hostNameVerificationDisabled;
    }

    public void setHostNameVerificationDisabled(boolean disabled){
        hostNameVerificationDisabled = disabled;
    }

    public ClientBuilder updateClientBuilder(ClientBuilder clientBuilder){
        if(isHostNameVerificationDisabled()){
            showWarning();
            return clientBuilder.sslContext(createGullibleSSLContext());
        }
        return clientBuilder;
    }


    private static boolean warned = false;
    private static void showWarning(){
        if(!warned){
            warned = true;
            System.err.print("*********************************************************************\n" +
                    "   WARNING!!! \n" +
                    "   You have disabled hostname verification by setting the " + DISABLE_HOSTNAME_VERIFICATION_PROPERTY + " system property!!!\n" +
                    "   YOU SHOULD NOT DO THIS!!!\n" +
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

    class GullibleTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            // Do nothing to trust it
        }

        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            // Do nothing to trust it
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}

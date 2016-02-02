package net.conjur.util;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.ClientBuilder;

/**
 * Don't use this!
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
            clientBuilder.hostnameVerifier(new HostnameVerifier() {
                public boolean verify(String s, SSLSession sslSession) {
                    System.out.println("verifying hostname " + s);
                    return true;
                }
            });
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
}

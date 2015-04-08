package net.conjur.api.examples;

import net.conjur.api.authn.AuthnClient;
import net.conjur.api.authn.Token;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Simple example demonstrating conjur authn
 */
public class AuthnExample {
    public static void main(String[] args) throws Exception{
        // print stack traces even when mvn doesn't want to
        try{
            main();
        }catch(Throwable ex){
            ex.printStackTrace();
        }
    }

    public static void main() throws Exception{
        // obviously reading passwords off the stdin is pretty sketch.  Don't use
        // production credentials here.
        String login = gets("Enter your conjur login: ");
        String pswd  = gets("Enter your conjur password: ");

        // Create an authn client
        puts("Using credentials %s:%s", login, pswd);
        AuthnClient authn = new AuthnClient(login, pswd);

        // use it to get an api key for our login
        String apiKey = authn.login();
        puts("Your api key is %s", apiKey);

        // use it to get an authn token
        Token token = authn.authenticate();
        puts("Your authn token is %s", token);

        // we can also do both operations with an api key
        authn = new AuthnClient(login, apiKey);
        puts("Using credentials %s:%s", login, apiKey);
        puts("Login: %s", authn.login());
        puts("Authenticate: %s", authn.authenticate());
    }

    public static String gets(String prompt) throws Exception{
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(prompt);
        return reader.readLine();
    }

    public static void puts(String msg, Object...args){
        System.out.format(msg + "\n", args);
    }
}

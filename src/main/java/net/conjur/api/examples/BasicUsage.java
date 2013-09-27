package net.conjur.api.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.conjur.api.ConjurApiException;
import net.conjur.api.Endpoints;
import net.conjur.api.User;
import net.conjur.api.authn.Authn;
import net.conjur.api.authn.Token;
import net.conjur.api.directory.Directory;

/*
 * This example shows how to create an Endpoint configuration, login as an existing
 * user, create a new user, and login as that user.
 * 
 * To run it, you will have to, at minimum, change USERNAME and PASSWORD to
 * the credentials provided for your conjur account.  By default, this will connect to
 * the conjur sandbox account, but you can configure the example to connect to a different
 * account by changing ACCOUNT and STACK to the appropriate values.
 * 
 * IMPORTANT: This example modifies conjur resources and should NOT be used on a "real" account.
 */
public class BasicUsage {
	// TODO document stack/account
	
	// Conjur account.  Use sandbox by default.
	public static final String ACCOUNT = "sandbox";
	
	// Conjur stack.  Use ci (standard for sandbox) by default.
	public static final String STACK = "ci";
	
	// Username for an existing conjur user, used for the initial authentication
	public static final String USERNAME = null;
	
	// Password for the user specified by USERNAME
	public static final String PASSWORD = null;
	
	public static void main(String[] args) throws ConjurApiException, IOException {
		puts("*** BASIC CONJUR USAGE***");
		
		// Make sure you've configured the example;
		if(USERNAME == null)
			die("You must set the USERNAME constant to run this example");
		if(PASSWORD == null)
			die("You must set the PASSWORD constant to run this example");
		
		/* To connect to conjur services, you need an Endpoints object that provides the 
		 * endpoints (urls) to which the conjur client connects.  An endpoint needs, at minimum,
		 * to know what account and stack to use.  You can also provide an environment, such as
		 * development, test or production, but you will not usually use this -- it mainly exists so
		 * that clients can be configured to connect to localhost for testing and development.
		 */
		fmt("Using stack='%s', account='%s'", STACK, ACCOUNT);
		Endpoints endpoints = new Endpoints(STACK, ACCOUNT);
		
		/* Currently the conjur services are separated by functionality.  A Facade encapsulating all services
		 * is on the roadmap, but for now we'll create actual Authn and Directory instances.
		 */
		
		// To authenticate we need an Authn client, which we create from the endpoints.
		Authn authn = new Authn(endpoints);
		
		// The login method exchanges a conjur username and password for an API key.
		fmt("Login as %s", USERNAME);
		String apiKey = authn.login(USERNAME, PASSWORD);
		puts("OK!");
		
		// The authenticate method exchanges a username and API key for an API token.
		puts("Authenticating...");
		Token token = authn.authenticate(USERNAME, apiKey);
		puts("OK!");
		
		// Once we have a token, we can create an Directory client instance to manipulate users.
		Directory directory = new Directory(endpoints, token);
		
		// Ask for a username
		System.out.print("Enter username to create: ");
		String createUsername = new BufferedReader(new InputStreamReader(System.in)).readLine();
		
		fmt("Attempting to create user %s", createUsername);
		User user = directory.createUser(createUsername);
		puts("OK!");
		
		fmt("created user: %s",user);
		
		// Login and authenticate as the user we created using the 
		// helper methods on the user instance.
		fmt("Login and authenticate as %s", user.getLogin());
		Token userToken = user.authenticate(authn);
		fmt("OK!");
		// ... use the token returned to perform actions as user
		fmt("Got token: %s", userToken.getKey());
	}
	
	// Some sugar
	public static void die(String msg){
		System.err.println(msg);
		System.exit(1);
	}
	public static void puts(String msg){
		System.out.println(msg);
	}
	public static void fmt(String msg, Object...args){
		System.out.println(String.format(msg, args));
	}
}

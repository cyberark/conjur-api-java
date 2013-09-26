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
	
	
	public static void main(String[] args) throws ConjurApiException, IOException {
		// Get the username and password from the cli args.  In actual usage you would get these
		// from your application config.  These must be the username and password of an *existing*
		// Conjur user.
		if(args.length != 2){
			System.err.println("Usage: java -jar basic-usage.jar <username> <password>");
		}
		
		String existingUsername = args[0];
		String password = args[1];
		
		/* To connect to conjur services, you need an Endpoints object that provides the 
		 * endpoints (urls) to which the conjur client connects.  An endpoint needs, at minimum,
		 * to know what account and stack to use.  You can also provide an environment, such as
		 * development, test or production, but you will not usually use this -- it mainly exists so
		 * that clients can be configured to connect to localhost for testing and development.
		 */
		Endpoints endpoints = new Endpoints(STACK, ACCOUNT);
		
		/* Currently the conjur services are separated by functionality.  A Facade encapsulating all services
		 * is on the roadmap, but for now we'll create actual Authn and Directory instances.
		 */
		
		// To authenticate we need an Authn client, which we create from the endpoints.
		Authn authn = new Authn(endpoints);
		
		// The login method exchanges a conjur username and password for an API key.
		String apiKey = authn.login(existingUsername, password);
		
		// The authenticate method exchanges a username and API key for an API token.
		Token token = authn.authenticate(existingUsername, apiKey);
		
		// Once we have a token, we can create an Directory client instance to manipulate users.
		Directory directory = new Directory(endpoints, token);
		
		// Ask for a username
		System.out.print("Enter username to create: ");
		String createUsername = new BufferedReader(new InputStreamReader(System.in)).readLine();
		User user = directory.createUser(createUsername);
		
		System.out.println("created user " + user);
		
		// Login and authenticate as the user we created using the helper methods on the user instance.
		Token userToken = user.authenticate(authn);
		System.out.println(userToken.toJson());
		// ... use userToken to perform actions as user.
	}

}

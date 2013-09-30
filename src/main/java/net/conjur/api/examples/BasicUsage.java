package net.conjur.api.examples;

import java.io.IOException;

import net.conjur.api.Endpoints;
import net.conjur.api.authn.AuthnClient;
import net.conjur.api.authn.Token;
import net.conjur.api.directory.DirectoryClient;
import net.conjur.api.directory.User;
import net.conjur.api.directory.Variable;
import net.conjur.api.exceptions.ConjurApiException;
import net.conjur.api.exceptions.http.ForbiddenException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
	public static final String STACK = "v3";
	
	public static String username;
	public static String password;
	
	public static void configureFromArg(String authn){
		String[] parts = authn.split(":", 2);
		
		if(parts.length < 2){
			die("authn must contain :");
		}
		
		username = parts[0];
		password = parts[1];
	}

    public static void main(String[] args){
        try{
            mainWithExceptions(args);
        }catch(Throwable e){
            System.err.println("BasicUsage threw an exception: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    // We want to print exceptions ourselves, so we wrap this method in the real main()
	public static void mainWithExceptions(String[] args) throws ConjurApiException, IOException {
		
		if(args.length == 1){
			configureFromArg(args[0]);
		}else{
			die("You must provide one argument");
		}
		
		puts("*** BASIC CONJUR USAGE***");
		
		/* To connect to conjur services, you need an Endpoints object that provides the 
		 * endpoints (urls) to which the conjur client connects.  An endpoint needs, at minimum,
		 * to know what account and stack to use.  You can also provide an environment, such as
		 * development, test or production, but you will not usually use this -- it mainly exists so
		 * that clients can be configured to connect to localhost for testing and development.
		 */
		fmt("Using stack='%s', account='%s', username='%s'", STACK, ACCOUNT, username);
		Endpoints endpoints = new Endpoints(STACK, ACCOUNT);
		
		/* Currently the conjur services are separated by functionality.  A Facade encapsulating all services
		 * is on the roadmap, but for now we'll create actual Authn and Directory instances.
		 */
		
		// To authenticate we need an Authn client, which we create from the endpoints.
		AuthnClient authn = new AuthnClient(endpoints);
		
		// The login method exchanges a conjur username and password for an API key.
		fmt("Login as %s", username);
		String apiKey = authn.login(username, password);
		puts("OK!");
		
		// The authenticate method exchanges a username and API key for an API token.
		puts("Authenticating...");
		Token token = authn.authenticate(username, apiKey);
		puts("OK!");
		
		// Once we have a token, we can create an Directory client instance to manipulate users.
		DirectoryClient directory = new DirectoryClient(endpoints, token);
		
		// We can create a unique identifier the same way the command line client does: by 
		// creating a variable without passing an id.
		puts("Generating unique id");
		Variable uniqueIdVariable = directory.createVariable("unique-id");
		String uniqueId = uniqueIdVariable.getId();
		
		fmt("Using namespace %s", uniqueId);
		
		String createUsername = String.format("%s:%s", uniqueId, "alice");
		
		fmt("Attempting to create user %s", createUsername);
		User user = directory.createUser(createUsername);
		fmt("Created user: %s",user);
		
		// Login and authenticate as the user we created using the 
		// helper methods on the user instance.
		fmt("Login and authenticate as %s", user.getLogin());
		
		// Note that the user's apiKey is only available when the user has just been created,
		// not when it is retrieved by getUser()
		Token userToken = authn.authenticate(user.getLogin(), user.getApiKey());
		fmt("OK!");
		
		// Use the token returned to perform actions as user
		// Note that echoing the token to the standard output is bad
		// practice in real life.
		fmt("Got API token for our user: %s", userToken.getKey());
		
		// Create a client that will perform actions as alice
		DirectoryClient userClient = new DirectoryClient(endpoints, userToken);
		
		fmt("Creating a secretQuestion variable for %s", user.getLogin());
		
		Variable secretQuestion = userClient.createVariable("secretQuestion", "application/json");
		fmt("Created secret question %s", secretQuestion);
		
		// In a real application you would probably have a model object, and you might
		// store a list of questions in a single variable.
		JsonObject valueJson = new JsonObject();
		valueJson.addProperty("question", "What is your favorite color?");
		valueJson.addProperty("answer", "yellow");
		String value = new Gson().toJson(valueJson);
		
		fmt("Setting the value to json '%s'", value);
		secretQuestion.addValue(value);
		puts("OK!");
		
		fmt("Fetching variable %s", secretQuestion.getId());
		// Variable also provides a helper method to re-fetch itself.
		Variable fetched = userClient.getVariable(secretQuestion.getId());
		fmt("Fetched %s", fetched);
		
		fmt("Retrieving it's value");
		String fetchedValue = fetched.getValue();
		fmt("Value is %s", fetchedValue);
		
		// Create a new user (using our original account) to demonstrate that alice's variables
		// are only visible to her.
		String bobLogin = String.format("%s:%s", uniqueId, "bob");
		fmt("Creating an evil user with login %s", bobLogin);
		User bob = directory.createUser(bobLogin);
		fmt("Evil user %s created >:-)", bob.getLogin());
		
		// Authenticate as bob and create a directory client to do evil stuff
		// as him.
		fmt("Fetching token for %s", bob.getLogin());
		Token bobToken = authn.authenticate(bob.getLogin(), bob.getApiKey());
		fmt("OK!");
		
		DirectoryClient bobClient = new DirectoryClient(endpoints, bobToken);
		
		// Bob can't see alice's variable
		try{
			puts("Bob is trying to access alice's secret question so he can steal her PHI!");
			String uhOh = bobClient.getVariable(secretQuestion.getId()).getValue();
			fmt("Oh Noes! Bob can see alice's secret question!  Her PHI is his! The secret is '%s'", uhOh);
		}catch(ForbiddenException e){
			fmt("Denied!  Evil prevented!");
		}
		
		puts("*** FIN ***");
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

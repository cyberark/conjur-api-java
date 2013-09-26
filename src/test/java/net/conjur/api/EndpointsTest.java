package net.conjur.api;

import static org.junit.Assert.*;

import org.junit.Test;

public class EndpointsTest {
	private final String stack = "stack";
	private final String account = "the-account";
	
	@Test
	public void staticUrlMethodsReturnLocalhostInTest() {
		String env = "test";
		
		assertEquals( "it should use localhost:5000 for authn in test env", 
				"http://localhost:5000",
				Endpoints.authnUrl(env, stack, account));
		
		assertEquals( "it should use localhost:5100 for authz in test env",
				"http://localhost:5100",
				Endpoints.authzUrl(env, stack, account));
		
		assertEquals( "it should use localhost:5200 for directory in test env",
				"http://localhost:5200",
				Endpoints.directoryUrl(env, stack, account));
		
	}
	
	@Test
	public void staticUrlMethodsReturnEndpointsInProduction(){
		String env = "production";

		assertEquals( "it should use authn-{account}-conjur for authn in production env",
				"https://authn-the-account-conjur.herokuapp.com",
				Endpoints.authnUrl(env, stack, account));
		
		assertEquals( "it should use authz-{stack}-conjur for authz in production env",
				"https://authz-stack-conjur.herokuapp.com",
				Endpoints.authzUrl(env, stack, account));
		
		assertEquals( "it should use core-{account}-conjur for directory in production env",
				"https://core-the-account-conjur.herokuapp.com",
				Endpoints.directoryUrl(env, stack, account));
		
	}
	
	@Test
	public void instanceMethodsReturnLocalhostInTest(){
		Endpoints ep = new Endpoints("test", stack, account);
		assertEquals("http://localhost:5000", ep.authn());
		assertEquals("http://localhost:5100", ep.authz());
		assertEquals("http://localhost:5200", ep.directory());
	}
	
	@Test
	public void instanceMethodsReturnEndpointsInProduction(){
		Endpoints ep = new Endpoints("production", stack, account);

		assertEquals("https://authn-the-account-conjur.herokuapp.com", ep.authn());
		assertEquals("https://authz-stack-conjur.herokuapp.com", ep.authz());
		assertEquals("https://core-the-account-conjur.herokuapp.com", ep.directory());
	}

}

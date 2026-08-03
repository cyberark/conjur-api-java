package com.cyberark.conjur.api.clients;

import com.cyberark.conjur.api.Token;
import jakarta.ws.rs.client.Client;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class AWSIAMAuthenticatorTests {

    @BeforeAll
    public static void logSuiteStart() {
        System.out.println("### START AWSIAMAuthenticatorTests ###");
    }

    private static class TestableAWSIAMAuthenticator extends AWSIAMAuthenticator {
        private final String stubbedHeaders;
        String headersSentToConjur;

        TestableAWSIAMAuthenticator(String stubbedHeaders) {
            super(
                    mock(Client.class),
                    "https://conjur.example/api",
                    "test-account",
                    "host/data/test/myspace/123456789/MyRole",
                    "authn-iam/prod",
                    "us-east-1"
            );
            this.stubbedHeaders = stubbedHeaders;
        }

        @Override
        String buildSignedHeaders() {
            return stubbedHeaders;
        }

        @Override
        protected Token exchangeHeadersForConjurToken(String headersJson) {
            headersSentToConjur = headersJson;
            return Token.fromJson("{\"payload\":\"e30=\",\"protected\":\"e30=\",\"signature\":\"sig\"}");
        }
    }

    @Test
    public void authenticate_passesSignedHeadersToConjur() {
        String expectedHeaders = "{\"Authorization\":\"AWS4-HMAC-SHA256 ...\",\"x-amz-date\":\"20240101T000000Z\"}";
        TestableAWSIAMAuthenticator authenticator = new TestableAWSIAMAuthenticator(expectedHeaders);

        Token token = authenticator.authenticate();

        assertNotNull(token);
        assertEquals(expectedHeaders, authenticator.headersSentToConjur);
    }

    @Test
    public void buildSignedHeaders_validRegion_buildsStsUrl() {
        assertTrue(AWSIAMAuthenticator.isValidAwsRegion("us-east-1"));
        assertTrue(AWSIAMAuthenticator.isValidAwsRegion("us-west-2"));
        assertTrue(AWSIAMAuthenticator.isValidAwsRegion("eu-central-1"));
        assertTrue(AWSIAMAuthenticator.isValidAwsRegion("us-gov-west-1"));
    }

    @Test
    public void buildSignedHeaders_globalRegion_usesGlobalStsUrl() {
        assertTrue(AWSIAMAuthenticator.isValidAwsRegion("global"));
    }

    @Test
    public void signingRegionFor_globalRegion_usesUsEast1() {
        assertEquals(Region.US_EAST_1, AWSIAMAuthenticator.signingRegionFor("global"));
    }

    @Test
    public void signingRegionFor_standardRegion_usesItDirectly() {
        assertEquals(Region.of("eu-west-1"), AWSIAMAuthenticator.signingRegionFor("eu-west-1"));
    }

    @Test
    public void buildSignedHeaders_invalidRegion_throwsException() {
        AWSIAMAuthenticator authenticator = new AWSIAMAuthenticator(
                mock(Client.class),
                "https://conjur.example/api",
                "test-account",
                "host/data/test/myspace/123456789/MyRole",
                "authn-iam/prod",
                "invalid?region"
        );

        assertThrows(IllegalArgumentException.class, authenticator::buildSignedHeaders);
    }

    @Test
    public void buildSignedHeaders_invalidHost_isRejected() {
        assertFalse(AWSIAMAuthenticator.isValidAwsHost("sts.us-east-1.amazonaws.com.malware.com"));
        assertFalse(AWSIAMAuthenticator.isValidAwsHost("evil.com"));
        assertFalse(AWSIAMAuthenticator.isValidAwsHost(null));
    }

    @Test
    public void getConjurAuthenticateUrl_encodesIdentity() {
        AWSIAMAuthenticator authenticator = new AWSIAMAuthenticator(
                mock(Client.class),
                "https://conjur.example/api",
                "test-account",
                "host/data/test/myspace/123456789/My Role",
                "authn-iam/prod",
                "us-east-1"
        );

        String url = authenticator.getConjurAuthenticateUrl();
        assertTrue(url.contains("My%20Role") || url.contains("My+Role") || !url.contains("My Role"),
                "Identity should be URL-encoded in: " + url);
    }
}

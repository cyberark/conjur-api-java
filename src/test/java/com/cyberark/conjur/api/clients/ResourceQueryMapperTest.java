package com.cyberark.conjur.api.clients;

import com.cyberark.conjur.api.ResourceQuery;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceQueryMapperTest {

    private static final URI RESOURCES_URI = URI.create("https://conjur.example.com/resources/myaccount");

    @Test
    void listUriForAllUsesBaseUriWithoutParams() {
        URI uri = ResourceQueryMapper.toListUri(RESOURCES_URI, ResourceQuery.all());

        assertEquals("https://conjur.example.com/resources/myaccount", uri.toString());
    }

    @Test
    void listUriMapsAndEncodesAllParameters() {
        ResourceQuery query = ResourceQuery.builder()
                .kind("variable")
                .search("foo bar/baz")
                .actingAs("myorg:user:alice")
                .limit(10)
                .offset(20)
                .build();

        URI uri = ResourceQueryMapper.toListUri(RESOURCES_URI, query);

        assertEquals(
                "https://conjur.example.com/resources/myaccount?kind=variable&search=foo%20bar%2Fbaz&acting_as=myorg%3Auser%3Aalice&limit=10&offset=20",
                uri.toString());
    }

    @Test
    void countUriUsesCountAndIgnoresLimitOffset() {
        ResourceQuery query = ResourceQuery.builder()
                .kind("variable")
                .search("demo")
                .actingAs("myorg:user:alice")
                .limit(100)
                .offset(200)
                .build();

        URI uri = ResourceQueryMapper.toCountUri(RESOURCES_URI, query);

        assertEquals(
                "https://conjur.example.com/resources/myaccount?count=true&kind=variable&search=demo&acting_as=myorg%3Auser%3Aalice",
                uri.toString());
    }

    @Test
    void countUriWithAllOnlyAddsCountFlag() {
        URI uri = ResourceQueryMapper.toCountUri(RESOURCES_URI, ResourceQuery.all());

        assertEquals("https://conjur.example.com/resources/myaccount?count=true", uri.toString());
    }
}


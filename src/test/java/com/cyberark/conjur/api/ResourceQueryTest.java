package com.cyberark.conjur.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResourceQueryTest {

    @Test
    void allCreatesEmptyQuery() {
        ResourceQuery query = ResourceQuery.all();

        assertNull(query.getKind());
        assertNull(query.getSearch());
        assertNull(query.getActingAs());
        assertNull(query.getLimit());
        assertNull(query.getOffset());
    }

    @Test
    void builderMapsAllFields() {
        ResourceQuery query = ResourceQuery.builder()
                .kind("variable")
                .search("demo")
                .actingAs("myorg:user:alice")
                .limit(25)
                .offset(50)
                .build();

        assertEquals("variable", query.getKind());
        assertEquals("demo", query.getSearch());
        assertEquals("myorg:user:alice", query.getActingAs());
        assertEquals(Integer.valueOf(25), query.getLimit());
        assertEquals(Integer.valueOf(50), query.getOffset());
    }
}


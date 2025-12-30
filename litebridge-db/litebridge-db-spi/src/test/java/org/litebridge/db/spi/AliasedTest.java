package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AliasedTest {

    @Test
    void name() {
        // Given
        final Aliased aliased = new Aliased("testName");

        // When
        final String result = aliased.name();

        // Then
        assertEquals("testName", result);
    }

    @Test
    void alias() {
        // Given
        final Aliased aliased = new Aliased("testName", "testAlias");

        // When
        final String result = aliased.alias();

        // Then
        assertEquals("testAlias", result);
    }

    @Test
    void as() {
        // Given
        final Aliased aliased = new Aliased("testName");

        // When
        final Aliased result = aliased.as("testAlias");

        // Then
        assertEquals("testName", result.name());
        assertEquals("testAlias", result.alias());
    }

    @Test
    void a() {
        // When
        final Aliased result = Aliased.a("testName");

        // Then
        assertEquals("testName", result.name());
        assertNull(result.alias());
    }

    @Test
    void setAlias() {
        // Given
        final Aliased aliased = new Aliased("testName");

        // When
        aliased.setAlias("testAlias");

        // Then
        assertEquals("testName", aliased.name());
        assertEquals("testAlias", aliased.alias());
    }
}
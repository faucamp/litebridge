package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void aliasOrName_returnsAliasWhenPresent() {
        // Given
        final Aliased aliased = new Aliased("testName", "testAlias");

        // When
        final String result = aliased.aliasOrName();

        // Then
        assertEquals("testAlias", result);
    }

    @Test
    void aliasOrName_returnsNameWhenAliasMissing() {
        // Given
        final Aliased aliased = new Aliased("testName");

        // When
        final String result = aliased.aliasOrName();

        // Then
        assertEquals("testName", result);
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
    void setAlias() {
        // Given
        final Aliased aliased = new Aliased("testName");

        // When
        aliased.setAlias("testAlias");

        // Then
        assertEquals("testName", aliased.name());
        assertEquals("testAlias", aliased.alias());
    }

    @Test
    void equals_true() {
        // Given
        final Aliased aliased1 = new Aliased("testName", "testAlias");
        final Aliased aliased2 = new Aliased("testName", "testAlias");

        // When
        final boolean result = aliased1.equals(aliased2);

        // Then
        assertTrue(result);
    }

    @Test
    void equals_false_name() {
        // Given
        final Aliased aliased1 = new Aliased("testName", "testAlias");
        final Aliased aliased2 = new Aliased("otherName", "testAlias");

        // When
        final boolean result = aliased1.equals(aliased2);

        // Then
        assertFalse(result);
    }

    @Test
    void equals_false_alias() {
        // Given
        final Aliased aliased1 = new Aliased("testName", "testAlias");
        final Aliased aliased2 = new Aliased("testName", "otherAlias");

        // When
        final boolean result = aliased1.equals(aliased2);

        // Then
        assertFalse(result);
    }

    @Test
    void equals_false_differentType() {
        // Given
        final Aliased aliased = new Aliased("testName", "testAlias");
        final Object other = new Object();

        // When
        final boolean result = aliased.equals(other);

        // Then
        assertFalse(result);
    }

    @Test
    void equalsIgnoreAlias_true() {
        // Given
        final Aliased aliased1 = new Aliased("testName", "testAlias");
        final Aliased aliased2 = new Aliased("testName", "otherAlias");

        // When
        final boolean result = aliased1.equalsIgnoreAlias(aliased2);

        // Then
        assertTrue(result);
    }

    @Test
    void equalsIgnoreAlias_false() {
        // Given
        final Aliased aliased1 = new Aliased("testName", "testAlias");
        final Aliased aliased2 = new Aliased("otherName", "testAlias");

        // When
        final boolean result = aliased1.equalsIgnoreAlias(aliased2);

        // Then
        assertFalse(result);
    }

    @Test
    void hashCode_sameWhenEqual() {
        // Given
        final Aliased aliased1 = new Aliased("testName", "testAlias");
        final Aliased aliased2 = new Aliased("testName", "testAlias");

        // When
        final int result1 = aliased1.hashCode();
        final int result2 = aliased2.hashCode();

        // Then
        assertEquals(result1, result2);
    }

    @Test
    void hashCode_differsWhenAliasDiffers() {
        // Given
        final Aliased aliased1 = new Aliased("testName", "testAlias");
        final Aliased aliased2 = new Aliased("testName", "otherAlias");

        // When
        final int result1 = aliased1.hashCode();
        final int result2 = aliased2.hashCode();

        // Then
        assertFalse(result1 == result2);
    }
}
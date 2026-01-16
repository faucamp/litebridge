package org.litebridge.orm.api.select.model;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.OrderBy;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrderBySpecTest {

    @Test
    void columns_single() {
        // Given
        final OrderBySpec orderBySpec = new OrderBySpec(new String[] {"TEST_COLUMN"});

        // When
        final String[] result = orderBySpec.columns();

        // Then
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("TEST_COLUMN", result[0]);
    }

    @Test
    void columns_multiple() {
        // Given
        final OrderBySpec orderBySpec = new OrderBySpec(new String[] {"TEST_COLUMN1", "TEST_COLUMN2"});

        // When
        final String[] result = orderBySpec.columns();

        // Then
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("TEST_COLUMN1", result[0]);
        assertEquals("TEST_COLUMN2", result[1]);
    }

    @Test
    void isAsc_true() {
        // Given
        final OrderBySpec orderBySpec = new OrderBySpec(new String[] {"TEST_COLUMN1", "TEST_COLUMN2"});
        orderBySpec.setAsc(true);

        // When
        final boolean result = orderBySpec.isAsc();

        // Then
        assertTrue(result);
    }

    @Test
    void isAsc_false() {
        // Given
        final OrderBySpec orderBySpec = new OrderBySpec(new String[] {"TEST_COLUMN1", "TEST_COLUMN2"});
        orderBySpec.setAsc(false);

        // When
        final boolean result = orderBySpec.isAsc();

        // Then
        assertFalse(result);
    }

    @Test
    void isAsc_notSet() {
        // Given
        final OrderBySpec orderBySpec = new OrderBySpec(new String[] {"TEST_COLUMN1", "TEST_COLUMN2"});

        // When
        final boolean result = orderBySpec.isAsc();

        // Then
        assertTrue(result);
    }

    @Test
    void toOrderBys() {
        // Given
        final OrderBySpec orderBySpec = new OrderBySpec(new String[] {"TEST_COLUMN1", "TEST_COLUMN2"});

        // When
        final List<OrderBy> result = orderBySpec.toOrderBys();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(new OrderBy("TEST_COLUMN1", true), result.get(0));
        assertEquals(new OrderBy("TEST_COLUMN2", true), result.get(1));
    }
}
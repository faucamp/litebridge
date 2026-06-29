package org.litebridgedb.orm.api.select.model;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.impl.function.SelectColumn;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderBySpecTest {

    @Test
    void expressions_single() {
        // Given
        final SelectColumnSpec selectColumnSpec = new SelectColumnSpec(new Column(new Table("TEST_TABLE"), "TEST_COLUMN"));
        final OrderBySpec orderBySpec = new OrderBySpec(List.of(selectColumnSpec));

        // When
        final List<ExpressionSpec> result = orderBySpec.expressions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(selectColumnSpec, result.getFirst());
    }

    @Test
    void expressions_multiple() {
        // Given
        final SelectColumnSpec selectColumnSpec1 = new SelectColumnSpec(new Column(new Table("TEST_TABLE"), "TEST_COLUMN1"));
        final SelectColumnSpec selectColumnSpec2 = new SelectColumnSpec(new Column(new Table("TEST_TABLE"), "TEST_COLUMN2"));
        final OrderBySpec orderBySpec = new OrderBySpec(List.of(selectColumnSpec1, selectColumnSpec2));

        // When
        final List<ExpressionSpec> result = orderBySpec.expressions();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(selectColumnSpec1, result.getFirst());
        assertEquals(selectColumnSpec2, result.get(1));
    }

    @Test
    void isAsc_true() {
        // Given
        final SelectColumnSpec selectColumnSpec1 = new SelectColumnSpec(new Column(new Table("TEST_TABLE"), "TEST_COLUMN1"));
        final SelectColumnSpec selectColumnSpec2 = new SelectColumnSpec(new Column(new Table("TEST_TABLE"), "TEST_COLUMN2"));
        final OrderBySpec orderBySpec = new OrderBySpec(List.of(selectColumnSpec1, selectColumnSpec2));
        orderBySpec.setAsc(true);

        // When
        final boolean result = orderBySpec.isAsc();

        // Then
        assertTrue(result);
    }

    @Test
    void isAsc_false() {
        // Given
        final SelectColumnSpec selectColumnSpec1 = new SelectColumnSpec(new Column(new Table("TEST_TABLE"), "TEST_COLUMN1"));
        final SelectColumnSpec selectColumnSpec2 = new SelectColumnSpec(new Column(new Table("TEST_TABLE"), "TEST_COLUMN2"));
        final OrderBySpec orderBySpec = new OrderBySpec(List.of(selectColumnSpec1, selectColumnSpec2));
        orderBySpec.setAsc(false);

        // When
        final boolean result = orderBySpec.isAsc();

        // Then
        assertFalse(result);
    }

    @Test
    void isAsc_notSet() {
        // Given
        final SelectColumnSpec selectColumnSpec1 = new SelectColumnSpec(new Column(new Table("TEST_TABLE"), "TEST_COLUMN1"));
        final SelectColumnSpec selectColumnSpec2 = new SelectColumnSpec(new Column(new Table("TEST_TABLE"), "TEST_COLUMN2"));
        final OrderBySpec orderBySpec = new OrderBySpec(List.of(selectColumnSpec1, selectColumnSpec2));

        // When
        final boolean result = orderBySpec.isAsc();

        // Then
        assertTrue(result);
    }
}
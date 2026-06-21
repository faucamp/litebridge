package org.litebridgedb.db.spi.query;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.expression.LiteralExpression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConditionTest {

    @Test
    void constructor() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "TEST_COLUMN");
        final Operator operator = Operator.EQ;
        final Object value = "testValue";

        // When
        final Condition result = new Condition(column, operator, new LiteralExpression(value));

        // Then
        assertEquals(column, result.column());
        assertEquals(operator, result.operator());
        assertEquals(value, ((LiteralExpression) result.value()).value());
    }

    @Test
    void constructor_columnOperator_IS_NULL() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "TEST_COLUMN");
        final Operator operator = Operator.IS_NULL;

        // When
        final Condition result = new Condition(column, operator);

        // Then
        assertNotNull(result);
        assertEquals(column, result.column());
        assertEquals(operator, result.operator());
        assertNull(result.value());
    }

    @Test
    void constructor_columnOperator_IS_NOT_NULL() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "TEST_COLUMN");
        final Operator operator = Operator.IS_NOT_NULL;

        // When
        final Condition result = new Condition(column, operator);

        // Then
        assertNotNull(result);
        assertEquals(column, result.column());
        assertEquals(operator, result.operator());
        assertNull(result.value());
    }

    @Test
    void constructor_columnOperator_invalidOperator() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "TEST_COLUMN");
        final Operator operator = Operator.NEQ;

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> new Condition(column, operator));
    }
}
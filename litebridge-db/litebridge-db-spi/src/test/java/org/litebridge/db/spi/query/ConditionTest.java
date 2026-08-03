package org.litebridge.db.spi.query;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.ColumnExpressionTest;
import org.litebridge.db.spi.expression.LiteralExpression;

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
        final Condition result = new Condition(ColumnExpressionTest.select(column), operator, new LiteralExpression(value));

        // Then
        assertEquals(column, (((ColumnExpressionTest.SelectColumnExpression) result.lhs()).column()));
        assertEquals(operator, result.operator());
        assertEquals(value, ((LiteralExpression) result.rhs()).value());
    }

    @Test
    void constructor_columnOperator_IS_NULL() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "TEST_COLUMN");
        final Operator operator = Operator.IS_NULL;

        // When
        final Condition result = new Condition(ColumnExpressionTest.select(column), operator);

        // Then
        assertNotNull(result);
        assertEquals(column, (((ColumnExpressionTest.SelectColumnExpression) result.lhs()).column()));
        assertEquals(operator, result.operator());
        assertNull(result.rhs());
    }

    @Test
    void constructor_columnOperator_IS_NOT_NULL() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "TEST_COLUMN");
        final Operator operator = Operator.IS_NOT_NULL;

        // When
        final Condition result = new Condition(ColumnExpressionTest.select(column), operator);

        // Then
        assertNotNull(result);
        assertEquals(column, (((ColumnExpressionTest.SelectColumnExpression) result.lhs()).column()));
        assertEquals(operator, result.operator());
        assertNull(result.rhs());
    }

    @Test
    void constructor_columnOperator_invalidOperator() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "TEST_COLUMN");
        final Operator operator = Operator.NEQ;

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> new Condition(ColumnExpressionTest.select(column), operator));
    }
}
//package org.litebridge.db.spi.query;
//
//import org.junit.jupiter.api.Test;
//import org.litebridge.db.spi.Column;
//import org.litebridge.db.spi.Table;
//import org.litebridge.db.spi.expression.ColumnExpressionTest;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertSame;
//
//class OrderByTest {
//
//    @Test
//    void constructor() {
//        // Given
//        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
//        final Column column = new Column(table, "TEST_COLUMN");
//        final ColumnExpressionTest.SelectColumnExpression selectColumnExpression = new ColumnExpressionTest.SelectColumnExpression(column);
//        final boolean ascending = false;
//
//        // When
//        final OrderBy orderBy = new OrderBy(selectColumnExpression, ascending);
//
//        // Then
//        assertSame(selectColumnExpression, orderBy.expression());
//        assertEquals(ascending, orderBy.asc());
//    }
//}
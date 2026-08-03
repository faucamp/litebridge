package org.litebridge.db.spi.impl.function;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;
import org.litebridge.db.spi.query.Select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CoreFunctionExpressionsTest {

    private final ColumnIdentifierGenerator generator = new ColumnIdentifierGenerator();
    private final Select select = mock(Select.class);

    @Test
    void selectColumn_toSql() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final SelectColumn selectColumn = new SelectColumn(column, generator);

        // When
        final String sql = selectColumn.toSql(select, ClauseType.SELECT);

        // Then
        assertEquals("TEST.VAL", sql);
    }

    @Test
    void selectColumn_toSql_where() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final SelectColumn selectColumn = new SelectColumn(column, generator);

        // When
        final String sql = selectColumn.toSql(select, ClauseType.WHERE);

        // Then
        assertEquals("TEST.VAL", sql);
    }

    @Test
    void selectColumn_toSql_withParent() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final SelectColumn selectColumn = new SelectColumn(column, generator);
        final org.litebridge.db.spi.expression.DelegateExpression parent = mock(org.litebridge.db.spi.expression.DelegateExpression.class);

        // When
        final String sql = selectColumn.toSql(select, ClauseType.SELECT, parent);

        // Then
        assertEquals("TEST.VAL", sql);
    }

    @Test
    void selectReferenceImpl_toSql() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final SelectReferenceImpl reference = new SelectReferenceImpl(column, new ColumnIdentifierGenerator());

        // When
        final String sql = reference.toSql(select, ClauseType.SELECT);

        // Then
        assertEquals("TEST.VAL", sql);
    }

    @Test
    void selectReferenceImpl_toString() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final SelectReferenceImpl reference = new SelectReferenceImpl(column, new ColumnIdentifierGenerator());

        // When
        final String str = reference.toString();

        // Then
        assertTrue(str.contains("VAL"));
    }

    @Test
    void aliasedDelegateColumnExpression_toSql() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final ColumnExpression target = mock(ColumnExpression.class);
        when(target.column()).thenReturn(column);
        final DelegateColumnExpressionImpl expression = new DelegateColumnExpressionImpl(target, generator);

        // When
        final String sql = expression.toSql(select, ClauseType.SELECT);

        // Then
        assertEquals("TEST.VAL", sql);
    }

    @Test
    void aliasedDelegateColumnExpression_toSqlWithAliasSet() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL", "v");
        final ColumnExpression target = mock(ColumnExpression.class);
        when(target.column()).thenReturn(column);
        final DelegateColumnExpressionImpl expression = new DelegateColumnExpressionImpl(target, generator);

        // When
        final String sql = expression.toSql(select, ClauseType.SELECT);

        // Then
        assertEquals("TEST.VAL AS v", sql);
    }

    @Test
    void aliasedDelegateColumnExpression_toSql_where() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final ColumnExpression target = mock(ColumnExpression.class);
        when(target.column()).thenReturn(column);
        final DelegateColumnExpressionImpl expression = new DelegateColumnExpressionImpl(target, generator);

        // When
        final String sql = expression.toSql(select, ClauseType.WHERE);

        // Then
        assertEquals("TEST.VAL", sql);
    }

    @Test
    void aliasedDelegateColumnExpression_toSql_withParent() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final ColumnExpression target = mock(ColumnExpression.class);
        when(target.column()).thenReturn(column);
        final DelegateColumnExpressionImpl expression = new DelegateColumnExpressionImpl(target, generator);
        final org.litebridge.db.spi.expression.DelegateExpression parent = mock(org.litebridge.db.spi.expression.DelegateExpression.class);

        // When
        final String sql = expression.toSql(select, ClauseType.SELECT, parent);

        // Then
        assertEquals("TEST.VAL", sql);
    }

    @Test
    void sqlFunctionRegistryFactory_registryUsage() {
        // Given
        final SelectSqlGenerator selectSqlGenerator = mock(SelectSqlGenerator.class);
        final SqlFunctionRegistryFactory factory = new SqlFunctionRegistryFactory(generator, selectSqlGenerator);
        final SqlFunctionRegistry registry = factory.create();
        final Column column = new Column(new Table("T"), "C");
        final ColumnExpression colExpr = mock(ColumnExpression.class);
        when(colExpr.column()).thenReturn(column);

        // When & Then
        assertNotNull(registry.select().column().create(column));
        assertNotNull(registry.select().subselect().create(mock(Select.class)));
        assertNotNull(registry.select().literal().create("val"));
        assertNotNull(registry.select().reference().create(column));

        assertNotNull(registry.aggregate().avg().create(colExpr, new Object[0]));
        assertNotNull(registry.aggregate().min().create(colExpr, new Object[0]));
        assertNotNull(registry.aggregate().max().create(colExpr, new Object[0]));
        assertNotNull(registry.aggregate().count());

        assertNotNull(registry.scalar().upper().create(colExpr, new Object[0]));
        assertNotNull(registry.scalar().lower().create(colExpr, new Object[0]));
        assertNotNull(registry.scalar().substring().create(colExpr, new Object[]{1, 5}));
        assertNotNull(registry.scalar().abs().create(colExpr, new Object[0]));

        assertNotNull(registry.date().currentTimestamp());
    }
}

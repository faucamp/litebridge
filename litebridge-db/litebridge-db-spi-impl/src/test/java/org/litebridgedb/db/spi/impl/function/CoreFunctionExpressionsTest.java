package org.litebridgedb.db.spi.impl.function;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.expression.SelectExpression;
import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.impl.sql.SelectSqlGenerator;
import org.litebridgedb.db.spi.query.Select;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CoreFunctionExpressionsTest {

    private final ColumnIdentifierGenerator generator = new ColumnIdentifierGenerator();
    private final Select select = mock(Select.class);

    @Test
    void aliasedColumnExpression_localId() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL", "v");
        final AliasedColumnExpression expression = new AliasedColumnExpression(column, generator);
        final Select selectMock = mock(Select.class);
        final SelectColumn selectColumn = new SelectColumn(column, generator);

        // Case 1: Column is selected
        when(selectMock.expressions()).thenReturn(List.of(selectColumn));
        assertEquals("v", expression.localId(selectMock));

        // Case 2: Column is not selected (different expression in list)
        final Column otherColumn = new Column(new Table("TEST"), "OTHER");
        final SelectColumn otherSelectColumn = new SelectColumn(otherColumn, generator);
        when(selectMock.expressions()).thenReturn(List.of(otherSelectColumn));
        assertEquals("VAL", expression.localId(selectMock));

        // Case 3: List contains non-SelectColumn expressions
        final SelectExpression literal = mock(SelectExpression.class);
        when(selectMock.expressions()).thenReturn(List.of(literal));
        assertEquals("VAL", expression.localId(selectMock));

        // Case 4: Not a Select operation
        assertEquals("VAL", expression.localId(select));

        // Case 5: No alias on expression
        final AliasedColumnExpression noAliasExpr = new AliasedColumnExpression(new Column(new Table("TEST"), "VAL"), generator);
        assertEquals("VAL", noAliasExpr.localId(selectMock));
    }

    @Test
    void aliasedDelegateColumnExpression_localId() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL", "v");
        final ColumnExpression target = mock(ColumnExpression.class);
        when(target.column()).thenReturn(column);
        final AliasedDelegateColumnExpression expression = new AliasedDelegateColumnExpression(target, generator);
        final Select selectMock = mock(Select.class);
        final SelectColumn selectColumn = new SelectColumn(column, generator);
        when(selectMock.expressions()).thenReturn(List.of(selectColumn));

        // When
        final String localIdSelected = expression.localId(selectMock);
        final String localIdNotSelected = expression.localId(select);

        // Then
        assertEquals("v", localIdSelected);
        assertEquals("VAL", localIdNotSelected);
    }

    @Test
    void selectColumn_toSql() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final SelectColumn selectColumn = new SelectColumn(column, generator);

        // When
        final String sql = selectColumn.toSql(select);

        // Then
        assertEquals("VAL", sql);
    }

    @Test
    void selectReferenceImpl_toSql() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final SelectReferenceImpl reference = new SelectReferenceImpl(column, new ColumnIdentifierGenerator());

        // When
        final String sql = reference.toSql(select);

        // Then
        assertEquals("VAL", sql);
    }

    @Test
    void aliasedColumnExpression_toSql() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final AliasedColumnExpression expression = new AliasedColumnExpression(column, generator);

        // When
        final String sql = expression.toSql(select);
        final String sqlWithAlias = expression.toSqlWithAlias(select);

        // Then
        assertEquals("TEST.VAL", sql);
        assertEquals("TEST.VAL", sqlWithAlias); // No alias set on expression
    }

    @Test
    void aliasedColumnExpression_toSqlWithAliasSet() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL", "v");
        final AliasedColumnExpression expression = new AliasedColumnExpression(column, generator);

        // When
        final String sql = expression.toSql(select);
        final String sqlWithAlias = expression.toSqlWithAlias(select);

        // Then
        assertEquals("TEST.VAL", sql);
        assertEquals("TEST.VAL AS v", sqlWithAlias);
    }

    @Test
    void aliasedDelegateColumnExpression_toSql() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final ColumnExpression target = mock(ColumnExpression.class);
        when(target.column()).thenReturn(column);
        final AliasedDelegateColumnExpression expression = new AliasedDelegateColumnExpression(target, generator);

        // When
        final String sql = expression.toSql(select);
        final String sqlWithAlias = expression.toSqlWithAlias(select);

        // Then
        assertEquals("TEST.VAL", sql);
        assertEquals("TEST.VAL", sqlWithAlias);
    }

    @Test
    void aliasedDelegateColumnExpression_toSqlWithAliasSet() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL", "v");
        final ColumnExpression target = mock(ColumnExpression.class);
        when(target.column()).thenReturn(column);
        final AliasedDelegateColumnExpression expression = new AliasedDelegateColumnExpression(target, generator);

        // When
        final String sql = expression.toSql(select);
        final String sqlWithAlias = expression.toSqlWithAlias(select);

        // Then
        assertEquals("TEST.VAL", sql);
        assertEquals("TEST.VAL AS v", sqlWithAlias);
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

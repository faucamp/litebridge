package org.litebridge.db.spi.impl.expression.function;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.impl.expression.SelectColumn;
import org.litebridge.db.spi.impl.expression.SqlFunctionRegistryFactory;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;
import org.litebridge.db.spi.query.Select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CoreFunctionExpressionsTest {

    private final LabelGenerator labelGenerator = new LabelGenerator();
    private final Select select = mock(Select.class);

    @Test
    void selectColumn_toSql() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final SelectColumn selectColumn = new SelectColumn(column, null, null, labelGenerator);

        // When
        final String sql = selectColumn.toSql(select, ClauseType.SELECT);

        // Then
        assertEquals("TEST.VAL", sql);
    }

    @Test
    void selectColumn_toSql_where() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final SelectColumn selectColumn = new SelectColumn(column, null, null, labelGenerator);

        // When
        final String sql = selectColumn.toSql(select, ClauseType.WHERE);

        // Then
        assertEquals("TEST.VAL", sql);
    }

    @Test
    void selectColumn_toSql_withParent() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final SelectColumn selectColumn = new SelectColumn(column, null, null, labelGenerator);
        final org.litebridge.db.spi.expression.DelegateExpression parent = mock(org.litebridge.db.spi.expression.DelegateExpression.class);

        // When
        final String sql = selectColumn.toSql(select, ClauseType.SELECT, parent);

        // Then
        assertEquals("TEST.VAL", sql);
    }

    @Test
    void sqlFunctionRegistryFactory_registryUsage() {
        // Given
        final SelectSqlGenerator selectSqlGenerator = mock(SelectSqlGenerator.class);
        final SqlFunctionRegistryFactory factory = new SqlFunctionRegistryFactory(labelGenerator, selectSqlGenerator);
        final SqlFunctionRegistry registry = factory.create();
        final Column column = new Column(new Table("T"), "C");
        final ColumnExpression colExpr = mock(ColumnExpression.class);
        when(colExpr.column()).thenReturn(column);
        final Object[] aliasArgs = new Object[]{null};

        // When & Then
        assertNotNull(registry.select().column().create(column, null, null));
        assertNotNull(registry.select().subselect().create(mock(Select.class)));
        assertNotNull(registry.select().literal().create("val", "alias"));

        assertNotNull(registry.aggregate().avg().create(colExpr, aliasArgs));
        assertNotNull(registry.aggregate().min().create(colExpr, aliasArgs));
        assertNotNull(registry.aggregate().max().create(colExpr, aliasArgs));
        assertNotNull(registry.aggregate().count());

        assertNotNull(registry.scalar().upper().create(colExpr, aliasArgs));
        assertNotNull(registry.scalar().lower().create(colExpr, aliasArgs));
        assertNotNull(registry.scalar().substring().create(colExpr, 1, 5, null));
        assertNotNull(registry.scalar().abs().create(colExpr, aliasArgs));

        assertNotNull(registry.date().currentTimestamp());
    }
}

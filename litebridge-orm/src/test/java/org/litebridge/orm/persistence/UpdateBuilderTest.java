package org.litebridge.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.update.Update;
import org.litebridge.orm.api.select.ast.ConditionNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.compiler.QueryCompiler;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.ClassFieldAccessorCache;

import java.lang.invoke.MethodHandles;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateBuilderTest {

    @Test
    void where() {
        // Given
        final UpdateBuilder updateBuilder = new UpdateBuilder(ormTable(), mock(LitebridgeContext.class));
        final QueryNode conditionNode = new ConditionNode(null, LogicOperator.NOOP, null, null, Operator.IS_NULL, null);

        // When
        final AbstractStatementBuilder result = updateBuilder.where(conditionNode);

        // Then
        assertSame(updateBuilder, result);
    }

    @Test
    void build() {
        // Given
        final OrmTable ormTable = ormTable();
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final QueryCompiler queryCompiler = mock(QueryCompiler.class);
        when(litebridgeContext.createQueryCompiler()).thenReturn(queryCompiler);
        final UpdateBuilder updateBuilder = new UpdateBuilder(ormTable, litebridgeContext);

        final Column column = new Column(new Table("TEST_TABLE"), "TEST_COLUMN");
        final ConditionNode conditionNode = new ConditionNode(null, LogicOperator.NOOP, null, new SelectColumnSpec(column), Operator.EQ, "test");

        // When
        updateBuilder.where(conditionNode);

        // When
        final PreparedOperation result = updateBuilder.build();

        // Then
        assertNotNull(result);
        assertInstanceOf(Update.class, result.operation());
        final Update update = (Update) result.operation();
        assertEquals(ormTable.getMetaData().toTable(), update.table());
    }

    private static OrmTable ormTable() {
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final Table table = new Table("", "public", "test_table");
        final ColumnMetaData idColumn = new ColumnMetaData(table, "id", false, Types.BIGINT);
        final TableMetaData tableMetaData = new TableMetaData(table, List.of("id"), List.of(idColumn));
        return new OrmTable(TestDto.class, tableMetaData, Map.of(), changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
    }

    private static ColumnMetaData column(final String tableName, final String columnName, final int type) {
        return new ColumnMetaData(new Table("", "public", tableName), columnName, false, type);
    }

    private static class TestDto {
        private Long id;
    }
}
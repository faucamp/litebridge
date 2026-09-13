package org.litebridge.orm.engine.compiler;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.update.Delete;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.DeleteNode;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.mockito.Mockito;

import java.sql.Types;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeleteCompilationContextTest {

    private LitebridgeContext createMockContext() {
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMetaDataCache metadataCache = mock(TableMetaDataCache.class);
        final SelectExpressionMapper expressionMapper = mock(SelectExpressionMapper.class);
        final TypeConverter typeConverter = mock(TypeConverter.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class, Mockito.RETURNS_DEEP_STUBS);

        when(context.tableRegistry()).thenReturn(tableRegistry);
        when(context.tableMetaDataCache()).thenReturn(metadataCache);
        when(context.selectExpressionMapper()).thenReturn(expressionMapper);
        when(context.typeConverter()).thenReturn(typeConverter);
        when(context.sqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        return context;
    }

    @Test
    void constructWithTableNameAndEmptyWhere() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);

        final DeleteNode deleteNode = new DeleteNode(null, "items", null);

        // When
        final DeleteCompilationContext compilationContext = new DeleteCompilationContext(deleteNode, context);
        final Delete operation = compilationContext.toOperation();

        // Then
        assertEquals(table, operation.table());
        assertTrue(operation.where().conditions().isEmpty());
        assertTrue(operation.where().subgroups().isEmpty());
    }

    @Test
    void constructWithDtoClassAndWhereCondition() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("users");
        final OrmTable ormTable = mock(OrmTable.class);
        final ColumnMetaData colMeta = new ColumnMetaData(table, "user_id", true, Types.INTEGER, 0);
        final TableMetaData tableMetaData = new TableMetaData(table, List.of("user_id"), List.of(colMeta));

        when(ormTable.getMetaData()).thenReturn(tableMetaData);
        when(ormTable.columnMetaDataForField("id")).thenReturn(colMeta);
        when(context.tableRegistry().getOrmTableOrThrow(UserDto.class)).thenReturn(ormTable);
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(tableMetaData);
        when(context.typeConverter().convert(1, Types.INTEGER)).thenReturn(1);

        final ColumnExpression colExpr = mock(ColumnExpression.class);
        when(colExpr.column()).thenReturn(colMeta.toColumn());
        when(context.selectExpressionMapper().toSelectExpression(any(), eq(true))).thenReturn(colExpr);

        final DeleteNode deleteNode = new DeleteNode(null, null, UserDto.class);

        // When
        final DeleteCompilationContext compilationContext = new DeleteCompilationContext(deleteNode, context);
        assertNotNull(compilationContext.ensureWhereConditionGroupStack());
        assertSame(compilationContext.ensureWhereConditionGroupStack(), compilationContext.ensureWhereConditionGroupStack());

        final ConditionNode conditionNode = new ConditionNode(null, LogicOperator.AND, "id", null, Operator.EQ, 1);
        compilationContext.addWhereCondition(conditionNode);

        final Delete operation = compilationContext.toOperation();

        // Then
        assertEquals(table, operation.table());
        assertEquals(1, operation.where().conditions().size());
        assertEquals(1, compilationContext.getBindValues().size());
    }

    static class UserDto {
        private Integer id;
    }
}

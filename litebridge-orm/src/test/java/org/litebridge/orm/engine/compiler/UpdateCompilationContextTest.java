package org.litebridge.orm.engine.compiler;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.math.MathOperator;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.Update;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.SetNode;
import org.litebridge.orm.engine.ast.UpdateNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.intent.ExpressionSpecArray;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.mockito.Mockito;

import java.sql.Types;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateCompilationContextTest {

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
    void constructWithTableNameAndSetColumn() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnMetaData col = new ColumnMetaData(table, "name", true, Types.VARCHAR, 100);
        final TableMetaData metaData = new TableMetaData(table, List.of(), List.of(col));

        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(metaData);

        final UpdateNode updateNode = new UpdateNode(null, "items", null);
        final UpdateCompilationContext compilationContext = new UpdateCompilationContext(updateNode, context);

        final SetNode setNode = new SetNode(updateNode, "name", "Alice");
        compilationContext.addSetNode(setNode);

        // When
        final Update operation = compilationContext.toOperation();

        // Then
        assertEquals(table, operation.table());
        assertEquals(1, operation.columns().size());
        assertEquals("name", operation.columns().getFirst().name());
        assertEquals(List.of(new BindValue("Alice", Types.VARCHAR)), compilationContext.getBindValues());
        assertTrue(operation.where().conditions().isEmpty());
    }

    @Test
    void constructWithDtoClassAndSetField() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("users");
        final ColumnMetaData col = new ColumnMetaData(table, "user_name", true, Types.VARCHAR, 100);
        final TableMetaData metaData = new TableMetaData(table, List.of(), List.of(col));

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(ormTable.columnMetaDataForField("name")).thenReturn(col);
        when(context.tableRegistry().getOrmTableOrThrow(UserDto.class)).thenReturn(ormTable);

        final UpdateNode updateNode = new UpdateNode(null, null, UserDto.class);
        final UpdateCompilationContext compilationContext = new UpdateCompilationContext(updateNode, context);

        final SetNode setNode = new SetNode(updateNode, "name", null, "Bob", MathOperator.ADD);
        compilationContext.addSetNode(setNode);

        // When
        final Update operation = compilationContext.toOperation();

        // Then
        assertEquals(table, operation.table());
        assertEquals(1, operation.columns().size());
        assertEquals("user_name", operation.columns().getFirst().name());
        assertEquals(MathOperator.ADD, operation.columns().getFirst().mathOperator());
        assertEquals(List.of(new BindValue("Bob", Types.VARCHAR)), compilationContext.getBindValues());
    }

    @Test
    void setNodeWithColumnExpressionSpecAndQueryField() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("users");
        final ColumnMetaData nameCol = new ColumnMetaData(table, "user_name", true, Types.VARCHAR, 100);
        final ColumnMetaData ageCol = new ColumnMetaData(table, "age", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of(), List.of(nameCol, ageCol));

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(ormTable.columnMetaDataForField("name")).thenReturn(nameCol);
        when(ormTable.columnMetaDataForField("age")).thenReturn(ageCol);
        when(context.tableRegistry().getOrmTableOrThrow(UserDto.class)).thenReturn(ormTable);

        final UpdateNode updateNode = new UpdateNode(null, null, UserDto.class);
        final UpdateCompilationContext compilationContext = new UpdateCompilationContext(updateNode, context);

        final SetNode set1 = new SetNode(updateNode, new SelectColumnSpec(new Column(table, "age")), 25);
        final SetNode set2 = new SetNode(updateNode, new QueryField(UserDto.class, "name"), "Charlie");
        compilationContext.addSetNode(set1);
        compilationContext.addSetNode(set2);

        // When
        final Update operation = compilationContext.toOperation();

        // Then
        assertEquals(2, operation.columns().size());
        assertEquals(2, compilationContext.getBindValues().size());
    }

    @Test
    void setNodeWithUnsupportedExpressionSpecThrowsException() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final TableMetaData metaData = new TableMetaData(table, List.of(), List.of());

        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(metaData);

        final UpdateNode updateNode = new UpdateNode(null, "items", null);
        final UpdateCompilationContext compilationContext = new UpdateCompilationContext(updateNode, context);

        final SetNode setNode = new SetNode(updateNode, new ExpressionSpecArray(new ExpressionSpec[0]), "val");
        compilationContext.addSetNode(setNode);

        // When & Then
        final IllegalStateException ex = assertThrows(IllegalStateException.class, compilationContext::toOperation);
        assertTrue(ex.getMessage().contains("Unsupported expression spec"));
    }

    @Test
    void updateWithWhereConditionAppendsWhereBindValues() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnMetaData nameCol = new ColumnMetaData(table, "name", true, Types.VARCHAR, 100);
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol, nameCol));

        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(metaData);
        when(context.typeConverter().convert(1, Types.INTEGER)).thenReturn(1);

        final ColumnExpression colExpr = mock(ColumnExpression.class);
        when(colExpr.column()).thenReturn(idCol.toColumn());
        when(context.selectExpressionMapper().toSelectExpression(any(), eq(true))).thenReturn(colExpr);

        final UpdateNode updateNode = new UpdateNode(null, "items", null);
        final UpdateCompilationContext compilationContext = new UpdateCompilationContext(updateNode, context);

        assertNotNull(compilationContext.ensureWhereConditionGroupStack());
        assertSame(compilationContext.ensureWhereConditionGroupStack(), compilationContext.ensureWhereConditionGroupStack());

        final SetNode setNode = new SetNode(updateNode, "name", "Alice");
        compilationContext.addSetNode(setNode);

        final ConditionNode conditionNode = new ConditionNode(null, LogicOperator.AND, "id", null, Operator.EQ, 1);
        compilationContext.addCondition(conditionNode);

        // When
        final Update operation = compilationContext.toOperation();

        // Then
        assertEquals(1, operation.where().conditions().size());
        // Set bind value (Alice) + Where bind value (1)
        assertEquals(2, compilationContext.getBindValues().size());
        assertEquals(new BindValue("Alice", Types.VARCHAR), compilationContext.getBindValues().get(0));
        assertEquals(new BindValue(1, Types.INTEGER), compilationContext.getBindValues().get(1));
    }

    static class UserDto {
        private String name;
        private Integer age;
    }
}

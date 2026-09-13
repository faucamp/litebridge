package org.litebridge.orm.engine.compiler;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.LiteralExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.expression.SelectReference;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.expression.SubselectExpression;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.DeleteNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.mockito.Mockito;

import java.sql.Types;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractCompilationContextTest {

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

    private DeleteCompilationContext createContext(final LitebridgeContext context, final Table table) {
        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        final DeleteNode node = new DeleteNode(null, "items", null);
        return new DeleteCompilationContext(node, context);
    }

    @Test
    void defaultResolveAliasMethodsDoNotAlterInputs() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final DeleteCompilationContext compilationContext = createContext(context, table);

        final ColumnMetaData meta = new ColumnMetaData(table, "name", true, Types.VARCHAR, 255);
        final Column col = new Column(table, "name");
        final ExpressionSpec spec = new SelectColumnSpec(col);

        // When & Then
        assertEquals(col, compilationContext.resolveAlias(table, meta));
        assertSame(col, compilationContext.resolveAlias(table, col));
        assertSame(spec, compilationContext.resolveAlias(spec));
    }

    @Test
    void toConditionGroupWithEmptySpecReturnsEmptyGroup() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final DeleteCompilationContext compilationContext = createContext(context, table);
        final ConditionGroupSpec groupSpec = new ConditionGroupSpec();

        // When
        final ConditionGroup group = compilationContext.toConditionGroup(groupSpec, null, table);

        // Then
        assertTrue(group.conditions().isEmpty());
        assertTrue(group.subgroups().isEmpty());
    }

    @Test
    void toConditionGroupWithNestedSubgroups() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final DeleteCompilationContext compilationContext = createContext(context, table);

        final ConditionGroupSpec groupSpec = new ConditionGroupSpec();
        groupSpec.newCondition(LogicOperator.AND, "id", null, Operator.IS_NULL, null);

        final ConditionGroupSpec subSpec = groupSpec.newSubgroup(LogicOperator.OR).conditionGroupSpec();
        subSpec.newCondition(LogicOperator.AND, "status", null, Operator.IS_NOT_NULL, null);

        // When
        final ConditionGroup group = compilationContext.toConditionGroup(groupSpec, null, table);

        // Then
        assertEquals(1, group.conditions().size());
        assertEquals(LogicOperator.AND, group.conditions().getFirst().logicOperator());
        assertEquals(1, group.subgroups().size());
        assertEquals(LogicOperator.OR, group.subgroups().getFirst().logicOperator());
        assertEquals(1, group.subgroups().getFirst().conditionGroup().conditions().size());
    }

    @Test
    void toConditionWithLhsExpressionMultipleResolutionThrowsException() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final DeleteCompilationContext compilationContext = createContext(context, table);

        final ExpressionSpec lhsExpr = new SelectColumnSpec(new Column(table, "field1"));
        final ConditionSpec conditionSpec = new ConditionSpec(null, lhsExpr, Operator.EQ, "value");

        when(context.selectExpressionMapper().resolveProtoExpression(lhsExpr, null, table, ClauseType.WHERE))
                .thenReturn(List.of(new SelectColumnSpec(new Column(table, "a")), new SelectColumnSpec(new Column(table, "b"))));

        // When & Then
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> compilationContext.toCondition(conditionSpec, null, table));
        assertTrue(ex.getMessage().contains("Expected exactly one LHS expression spec"));
    }

    @Test
    void toConditionWithLhsExpressionSuccess() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final DeleteCompilationContext compilationContext = createContext(context, table);

        final ExpressionSpec lhsExpr = new SelectColumnSpec(new Column(table, "field1"));
        final ExpressionSpec resolvedSpec = new SelectColumnSpec(new Column(table, "field1"));
        final SelectExpression lhsSelectExpr = mock(SelectExpression.class);
        final ConditionSpec conditionSpec = new ConditionSpec(null, lhsExpr, Operator.IS_NULL, null);

        when(context.selectExpressionMapper().resolveProtoExpression(lhsExpr, null, table, ClauseType.WHERE))
                .thenReturn(List.of(resolvedSpec));
        when(context.selectExpressionMapper().toSelectExpression(resolvedSpec, true)).thenReturn(lhsSelectExpr);

        // When
        final Condition condition = compilationContext.toCondition(conditionSpec, null, table);

        // Then
        assertSame(lhsSelectExpr, condition.lhs());
        assertEquals(Operator.IS_NULL, condition.operator());
        assertNull(condition.rhs());
    }

    @Test
    void toConditionWithOrmTableField() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final DeleteCompilationContext compilationContext = createContext(context, table);

        final OrmTable ormTable = mock(OrmTable.class);
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "field1", true, Types.VARCHAR, 50);
        when(ormTable.columnMetaDataForField("field1")).thenReturn(columnMetaData);

        final SelectExpression lhsSelectExpr = mock(SelectExpression.class);
        when(context.selectExpressionMapper().toSelectExpression(any(), eq(true))).thenReturn(lhsSelectExpr);

        final ConditionSpec conditionSpec = new ConditionSpec("field1", null, Operator.IS_NOT_NULL, null);

        // When
        final Condition condition = compilationContext.toCondition(conditionSpec, ormTable, table);

        // Then
        assertSame(lhsSelectExpr, condition.lhs());
        assertEquals(Operator.IS_NOT_NULL, condition.operator());
        assertNull(condition.rhs());
    }

    @Test
    void toConditionWithSubselectRhs() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final DeleteCompilationContext compilationContext = createContext(context, table);

        final QueryNode subselectNode = new DeleteNode(null, "sub", null);
        final QueryCompiler queryCompiler = mock(QueryCompiler.class);
        when(context.createQueryCompiler()).thenReturn(queryCompiler);

        final Select subselect = mock(Select.class);
        final BindValue subBind = new BindValue("sub", Types.VARCHAR);
        final PreparedOperation subPrepared = new PreparedOperation(subselect, List.of(subBind));
        when(queryCompiler.compile(subselectNode)).thenReturn(subPrepared);

        final SubselectExpression subselectExpression = mock(SubselectExpression.class);
        when(context.sqlFunctionRegistry().select().subselect().create(subselect)).thenReturn(subselectExpression);

        final SelectExpression lhsSelectExpr = mock(SelectExpression.class);
        when(context.selectExpressionMapper().toSelectExpression(any(), eq(true))).thenReturn(lhsSelectExpr);

        final ConditionSpec conditionSpec = new ConditionSpec("id", null, Operator.IN, subselectNode);

        // When
        final Condition condition = compilationContext.toCondition(conditionSpec, null, table);

        // Then
        assertSame(lhsSelectExpr, condition.lhs());
        assertEquals(Operator.IN, condition.operator());
        assertSame(subselectExpression, condition.rhs());
        assertEquals(List.of(subBind), compilationContext.getBindValues());
    }

    @Test
    void toConditionWithRhsExpressionSpecMultipleResolutionThrowsException() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final DeleteCompilationContext compilationContext = createContext(context, table);

        final ExpressionSpec rhsExpr = new SelectColumnSpec(new Column(table, "val"));
        final ConditionSpec conditionSpec = new ConditionSpec("id", null, Operator.EQ, rhsExpr);

        when(context.selectExpressionMapper().resolveProtoExpression(rhsExpr, null, table, ClauseType.WHERE))
                .thenReturn(List.of(new SelectColumnSpec(new Column(table, "a")), new SelectColumnSpec(new Column(table, "b"))));

        // When & Then
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> compilationContext.toCondition(conditionSpec, null, table));
        assertTrue(ex.getMessage().contains("Expected exactly one RHS expression spec"));
    }

    @Test
    void toConditionWithRhsExpressionSpecSuccess() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final DeleteCompilationContext compilationContext = createContext(context, table);

        final ExpressionSpec rhsExpr = new SelectColumnSpec(new Column(table, "val"));
        final ExpressionSpec resolvedRhs = new SelectColumnSpec(new Column(table, "val"));
        final SelectExpression rhsSelectExpr = mock(SelectExpression.class);
        final ConditionSpec conditionSpec = new ConditionSpec("id", null, Operator.EQ, rhsExpr);

        when(context.selectExpressionMapper().resolveProtoExpression(rhsExpr, null, table, ClauseType.WHERE))
                .thenReturn(List.of(resolvedRhs));
        when(context.selectExpressionMapper().toSelectExpression(resolvedRhs, true)).thenReturn(rhsSelectExpr);

        // When
        final Condition condition = compilationContext.toCondition(conditionSpec, null, table);

        // Then
        assertSame(rhsSelectExpr, condition.rhs());
    }

    @Test
    void toConditionWithReferencedColumnRhs() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final DeleteCompilationContext compilationContext = createContext(context, table);

        final Column refColumn = new Column(new Table("other"), "ref_id");
        final SelectReference selectReference = mock(SelectReference.class);
        when(context.sqlFunctionRegistry().select().reference().create(refColumn)).thenReturn(selectReference);

        final ConditionSpec conditionSpec = new ConditionSpec("id", null, Operator.EQ, refColumn);

        // When
        final Condition condition = compilationContext.toCondition(conditionSpec, null, table);

        // Then
        assertSame(selectReference, condition.rhs());
    }

    @Test
    void toConditionWithUsingOperator() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final DeleteCompilationContext compilationContext = createContext(context, table);

        final LiteralExpression literalExpr = mock(LiteralExpression.class);
        when(context.sqlFunctionRegistry().select().literal().create("customLiteral", true)).thenReturn(literalExpr);

        final ConditionSpec conditionSpec = new ConditionSpec("id", null, Operator.USING, "customLiteral");

        // When
        final Condition condition = compilationContext.toCondition(conditionSpec, null, table);

        // Then
        assertEquals(Operator.USING, condition.operator());
        assertSame(literalExpr, condition.rhs());
    }

    @Test
    void toConditionWithColumnExpressionLhsSingleBindValue() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final DeleteCompilationContext compilationContext = createContext(context, table);

        final Column column = new Column(table, "name");
        final ColumnExpression columnExpr = mock(ColumnExpression.class);
        when(columnExpr.column()).thenReturn(column);
        when(context.selectExpressionMapper().toSelectExpression(any(), eq(true))).thenReturn(columnExpr);

        final ColumnMetaData meta = new ColumnMetaData(table, "name", true, Types.VARCHAR, 255);
        final TableMetaData tableMetaData = new TableMetaData(table, List.of(), List.of(meta));
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(tableMetaData);
        when(context.typeConverter().convert("Bob", Types.VARCHAR)).thenReturn("Bob");

        final ConditionSpec conditionSpec = new ConditionSpec("name", null, Operator.EQ, "Bob");

        // When
        final Condition condition = compilationContext.toCondition(conditionSpec, null, table);

        // Then
        assertNotNull(condition);
        assertEquals(1, compilationContext.getBindValues().size());
        assertEquals(new BindValue("Bob", Types.VARCHAR), compilationContext.getBindValues().getFirst());
    }

    @Test
    void toConditionWithColumnExpressionLhsCollectionBindValues() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final DeleteCompilationContext compilationContext = createContext(context, table);

        final Column column = new Column(table, "age");
        final ColumnExpression columnExpr = mock(ColumnExpression.class);
        when(columnExpr.column()).thenReturn(column);
        when(context.selectExpressionMapper().toSelectExpression(any(), eq(true))).thenReturn(columnExpr);

        final ColumnMetaData meta = new ColumnMetaData(table, "age", true, Types.INTEGER, 0);
        final TableMetaData tableMetaData = new TableMetaData(table, List.of(), List.of(meta));
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(tableMetaData);
        when(context.typeConverter().convert(10, Types.INTEGER)).thenReturn(10);
        when(context.typeConverter().convert(20, Types.INTEGER)).thenReturn(20);

        final ConditionSpec conditionSpec = new ConditionSpec("age", null, Operator.IN, List.of(10, 20));

        // When
        final Condition condition = compilationContext.toCondition(conditionSpec, null, table);

        // Then
        assertNotNull(condition);
        assertEquals(2, compilationContext.getBindValues().size());
        assertEquals(new BindValue(10, Types.INTEGER), compilationContext.getBindValues().get(0));
        assertEquals(new BindValue(20, Types.INTEGER), compilationContext.getBindValues().get(1));
    }

    @Test
    void createBindValuesWithNonColumnExpressionLhsNonNullAndNull() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final DeleteCompilationContext compilationContext = createContext(context, table);

        final SelectExpression nonColumnExpr = mock(SelectExpression.class);
        when(context.typeConverter().getSqlDataType(String.class)).thenReturn(Types.VARCHAR);

        // When rawValue is non-null
        final List<BindValue> binds1 = compilationContext.createBindValues(nonColumnExpr, "hello",
                context.tableMetaDataCache(), context.typeConverter());

        // Then
        assertEquals(List.of(new BindValue("hello", Types.VARCHAR)), binds1);

        // When rawValue is null
        final List<BindValue> binds2 = compilationContext.createBindValues(nonColumnExpr, null,
                context.tableMetaDataCache(), context.typeConverter());

        // Then
        assertEquals(List.of(new BindValue(null, Types.NULL)), binds2);
    }
}

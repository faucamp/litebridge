package org.litebridge.orm.engine.compiler;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.alias.DefaultAliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.expression.SelectReference;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.Select;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionJoinUsingNode;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.ConditionWithIdNode;
import org.litebridge.orm.engine.ast.GroupByNode;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.LimitNode;
import org.litebridge.orm.engine.ast.OrderByNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.Fn;
import org.litebridge.orm.expression.intent.ConvertSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.MappedManyToMany;
import org.litebridge.orm.persistence.MappedOneToMany;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.DefaultAliasGenerator;
import org.litebridge.tracking.FieldAccessor;
import org.mockito.Mockito;

import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SelectCompilationContextTest {

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
        when(context.aliasGenerator()).thenReturn(new DefaultAliasGenerator(new DefaultAliasTransformer()));
        return context;
    }

    @Test
    void constructWithDtoClassSelectAll() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("users");
        final ColumnMetaData idCol = new ColumnMetaData(table, "user_id", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of("user_id"), List.of(idCol));

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(ormTable.mappedColumns()).thenReturn(List.of(idCol));
        when(context.tableRegistry().getOrmTableOrThrow(UserDto.class)).thenReturn(ormTable);

        final SelectNode selectNode = new SelectNode(null, UserDto.class, null, null, null, null);

        // When
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, context);
        final Select select = compilationContext.toOperation();

        // Then
        assertNotNull(select);
        assertEquals(1, select.expressions().size());
    }

    @Test
    void constructWithDtoClassSpecificColumns() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("users");
        final ColumnMetaData idCol = new ColumnMetaData(table, "user_id", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of("user_id"), List.of(idCol));

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(ormTable.columnMetaDataForField("id")).thenReturn(idCol);
        when(context.tableRegistry().getOrmTableOrThrow(UserDto.class)).thenReturn(ormTable);

        final SelectNode selectNode = new SelectNode(null, UserDto.class, null, new String[]{"id"}, null, null);

        // When
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, context);
        final Select select = compilationContext.toOperation();

        // Then
        assertNotNull(select);
        assertEquals(1, select.expressions().size());
    }

    @Test
    void constructWithDtoClassExpressions() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("users");
        final ColumnMetaData idCol = new ColumnMetaData(table, "user_id", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of("user_id"), List.of(idCol));

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(context.tableRegistry().getOrmTableOrThrow(UserDto.class)).thenReturn(ormTable);

        final SelectColumnSpec spec = new SelectColumnSpec(new Column(table, "user_id"));
        when(context.selectExpressionMapper().resolveProtoExpression(any(), any(), any(), eq(ClauseType.SELECT)))
                .thenReturn(List.of(spec));
        when(context.selectExpressionMapper().toSelectExpression(any(), eq(false)))
                .thenReturn(mock(SelectExpression.class));

        final SelectNode selectNode = new SelectNode(null, UserDto.class, null, null, new ExpressionSpec[]{spec}, null);

        // When
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, context);
        final Select select = compilationContext.toOperation();

        // Then
        assertNotNull(select);
        assertEquals(1, select.expressions().size());
    }

    @Test
    void constructWithContextDtoClass() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("users");
        final ColumnMetaData idCol = new ColumnMetaData(table, "user_id", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of("user_id"), List.of(idCol));

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(ormTable.mappedColumns()).thenReturn(List.of(idCol));
        when(context.tableRegistry().getTableInContextOrThrow(UserDto.class, ContextDto.class)).thenReturn(ormTable);

        final SelectNode selectNode = new SelectNode(null, UserDto.class, ContextDto.class, null, null, null);

        // When
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, context);

        // Then
        assertNotNull(compilationContext);
    }

    @Test
    void constructWithSqlModeSelectAllAndSpecificColumns() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", true, Types.INTEGER, 0);
        final ColumnMetaData nameCol = new ColumnMetaData(table, "name", true, Types.VARCHAR, 100);
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol, nameCol));

        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(any())).thenReturn(metaData);

        // Select All in SQL mode
        final SelectNode selectAllNode = new SelectNode("items", null, null, null, null, null);
        final SelectCompilationContext allContext = new SelectCompilationContext(selectAllNode, context);
        assertEquals(2, allContext.toOperation().expressions().size());

        // Specific columns in SQL mode
        final SelectNode selectColsNode = new SelectNode("items", null, null, new String[]{"name"}, null, null);
        final SelectCompilationContext colsContext = new SelectCompilationContext(selectColsNode, context);
        assertEquals(1, colsContext.toOperation().expressions().size());
    }

    @Test
    void addJoinWithDtoClassAndTable() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("users");
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol));

        final TableRegistry tableRegistry = context.tableRegistry();
        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(ormTable.mappedColumns()).thenReturn(List.of(idCol));
        when(ormTable.getContextTableRegistry()).thenReturn(tableRegistry);
        when(tableRegistry.getOrmTableOrThrow(UserDto.class)).thenReturn(ormTable);

        final SelectNode selectNode = new SelectNode(null, UserDto.class, null, null, null, null);
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, context);

        // When adding join with DTO
        final OrmTable roleOrmTable = mock(OrmTable.class);
        final Table roleTable = new Table("roles");
        final TableMetaData roleMeta = new TableMetaData(roleTable, List.of("id"), List.of(new ColumnMetaData(roleTable, "id", true, Types.INTEGER, 0)));
        when(roleOrmTable.getMetaData()).thenReturn(roleMeta);
        when(context.tableRegistry().getOrmTableOrThrow(RoleDto.class)).thenReturn(roleOrmTable);

        final JoinNode joinDtoNode = new JoinNode(selectNode, "INNER", RoleDto.class, null);
        compilationContext.addJoin(joinDtoNode);

        // When adding join with table name
        final Table ordersTable = new Table("orders");
        when(context.tableRegistry().getOrCreateSpiTable("orders")).thenReturn(ordersTable);
        final JoinNode joinTableNode = new JoinNode(joinDtoNode, "LEFT", null, "orders");
        compilationContext.addJoin(joinTableNode);

        // Then
        final Select select = compilationContext.toOperation();
        assertNotNull(select.joins());
        assertEquals(2, select.joins().size());
    }

    @Test
    void addJoinConditionWithRhsColumnAndWithout() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("users");
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol));

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(ormTable.mappedColumns()).thenReturn(List.of(idCol));
        when(ormTable.columnMetaDataForField("id")).thenReturn(idCol);

        final FieldAccessor fieldAccessor = mock(FieldAccessor.class);
        when(ormTable.getFieldForColumnName("id")).thenReturn(fieldAccessor);
        when(context.tableRegistry().getOrmTableOrThrow(UserDto.class)).thenReturn(ormTable);

        final SelectNode selectNode = new SelectNode(null, UserDto.class, null, null, null, null);
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, context);

        compilationContext.addJoin(new JoinNode(selectNode, "INNER", null, "orders"));

        // With rhsColumn
        final ConditionNode condWithRhsCol = new ConditionNode(null, LogicOperator.AND, "order_user_id", null, Operator.EQ, null, "id");
        compilationContext.addJoinCondition(condWithRhsCol);

        // Without rhsColumn
        final ConditionNode condWithoutRhsCol = new ConditionNode(null, LogicOperator.AND, "order_status", null, Operator.EQ, "PAID");
        compilationContext.addJoinCondition(condWithoutRhsCol);

        // Then
        assertEquals(2, compilationContext.joinConditionGroupStack().current().conditions().size());
    }

    @Test
    void toConditionNodeSinglePrimaryKey() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("users");
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol));

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(ormTable.mappedColumns()).thenReturn(List.of(idCol));

        final FieldAccessor fieldAccessor = mock(FieldAccessor.class);
        when(fieldAccessor.name()).thenReturn("userId");
        when(ormTable.getFieldForColumnName("id")).thenReturn(fieldAccessor);
        when(context.tableRegistry().getOrmTableOrThrow(UserDto.class)).thenReturn(ormTable);

        final SelectNode selectNode = new SelectNode(null, UserDto.class, null, null, null, null);
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, context);

        // When
        final ConditionWithIdNode withIdNode = new ConditionWithIdNode(null, LogicOperator.AND, Operator.EQ, 42);
        final ConditionNode result = compilationContext.toConditionNode(withIdNode);

        // Then
        assertNotNull(result);
        assertEquals("userId", result.lhsColumn());
        assertEquals(42, result.rhs());
        assertEquals(Operator.EQ, result.operator());
    }

    @Test
    void toConditionNodeCompositePrimaryKeyVariants() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("composite");
        final ColumnMetaData pk1Col = new ColumnMetaData(table, "pk1", true, Types.INTEGER, 0);
        final ColumnMetaData pk2Col = new ColumnMetaData(table, "pk2", true, Types.VARCHAR, 50);
        final TableMetaData metaData = new TableMetaData(table, List.of("pk1", "pk2"), List.of(pk1Col, pk2Col));

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(ormTable.mappedColumns()).thenReturn(List.of(pk1Col, pk2Col));

        final FieldAccessor fa1 = mock(FieldAccessor.class);
        when(fa1.name()).thenReturn("field1");
        final FieldAccessor fa2 = mock(FieldAccessor.class);
        when(fa2.name()).thenReturn("field2");

        when(ormTable.getFieldForColumnName("pk1")).thenReturn(fa1);
        when(ormTable.getFieldForColumnName("pk2")).thenReturn(fa2);
        when(context.tableRegistry().getOrmTableOrThrow(UserDto.class)).thenReturn(ormTable);

        final SelectNode selectNode = new SelectNode(null, UserDto.class, null, null, null, null);
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, context);

        // 1. Valid List
        final ConditionNode listResult = compilationContext.toConditionNode(new ConditionWithIdNode(null, LogicOperator.AND, Operator.EQ, List.of(1, "abc")));
        assertNotNull(listResult);

        // 2. Invalid List size throws
        assertThrows(IllegalArgumentException.class,
                () -> compilationContext.toConditionNode(new ConditionWithIdNode(null, LogicOperator.AND, Operator.EQ, List.of(1))));

        // 3. Valid Object[]
        final ConditionNode arrayResult = compilationContext.toConditionNode(new ConditionWithIdNode(null, LogicOperator.AND, Operator.EQ, new Object[]{1, "abc"}));
        assertNotNull(arrayResult);

        // 4. Invalid Object[] length throws
        assertThrows(IllegalArgumentException.class,
                () -> compilationContext.toConditionNode(new ConditionWithIdNode(null, LogicOperator.AND, Operator.EQ, new Object[]{1})));

        // 5. Valid Map
        final ConditionNode mapResult = compilationContext.toConditionNode(new ConditionWithIdNode(null, LogicOperator.AND, Operator.EQ, Map.of("field1", 1, "field2", "abc")));
        assertNotNull(mapResult);

        // 6. Invalid Map size throws
        assertThrows(IllegalArgumentException.class,
                () -> compilationContext.toConditionNode(new ConditionWithIdNode(null, LogicOperator.AND, Operator.EQ, Map.of("field1", 1))));

        // 7. Unsupported type throws
        assertThrows(IllegalArgumentException.class,
                () -> compilationContext.toConditionNode(new ConditionWithIdNode(null, LogicOperator.AND, Operator.EQ, "unsupported")));

        // 8. Null id throws
        assertThrows(IllegalArgumentException.class,
                () -> compilationContext.toConditionNode(new ConditionWithIdNode(null, LogicOperator.AND, Operator.EQ, null)));
    }

    @Test
    void toConditionNodeThrowsWhenNoPrimaryKey() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("no_pk");
        final ColumnMetaData col = new ColumnMetaData(table, "val", true, Types.VARCHAR, 50);
        final TableMetaData metaData = new TableMetaData(table, List.of(), List.of(col));

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(ormTable.mappedColumns()).thenReturn(List.of(col));
        when(context.tableRegistry().getOrmTableOrThrow(UserDto.class)).thenReturn(ormTable);

        final SelectNode selectNode = new SelectNode(null, UserDto.class, null, null, null, null);
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, context);

        // When & Then
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> compilationContext.toConditionNode(new ConditionWithIdNode(null, LogicOperator.AND, Operator.EQ, 1)));
        assertTrue(ex.getMessage().contains("No primary key fields found"));
    }

    @Test
    void groupByOrderByHavingAndLimit() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnMetaData catCol = new ColumnMetaData(table, "category", true, Types.VARCHAR, 50);
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol, catCol));

        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(any())).thenReturn(metaData);

        final SelectNode selectNode = new SelectNode("items", null, null, null, null, null);
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, context);

        // GroupBy via column names in SQL mode
        compilationContext.addGroupBy(new GroupByNode(null, new String[]{"category"}, null));

        // GroupBy via expressions
        final SelectColumnSpec spec = new SelectColumnSpec(new Column(table, "id"));
        when(context.selectExpressionMapper().resolveProtoExpression(any(), any(), any(), eq(ClauseType.GROUP_BY)))
                .thenReturn(List.of(spec));
        when(context.selectExpressionMapper().toSelectExpression(any(), eq(true)))
                .thenReturn(mock(SelectExpression.class));
        compilationContext.addGroupBy(new GroupByNode(null, null, new ExpressionSpec[]{spec}));

        // OrderBy via column name in SQL mode
        compilationContext.addOrderBy(new OrderByNode(null, "id", null, true));

        // OrderBy via expression
        when(context.selectExpressionMapper().resolveProtoExpression(any(), any(), any(), eq(ClauseType.ORDER_BY)))
                .thenReturn(List.of(spec));
        compilationContext.addOrderBy(new OrderByNode(null, null, spec, false));

        // Having condition
        assertNotNull(compilationContext.ensureHavingConditionGroupStack());
        compilationContext.addHavingCondition(new ConditionNode(null, LogicOperator.AND, "id", null, Operator.GT, 10));

        // Where condition
        compilationContext.addWhereCondition(new ConditionNode(null, LogicOperator.AND, "category", null, Operator.EQ, "books"));

        // Limit
        compilationContext.setLimit(new LimitNode(null, 20, 10));

        // When
        final Select select = compilationContext.toOperation();

        // Then
        assertNotNull(select.groupBy());
        assertEquals(2, select.groupBy().size());
        assertNotNull(select.orderBy());
        assertEquals(2, select.orderBy().size());
        assertNotNull(select.having());
        assertEquals(1, select.having().conditions().size());
        assertNotNull(select.where());
        assertEquals(1, select.where().conditions().size());
        assertNotNull(select.limit());
        assertEquals(20, select.limit().limit());
        assertEquals(10, select.limit().offset());
    }

    @Test
    void groupByAndOrderByInDtoMode() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("users");
        final ColumnMetaData nameCol = new ColumnMetaData(table, "user_name", true, Types.VARCHAR, 50);
        final TableMetaData metaData = new TableMetaData(table, List.of("user_name"), List.of(nameCol));

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(ormTable.mappedColumns()).thenReturn(List.of(nameCol));
        when(ormTable.columnMetaDataForField("name")).thenReturn(nameCol);
        when(context.tableRegistry().getOrmTableOrThrow(UserDto.class)).thenReturn(ormTable);

        final SelectNode selectNode = new SelectNode(null, UserDto.class, null, null, null, null);
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, context);

        // Group by field in DTO mode
        compilationContext.addGroupBy(new GroupByNode(null, new String[]{"name"}, null));

        // Order by field in DTO mode
        compilationContext.addOrderBy(new OrderByNode(null, "name", null, true));

        // Then
        final Select select = compilationContext.toOperation();
        assertEquals(1, select.groupBy().size());
        assertEquals(1, select.orderBy().size());
    }

    @Test
    void addJoinConditionUsingNodeWithUnsupportedTargetThrowsException() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("users");
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol));

        final TableRegistry tableRegistry = context.tableRegistry();
        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(ormTable.mappedColumns()).thenReturn(List.of(idCol));
        when(ormTable.getContextTableRegistry()).thenReturn(tableRegistry);

        final org.litebridge.db.spi.MappedFieldTarget unsupportedTarget = mock(org.litebridge.db.spi.MappedFieldTarget.class);
        when(ormTable.mappedFieldTargetForField("other")).thenReturn(unsupportedTarget);
        when(ormTable.mappedFieldTargetForFieldOrNull("other")).thenReturn(unsupportedTarget);
        when(tableRegistry.getOrmTableOrThrow(UserDto.class)).thenReturn(ormTable);

        final SelectNode selectNode = new SelectNode(null, UserDto.class, null, null, null, null);
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, context);

        compilationContext.addJoin(new JoinNode(selectNode, "INNER", UserDto.class, null));

        final ConditionJoinUsingNode usingNode = new ConditionJoinUsingNode(null, LogicOperator.AND, "other", null);

        // When & Then
        assertThrows(UnsupportedOperationException.class, () -> compilationContext.addJoinCondition(usingNode));
    }

    @Test
    void addJoinConditionUsingNodeOneToMany() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table userTable = new Table("users");
        final ColumnMetaData idCol = new ColumnMetaData(userTable, "id", true, Types.INTEGER, 0);
        final ColumnMetaData roleIdCol = new ColumnMetaData(userTable, "role_id", true, Types.INTEGER, 0);
        roleIdCol.setJoinColumn("id");
        final TableMetaData userMeta = new TableMetaData(userTable, List.of("id"), List.of(idCol, roleIdCol));

        final TableRegistry tableRegistry = context.tableRegistry();
        final OrmTable userOrmTable = mock(OrmTable.class);
        when(userOrmTable.getMetaData()).thenReturn(userMeta);
        when(userOrmTable.mappedColumns()).thenReturn(List.of(idCol, roleIdCol));
        when(userOrmTable.mappedFieldTargetForField("role")).thenReturn(roleIdCol);
        when(userOrmTable.mappedFieldTargetForFieldOrNull("role")).thenReturn(roleIdCol);
        when(userOrmTable.getContextTableRegistry()).thenReturn(tableRegistry);
        when(tableRegistry.getOrmTableOrThrow(UserDto.class)).thenReturn(userOrmTable);

        final Table roleTable = new Table("roles");
        final ColumnMetaData rolePkCol = new ColumnMetaData(roleTable, "id", true, Types.INTEGER, 0);
        final TableMetaData roleMeta = new TableMetaData(roleTable, List.of("id"), List.of(rolePkCol));
        final OrmTable roleOrmTable = mock(OrmTable.class);
        when(roleOrmTable.getMetaData()).thenReturn(roleMeta);
        when(roleOrmTable.mappedColumns()).thenReturn(List.of(rolePkCol));
        when(context.tableRegistry().getOrmTable(RoleDto.class)).thenReturn(roleOrmTable);

        final SelectNode selectNode = new SelectNode(null, UserDto.class, null, null, null, null);
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, context);

        compilationContext.addJoin(new JoinNode(selectNode, "INNER", RoleDto.class, null));

        final ConditionJoinUsingNode usingNode = new ConditionJoinUsingNode(null, LogicOperator.AND, "role", null);

        // When
        compilationContext.addJoinCondition(usingNode);

        // Then
        assertEquals(1, compilationContext.joinConditionGroupStack().current().conditions().size());
    }

    @Test
    void addJoinConditionUsingNodeOneToManyReverse() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table userTable = new Table("users");
        final ColumnMetaData idCol = new ColumnMetaData(userTable, "id", true, Types.INTEGER, 0);
        final TableMetaData userMeta = new TableMetaData(userTable, List.of("id"), List.of(idCol));

        final TableRegistry tableRegistry = context.tableRegistry();
        final OrmTable userOrmTable = mock(OrmTable.class);
        when(userOrmTable.getMetaData()).thenReturn(userMeta);
        when(userOrmTable.mappedColumns()).thenReturn(List.of(idCol));
        when(userOrmTable.getContextTableRegistry()).thenReturn(tableRegistry);
        when(tableRegistry.getOrmTableOrThrow(UserDto.class)).thenReturn(userOrmTable);

        final Table ordersTable = new Table("orders");
        final ColumnMetaData orderIdCol = new ColumnMetaData(ordersTable, "id", true, Types.INTEGER, 0);
        final ColumnMetaData orderUserIdCol = new ColumnMetaData(ordersTable, "user_id", true, Types.INTEGER, 0);
        final TableMetaData orderMeta = new TableMetaData(ordersTable, List.of("id"), List.of(orderIdCol, orderUserIdCol));
        final OrmTable orderOrmTable = mock(OrmTable.class);
        when(orderOrmTable.getMetaData()).thenReturn(orderMeta);
        when(orderOrmTable.mappedColumns()).thenReturn(List.of(orderIdCol, orderUserIdCol));
        when(orderOrmTable.columnMetaDataForField("userId")).thenReturn(orderUserIdCol);
        when(tableRegistry.getOrmTableOrThrow(OrderDto.class)).thenReturn(orderOrmTable);

        final FieldAccessor mappedBy = mock(FieldAccessor.class);
        when(mappedBy.name()).thenReturn("userId");
        final FieldAccessor collection = mock(FieldAccessor.class);
        final MappedOneToMany mappedOneToMany = new MappedOneToMany(mappedBy, collection);

        when(orderOrmTable.columnMetaDataForField(mappedBy)).thenReturn(orderUserIdCol);
        when(userOrmTable.mappedFieldTargetForField("orders")).thenReturn(mappedOneToMany);
        when(userOrmTable.mappedFieldTargetForFieldOrNull("orders")).thenReturn(mappedOneToMany);

        final SelectNode selectNode = new SelectNode(null, UserDto.class, null, null, null, null);
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, context);

        final JoinNode joinNode = new JoinNode(selectNode, "LEFT", OrderDto.class, null);
        final ConditionJoinUsingNode usingNode = new ConditionJoinUsingNode(null, LogicOperator.AND, "orders", null);
        joinNode.setCondition(usingNode);

        compilationContext.addJoin(joinNode);

        // When
        compilationContext.addJoinCondition(usingNode);

        // Then
        assertEquals(1, compilationContext.joinConditionGroupStack().current().conditions().size());
    }

    @Test
    void addJoinConditionUsingNodeManyToMany() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table userTable = new Table("users");
        final ColumnMetaData idCol = new ColumnMetaData(userTable, "id", true, Types.INTEGER, 0);
        final TableMetaData userMeta = new TableMetaData(userTable, List.of("id"), List.of(idCol));

        final TableRegistry tableRegistry = context.tableRegistry();
        final OrmTable userOrmTable = mock(OrmTable.class);
        when(userOrmTable.getMetaData()).thenReturn(userMeta);
        when(userOrmTable.mappedColumns()).thenReturn(List.of(idCol));
        when(userOrmTable.getContextTableRegistry()).thenReturn(tableRegistry);
        when(tableRegistry.getOrmTableOrThrow(UserDto.class)).thenReturn(userOrmTable);

        final Table joinTable = new Table("user_roles");
        final ColumnMetaData joinUserCol = new ColumnMetaData(joinTable, "user_id", true, Types.INTEGER, 0);
        final ColumnMetaData joinRoleCol = new ColumnMetaData(joinTable, "role_id", true, Types.INTEGER, 0);
        final TableMetaData joinMeta = new TableMetaData(joinTable, List.of("user_id", "role_id"), List.of(joinUserCol, joinRoleCol));
        final OrmTable joinOrmTable = mock(OrmTable.class);
        when(joinOrmTable.getMetaData()).thenReturn(joinMeta);
        when(joinOrmTable.dtoClass()).thenReturn((Class) RoleDto.class);

        final Table roleTable = new Table("roles");
        final ColumnMetaData rolePkCol = new ColumnMetaData(roleTable, "id", true, Types.INTEGER, 0);
        final TableMetaData roleMeta = new TableMetaData(roleTable, List.of("id"), List.of(rolePkCol));
        final OrmTable roleOrmTable = mock(OrmTable.class);
        when(roleOrmTable.getMetaData()).thenReturn(roleMeta);
        when(roleOrmTable.mappedColumns()).thenReturn(List.of(rolePkCol));
        when(roleOrmTable.dtoClass()).thenReturn((Class) RoleDto.class);
        when(tableRegistry.getOrmTableOrThrow(RoleDto.class)).thenReturn(roleOrmTable);

        final FieldAccessor collection = mock(FieldAccessor.class);
        final MappedManyToMany mappedManyToMany = new MappedManyToMany(joinOrmTable, "user_id", collection, () -> roleOrmTable, "role_id");

        when(userOrmTable.mappedFieldTargetForField("roles")).thenReturn(mappedManyToMany);
        when(userOrmTable.mappedFieldTargetForFieldOrNull("roles")).thenReturn(mappedManyToMany);

        final SelectNode selectNode = new SelectNode(null, UserDto.class, null, null, null, null);
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, context);

        final JoinNode joinNode = new JoinNode(selectNode, "INNER", RoleDto.class, null);
        final ConditionJoinUsingNode usingNode = new ConditionJoinUsingNode(null, LogicOperator.AND, "roles", null);
        joinNode.setCondition(usingNode);

        compilationContext.addJoin(joinNode);

        // When
        compilationContext.addJoinCondition(usingNode);

        // Then
        assertEquals(1, compilationContext.joinConditionGroupStack().current().conditions().size());
    }

    @Test
    void resolveAliasWithExistingAlias() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnMetaData col = new ColumnMetaData(table, "name", true, Types.VARCHAR, 50);
        final TableMetaData metaData = new TableMetaData(table, List.of(), List.of(col));

        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(any())).thenReturn(metaData);

        final SelectNode selectNode = new SelectNode("items", null, null, null, null, null);
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, context);

        // Column with existing alias
        final Column aliasedCol = new Column(table, "name").as("customAlias");
        assertSame(aliasedCol, compilationContext.resolveAlias(table, aliasedCol));

        // ExpressionSpec with aliased column
        final SelectColumnSpec specWithAlias = new SelectColumnSpec(aliasedCol);
        assertSame(specWithAlias, compilationContext.resolveAlias(specWithAlias));

        // ConvertSpec resolution
        final SelectColumnSpec colSpec = new SelectColumnSpec(new Column(new Table("other"), "col"));
        final ConvertSpec<?> convertSpec = Fn.convert(colSpec, String.class);
        final ExpressionSpec resolvedConvert = compilationContext.resolveAlias(convertSpec);
        assertNotNull(resolvedConvert);
    }

    static class UserDto {
        private String name;
    }

    static class RoleDto {}

    static class OrderDto {}

    static class ContextDto {}
}

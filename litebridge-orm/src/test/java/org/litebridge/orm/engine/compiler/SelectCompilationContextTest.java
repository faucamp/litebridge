package org.litebridge.orm.engine.compiler;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.Select;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionJoinUsingNode;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.GroupByNode;
import org.litebridge.orm.engine.ast.HavingNode;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.LimitNode;
import org.litebridge.orm.engine.ast.OrderByNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
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
        when(context.aliasGenerator()).thenReturn(new DefaultAliasGenerator());
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
        when(context.tableRegistry().getOrmTableOrThrow(any(Table.class))).thenReturn(ormTable);
        when(context.mode()).thenReturn(LitebridgeContext.Mode.DTO);

        final SelectNode selectNode = new SelectNode(null, UserDto.class, null, null, null, null);

        // When
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, context);
        final Select select = (Select) compilationContext.toOperation();

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
        final Select select = (Select) compilationContext.toOperation();

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
        when(context.selectExpressionMapper().resolveProtoExpression(any(), any(), any(), nullable(String.class), eq(ClauseType.SELECT)))
                .thenReturn(List.of(spec));
        when(context.selectExpressionMapper().toSelectExpression(any(), eq(false)))
                .thenReturn(mock(SelectExpression.class));

        final SelectNode selectNode = new SelectNode(null, UserDto.class, null, null, new ExpressionSpec[]{spec}, null);

        // When
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, context);
        final Select select = (Select) compilationContext.toOperation();

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
        when(context.tableRegistry().getOrmTableInContextOrThrow(UserDto.class, ContextDto.class)).thenReturn(ormTable);

        final SelectNode selectNode = new SelectNode(UserDto.class, ContextDto.class, null, null, null, null);

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
        when(context.mode()).thenReturn(LitebridgeContext.Mode.SQL);

        // Select All in SQL mode
        final SelectNode selectAllNode = new SelectNode("items", null, null, null, null);
        final SelectCompilationContext allContext = new SelectCompilationContext(selectAllNode, context);
        assertEquals(0, ((Select) allContext.toOperation()).expressions().size());

        // Specific columns in SQL mode
        final SelectNode selectColsNode = new SelectNode("items", null, new String[]{"name"}, null, null);
        final SelectCompilationContext colsContext = new SelectCompilationContext(selectColsNode, context);
        assertEquals(1, ((Select) colsContext.toOperation()).expressions().size());
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

        final JoinNode joinDtoNode = new JoinNode(selectNode, Join.JoinType.INNER, RoleDto.class, null, null, null, null);
        compilationContext.addJoin(joinDtoNode);

        // When adding join with table name
        final Table ordersTable = new Table("orders");
        when(context.tableRegistry().getOrCreateSpiTable("orders")).thenReturn(ordersTable);
        final JoinNode joinTableNode = new JoinNode(joinDtoNode, Join.JoinType.LEFT, null, null, "orders", null, null);
        compilationContext.addJoin(joinTableNode);

        // Then
        final Select select = (Select) compilationContext.toOperation();
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

        final ConditionGroupSpecStack joinConditionGroupSpecStack = compilationContext.addJoin(new JoinNode(selectNode, Join.JoinType.INNER, null, null, "orders", null, null));

        // With rhsColumn
        final ConditionNode condWithRhsCol = new ConditionNode(null, LogicOperator.AND, "order_user_id", null, Operator.EQ, null, "id");
        joinConditionGroupSpecStack.current().newCondition(LogicOperator.AND, "order_user_id", null, Operator.EQ, "id");

        // Without rhsColumn
//        final ConditionNode condWithoutRhsCol = new ConditionNode(null, LogicOperator.AND, "order_status", null, Operator.EQ, "PAID");
//        compilationContext.addJoinCondition(condWithoutRhsCol);

        // Then
//        assertEquals(2, compilationContext.joinConditionGroupStack().current().conditions().size());
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

        final SelectNode selectNode = new SelectNode("items", null, null, null, null);
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, context);

        // GroupBy via column names in SQL mode
        compilationContext.addGroupByNode(new GroupByNode(null, new String[]{"category"}, null));

        // GroupBy via expressions
        final SelectColumnSpec spec = new SelectColumnSpec(new Column(table, "id"));
        when(context.selectExpressionMapper().resolveProtoExpression(any(), any(), any(), nullable(String.class), eq(ClauseType.GROUP_BY)))
                .thenReturn(List.of(spec));
        when(context.selectExpressionMapper().toSelectExpression(any(), eq(true)))
                .thenReturn(mock(SelectExpression.class));
        compilationContext.addGroupByNode(new GroupByNode(null, null, new ExpressionSpec[]{spec}));

        // OrderBy via column name in SQL mode
        compilationContext.addOrderByNode(new OrderByNode(null, "id", null, true));

        // OrderBy via expression
        when(context.selectExpressionMapper().resolveProtoExpression(any(), any(), any(), nullable(String.class), eq(ClauseType.ORDER_BY)))
                .thenReturn(List.of(spec));
        compilationContext.addOrderByNode(new OrderByNode(null, null, spec, false));

        // Having condition
        final HavingNode havingNode = new HavingNode(null, new ConditionNode(null, LogicOperator.AND, "id", null, Operator.GT, 10));
        final ConditionGroupSpecStack havingConditionGroupSpecStack = compilationContext.setHavingNode(havingNode);
        havingConditionGroupSpecStack.current()
                .newCondition(LogicOperator.AND, "id", null, Operator.GT, 10);

        // Where condition
        final WhereNode whereNode = new WhereNode(null, new ConditionNode(null, LogicOperator.AND, "category", null, Operator.EQ, "books"));
        final ConditionGroupSpecStack whereConditionGroupSpecStack = compilationContext.setWhereNode(whereNode);
        whereConditionGroupSpecStack.current()
                .newCondition(LogicOperator.AND, "category", null, Operator.EQ, "books");

        // Limit
        compilationContext.setLimitNode(new LimitNode(null, 20, 10));

        // When
        final Select select = (Select) compilationContext.toOperation();

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
        compilationContext.addGroupByNode(new GroupByNode(null, new String[]{"name"}, null));

        // Order by field in DTO mode
        compilationContext.addOrderByNode(new OrderByNode(null, "name", null, true));

        // Then
        final Select select = (Select) compilationContext.toOperation();
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
        when(tableRegistry.getOrmTableOrThrow(any(Table.class))).thenReturn(ormTable);

        final SelectNode selectNode = new SelectNode(null, UserDto.class, null, null, null, null);
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, context);

        compilationContext.addJoin(new JoinNode(selectNode, Join.JoinType.INNER, UserDto.class, null, null, null, null));

        final ConditionJoinUsingNode usingNode = new ConditionJoinUsingNode(null, LogicOperator.AND, "other", null);
        compilationContext.addJoinUsingCondition(usingNode);

        // When & Then
        assertThrows(UnsupportedOperationException.class, compilationContext::toOperation);
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

        compilationContext.addJoin(new JoinNode(selectNode, Join.JoinType.INNER, RoleDto.class, null, null, null, null));

        final ConditionJoinUsingNode usingNode = new ConditionJoinUsingNode(null, LogicOperator.AND, "role", null);

        // When / Then
        compilationContext.addJoinUsingCondition(usingNode);
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

        final JoinNode joinNode = new JoinNode(selectNode, Join.JoinType.LEFT, OrderDto.class, null, null, null, null);
        final ConditionJoinUsingNode usingNode = new ConditionJoinUsingNode(null, LogicOperator.AND, "orders", null);
        joinNode.setCondition(usingNode);

        compilationContext.addJoin(joinNode);

        // When / Then
        compilationContext.addJoinUsingCondition(usingNode);
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

        final JoinNode joinNode = new JoinNode(selectNode, Join.JoinType.INNER, RoleDto.class, null, null, null, null);
        final ConditionJoinUsingNode usingNode = new ConditionJoinUsingNode(null, LogicOperator.AND, "roles", null);
        joinNode.setCondition(usingNode);

        compilationContext.addJoin(joinNode);

        // When / Then
        compilationContext.addJoinUsingCondition(usingNode);
    }

    static class UserDto {
        private String name;
    }

    static class RoleDto {
    }

    static class OrderDto {
    }

    static class ContextDto {
    }
}

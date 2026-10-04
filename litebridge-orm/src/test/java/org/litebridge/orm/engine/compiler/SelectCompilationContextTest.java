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
import static org.mockito.ArgumentMatchers.anyMap;
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
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, null, context);
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
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, null, context);
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
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, null, context);

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
        final SelectCompilationContext allContext = new SelectCompilationContext(selectAllNode, null, context);
        assertEquals(0, ((Select) allContext.toOperation()).expressions().size());

        // Specific columns in SQL mode
        final SelectNode selectColsNode = new SelectNode("items", null, new String[]{"name"}, null, null);
        final SelectCompilationContext colsContext = new SelectCompilationContext(selectColsNode, null, context);
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
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, null, context);

        // When adding join with DTO
        final OrmTable roleOrmTable = mock(OrmTable.class);
        final Table roleTable = new Table("roles");
        final TableMetaData roleMeta = new TableMetaData(roleTable, List.of("id"), List.of(new ColumnMetaData(roleTable, "id", true, Types.INTEGER, 0)));
        when(roleOrmTable.getMetaData()).thenReturn(roleMeta);
        when(context.tableRegistry().getOrmTableOrThrow(RoleDto.class)).thenReturn(roleOrmTable);
        when(context.tableRegistry().getOrmTable(RoleDto.class)).thenReturn(roleOrmTable);

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
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, null, context);

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
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, null, context);

        // GroupBy via column names in SQL mode
        compilationContext.addGroupByNode(new GroupByNode(null, new String[]{"category"}, null));

        // GroupBy via expressions
        final SelectColumnSpec spec = new SelectColumnSpec(new Column(table, "id"));
        when(context.selectExpressionMapper().resolveProtoExpression(any(), any(), any(), nullable(String.class), eq(ClauseType.GROUP_BY)))
                .thenReturn(List.of(spec));
        when(context.selectExpressionMapper().toSelectExpression(any(), anyMap()))
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
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, null, context);

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
        when(tableRegistry.getOrmTable(UserDto.class)).thenReturn(ormTable);
        when(tableRegistry.getOrmTableOrThrow(any(Table.class))).thenReturn(ormTable);
        when(tableRegistry.getOrmTable(any(Table.class))).thenReturn(ormTable);

        final SelectNode selectNode = new SelectNode(null, UserDto.class, null, null, null, null);
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, null, context);

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
        roleIdCol.setJoinColumnSupplier(() -> idCol);
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
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, null, context);

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
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, null, context);

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
        when(context.mode()).thenReturn(LitebridgeContext.Mode.DTO);

        final Table userTable = new Table("users");
        final ColumnMetaData idCol = new ColumnMetaData(userTable, "id", true, Types.INTEGER, 0);
        final TableMetaData userMeta = new TableMetaData(userTable, List.of("id"), List.of(idCol));

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

        final TableRegistry tableRegistry = context.tableRegistry();
        final OrmTable userOrmTable = mock(OrmTable.class);
        when(userOrmTable.getMetaData()).thenReturn(userMeta);
        when(userOrmTable.mappedColumns()).thenReturn(List.of(idCol));
        when(userOrmTable.getContextTableRegistry()).thenReturn(tableRegistry);
        when(tableRegistry.getOrmTableOrThrow(UserDto.class)).thenReturn(userOrmTable);
        when(tableRegistry.getOrmTable(UserDto.class)).thenReturn(userOrmTable);
        when(tableRegistry.getOrmTableOrThrow(RoleDto.class)).thenReturn(roleOrmTable);
        when(tableRegistry.getOrmTable(RoleDto.class)).thenReturn(roleOrmTable);
        when(tableRegistry.getOrmTableOrThrow(any(Table.class))).thenReturn(userOrmTable);
        when(tableRegistry.getOrmTable(any(Table.class))).thenReturn(userOrmTable);
        when(tableRegistry.getOrmTable(any(String.class))).thenReturn(userOrmTable);
        when(tableRegistry.getOrmTableOrThrow(any(String.class))).thenReturn(userOrmTable);
        when(context.tableMetaDataCache().ensureTableMetaData(joinTable)).thenReturn(joinMeta);

        final FieldAccessor collection = mock(FieldAccessor.class);
        final MappedManyToMany mappedManyToMany = new MappedManyToMany(joinOrmTable, new String[]{"user_id"}, collection, () -> roleOrmTable, new String[]{"role_id"});

        when(userOrmTable.mappedFieldTargetForField("roles")).thenReturn(mappedManyToMany);
        when(userOrmTable.mappedFieldTargetForFieldOrNull("roles")).thenReturn(mappedManyToMany);

        final SelectNode selectNode = new SelectNode(null, UserDto.class, null, null, null, null);
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, null, context);

        final JoinNode joinNode = new JoinNode(selectNode, Join.JoinType.INNER, RoleDto.class, null, null, null, null);
        final ConditionJoinUsingNode usingNode = new ConditionJoinUsingNode(null, LogicOperator.AND, "roles", null);
        joinNode.setCondition(usingNode);

        compilationContext.addJoin(joinNode);
        compilationContext.addJoinUsingCondition(usingNode);

        // When
        final Select select = (Select) compilationContext.toOperation();

        // Then
        assertNotNull(select);
        assertNotNull(select.joins());
        assertEquals(2, select.joins().size());
        final Join firstJoin = select.joins().get(0);
        final Join secondJoin = select.joins().get(1);
        assertEquals(Join.JoinType.INNER, firstJoin.type());
        assertEquals(Join.JoinType.INNER, secondJoin.type());
        assertNotNull(firstJoin.conditions());
        assertNotNull(secondJoin.conditions());
    }

    @Test
    void addJoinConditionUsingNodeManyToMany_compositePk() {
        // Given
        final LitebridgeContext context = createMockContext();
        when(context.mode()).thenReturn(LitebridgeContext.Mode.DTO);

        when(context.selectExpressionMapper().toSelectExpression(any(), anyMap()))
                .thenAnswer(inv -> {
                    final Object arg = inv.getArgument(0);
                    if (arg instanceof SelectColumnSpec spec) {
                        final org.litebridge.db.spi.expression.ColumnExpression colExpr = mock(org.litebridge.db.spi.expression.ColumnExpression.class);
                        when(colExpr.column()).thenReturn(spec.getColumn());
                        when(colExpr.tableAlias()).thenReturn(spec.getTableAlias());
                        return colExpr;
                    }
                    return mock(SelectExpression.class);
                });

        final Table userTable = new Table("comp_users");
        final ColumnMetaData userPk1 = new ColumnMetaData(userTable, "pk1", true, Types.INTEGER, 0);
        final ColumnMetaData userPk2 = new ColumnMetaData(userTable, "pk2", true, Types.INTEGER, 1);
        final TableMetaData userMeta = new TableMetaData(userTable, List.of("pk1", "pk2"), List.of(userPk1, userPk2));

        final Table joinTable = new Table("comp_user_roles");
        final ColumnMetaData joinLeftPk1 = new ColumnMetaData(joinTable, "left_pk1", true, Types.INTEGER, 0);
        final ColumnMetaData joinLeftPk2 = new ColumnMetaData(joinTable, "left_pk2", true, Types.INTEGER, 1);
        final ColumnMetaData joinRightPk1 = new ColumnMetaData(joinTable, "right_pk1", true, Types.INTEGER, 2);
        final ColumnMetaData joinRightPk2 = new ColumnMetaData(joinTable, "right_pk2", true, Types.INTEGER, 3);
        final TableMetaData joinMeta = new TableMetaData(joinTable, List.of("left_pk1", "left_pk2", "right_pk1", "right_pk2"), List.of(joinLeftPk1, joinLeftPk2, joinRightPk1, joinRightPk2));
        final OrmTable joinOrmTable = mock(OrmTable.class);
        when(joinOrmTable.getMetaData()).thenReturn(joinMeta);
        when(joinOrmTable.dtoClass()).thenReturn((Class) RoleDto.class);

        final Table roleTable = new Table("comp_roles");
        final ColumnMetaData rolePk1 = new ColumnMetaData(roleTable, "rpk1", true, Types.INTEGER, 0);
        final ColumnMetaData rolePk2 = new ColumnMetaData(roleTable, "rpk2", true, Types.INTEGER, 1);
        final TableMetaData roleMeta = new TableMetaData(roleTable, List.of("rpk1", "rpk2"), List.of(rolePk1, rolePk2));
        final OrmTable roleOrmTable = mock(OrmTable.class);
        when(roleOrmTable.getMetaData()).thenReturn(roleMeta);
        when(roleOrmTable.mappedColumns()).thenReturn(List.of(rolePk1, rolePk2));
        when(roleOrmTable.dtoClass()).thenReturn((Class) RoleDto.class);

        final TableRegistry tableRegistry = context.tableRegistry();
        final OrmTable userOrmTable = mock(OrmTable.class);
        when(userOrmTable.getMetaData()).thenReturn(userMeta);
        when(userOrmTable.mappedColumns()).thenReturn(List.of(userPk1, userPk2));
        when(userOrmTable.getContextTableRegistry()).thenReturn(tableRegistry);
        when(tableRegistry.getOrmTableOrThrow(UserDto.class)).thenReturn(userOrmTable);
        when(tableRegistry.getOrmTable(UserDto.class)).thenReturn(userOrmTable);
        when(tableRegistry.getOrmTableOrThrow(RoleDto.class)).thenReturn(roleOrmTable);
        when(tableRegistry.getOrmTable(RoleDto.class)).thenReturn(roleOrmTable);
        when(tableRegistry.getOrmTableOrThrow(any(Table.class))).thenReturn(userOrmTable);
        when(tableRegistry.getOrmTable(any(Table.class))).thenReturn(userOrmTable);
        when(tableRegistry.getOrmTable(any(String.class))).thenReturn(userOrmTable);
        when(tableRegistry.getOrmTableOrThrow(any(String.class))).thenReturn(userOrmTable);
        when(context.tableMetaDataCache().ensureTableMetaData(joinTable)).thenReturn(joinMeta);

        final FieldAccessor collection = mock(FieldAccessor.class);
        final MappedManyToMany mappedManyToMany = new MappedManyToMany(
                joinOrmTable,
                new String[]{"left_pk1", "left_pk2"},
                collection,
                () -> roleOrmTable,
                new String[]{"right_pk1", "right_pk2"});

        when(userOrmTable.mappedFieldTargetForField("roles")).thenReturn(mappedManyToMany);
        when(userOrmTable.mappedFieldTargetForFieldOrNull("roles")).thenReturn(mappedManyToMany);

        final SelectNode selectNode = new SelectNode(null, UserDto.class, null, null, null, null);
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, null, context);

        final JoinNode joinNode = new JoinNode(selectNode, Join.JoinType.INNER, RoleDto.class, null, null, null, null);
        final ConditionJoinUsingNode usingNode = new ConditionJoinUsingNode(null, LogicOperator.AND, "roles", null);
        joinNode.setCondition(usingNode);

        compilationContext.addJoin(joinNode);
        compilationContext.addJoinUsingCondition(usingNode);

        // When
        final Select select = (Select) compilationContext.toOperation();

        // Then
        assertNotNull(select);
        assertNotNull(select.joins());
        assertEquals(2, select.joins().size());

        final Join firstJoin = select.joins().get(0);
        final Join secondJoin = select.joins().get(1);

        assertEquals(Join.JoinType.INNER, firstJoin.type());
        assertEquals(Join.JoinType.INNER, secondJoin.type());

        // First join ON conditions: comp_users.pk1 = comp_user_roles.left_pk1 AND comp_users.pk2 = comp_user_roles.left_pk2
        assertEquals(2, firstJoin.conditions().conditions().size());
        final org.litebridge.db.spi.expression.ColumnExpression firstJoinLhs0 = (org.litebridge.db.spi.expression.ColumnExpression) firstJoin.conditions().conditions().get(0).condition().lhs();
        final org.litebridge.db.spi.expression.ColumnExpression firstJoinRhs0 = (org.litebridge.db.spi.expression.ColumnExpression) firstJoin.conditions().conditions().get(0).condition().rhs();
        final org.litebridge.db.spi.expression.ColumnExpression firstJoinLhs1 = (org.litebridge.db.spi.expression.ColumnExpression) firstJoin.conditions().conditions().get(1).condition().lhs();
        final org.litebridge.db.spi.expression.ColumnExpression firstJoinRhs1 = (org.litebridge.db.spi.expression.ColumnExpression) firstJoin.conditions().conditions().get(1).condition().rhs();
        assertEquals("pk1", firstJoinLhs0.column().name());
        assertEquals("left_pk1", firstJoinRhs0.column().name());
        assertEquals("pk2", firstJoinLhs1.column().name());
        assertEquals("left_pk2", firstJoinRhs1.column().name());

        // Second join ON conditions: comp_user_roles.right_pk1 = comp_roles.rpk1 AND comp_user_roles.right_pk2 = comp_roles.rpk2
        assertEquals(2, secondJoin.conditions().conditions().size());
        final org.litebridge.db.spi.expression.ColumnExpression secondJoinLhs0 = (org.litebridge.db.spi.expression.ColumnExpression) secondJoin.conditions().conditions().get(0).condition().lhs();
        final org.litebridge.db.spi.expression.ColumnExpression secondJoinRhs0 = (org.litebridge.db.spi.expression.ColumnExpression) secondJoin.conditions().conditions().get(0).condition().rhs();
        final org.litebridge.db.spi.expression.ColumnExpression secondJoinLhs1 = (org.litebridge.db.spi.expression.ColumnExpression) secondJoin.conditions().conditions().get(1).condition().lhs();
        final org.litebridge.db.spi.expression.ColumnExpression secondJoinRhs1 = (org.litebridge.db.spi.expression.ColumnExpression) secondJoin.conditions().conditions().get(1).condition().rhs();
        assertEquals("right_pk1", secondJoinLhs0.column().name());
        assertEquals("rpk1", secondJoinRhs0.column().name());
        assertEquals("right_pk2", secondJoinLhs1.column().name());
        assertEquals("rpk2", secondJoinRhs1.column().name());
    }

    @Test
    void multiLevelSharedDtoJoinResolvesCorrectLeftAlias() {
        // Given
        final LitebridgeContext context = createMockContext();
        when(context.mode()).thenReturn(LitebridgeContext.Mode.DTO);
        final TableRegistry tableRegistry = context.tableRegistry();

        // Tables & MetaData
        final Table tenantTable = new Table("tenants");
        final ColumnMetaData tenantIdCol = new ColumnMetaData(tenantTable, "id", true, Types.INTEGER, 0);
        final ColumnMetaData tenantSettingIdCol = new ColumnMetaData(tenantTable, "setting_id", true, Types.INTEGER, 1);
        tenantSettingIdCol.setJoinColumnSupplier(() -> tenantIdCol);
        final TableMetaData tenantMeta = new TableMetaData(tenantTable, List.of("id"), List.of(tenantIdCol, tenantSettingIdCol));
        final OrmTable tenantOrmTable = mock(OrmTable.class);
        when(tenantOrmTable.getMetaData()).thenReturn(tenantMeta);
        when(tenantOrmTable.mappedColumns()).thenReturn(List.of(tenantIdCol, tenantSettingIdCol));
        when(tenantOrmTable.dtoClass()).thenReturn((Class) TenantDto.class);
        when(tenantOrmTable.hasField("setting")).thenReturn(true);
        when(tenantOrmTable.hasField("accounts")).thenReturn(true);
        when(tenantOrmTable.columnMetaDataForField("setting")).thenReturn(tenantSettingIdCol);
        when(tenantOrmTable.mappedFieldTargetForField("setting")).thenReturn(tenantSettingIdCol);
        when(tenantOrmTable.mappedFieldTargetForFieldOrNull("setting")).thenReturn(tenantSettingIdCol);

        final Table tenantSettingTable = new Table("tenant_settings");
        final ColumnMetaData tenantSettingPkCol = new ColumnMetaData(tenantSettingTable, "id", true, Types.INTEGER, 0);
        final TableMetaData tenantSettingMeta = new TableMetaData(tenantSettingTable, List.of("id"), List.of(tenantSettingPkCol));
        final OrmTable tenantSettingOrmTable = mock(OrmTable.class);
        when(tenantSettingOrmTable.getMetaData()).thenReturn(tenantSettingMeta);
        when(tenantSettingOrmTable.mappedColumns()).thenReturn(List.of(tenantSettingPkCol));
        when(tenantSettingOrmTable.dtoClass()).thenReturn((Class) SettingDto.class);

        final FieldAccessor mappedByField = mock(FieldAccessor.class);
        when(mappedByField.name()).thenReturn("tenantId");
        final FieldAccessor accountsCollection = mock(FieldAccessor.class);
        final MappedOneToMany tenantAccountsMapping = new MappedOneToMany(mappedByField, accountsCollection);
        when(tenantOrmTable.mappedFieldTargetForField("accounts")).thenReturn(tenantAccountsMapping);
        when(tenantOrmTable.mappedFieldTargetForFieldOrNull("accounts")).thenReturn(tenantAccountsMapping);

        final Table accountTable = new Table("accounts");
        final ColumnMetaData accountIdCol = new ColumnMetaData(accountTable, "id", true, Types.INTEGER, 0);
        final ColumnMetaData accountTenantIdCol = new ColumnMetaData(accountTable, "tenant_id", true, Types.INTEGER, 1);
        final ColumnMetaData accountSettingIdCol = new ColumnMetaData(accountTable, "setting_id", true, Types.INTEGER, 2);

        final TableMetaData accountMeta = new TableMetaData(accountTable, List.of("id"), List.of(accountIdCol, accountTenantIdCol, accountSettingIdCol));
        final OrmTable accountOrmTable = mock(OrmTable.class);
        when(accountOrmTable.getMetaData()).thenReturn(accountMeta);
        when(accountOrmTable.mappedColumns()).thenReturn(List.of(accountIdCol, accountTenantIdCol, accountSettingIdCol));
        when(accountOrmTable.dtoClass()).thenReturn((Class) AccountDto.class);
        when(accountOrmTable.hasField("setting")).thenReturn(true);
        when(accountOrmTable.columnMetaDataForField("tenantId")).thenReturn(accountTenantIdCol);
        when(accountOrmTable.columnMetaDataForField(mappedByField)).thenReturn(accountTenantIdCol);
        when(accountOrmTable.columnMetaDataForField("setting")).thenReturn(accountSettingIdCol);
        when(accountOrmTable.mappedFieldTargetForField("setting")).thenReturn(accountSettingIdCol);
        when(accountOrmTable.mappedFieldTargetForFieldOrNull("setting")).thenReturn(accountSettingIdCol);

        final Table accountSettingTable = new Table("account_settings");
        final ColumnMetaData accountSettingPkCol = new ColumnMetaData(accountSettingTable, "id", true, Types.INTEGER, 0);
        final TableMetaData accountSettingMeta = new TableMetaData(accountSettingTable, List.of("id"), List.of(accountSettingPkCol));
        final OrmTable accountSettingOrmTable = mock(OrmTable.class);
        when(accountSettingOrmTable.getMetaData()).thenReturn(accountSettingMeta);
        when(accountSettingOrmTable.mappedColumns()).thenReturn(List.of(accountSettingPkCol));
        when(accountSettingOrmTable.dtoClass()).thenReturn((Class) SettingDto.class);
        accountSettingIdCol.setJoinColumnSupplier(() -> accountSettingPkCol);

        // Table registry stubs
        when(tableRegistry.getOrmTableOrThrow(TenantDto.class)).thenReturn(tenantOrmTable);
        when(tableRegistry.getOrmTable(TenantDto.class)).thenReturn(tenantOrmTable);
        when(tableRegistry.getOrmTableOrThrow(tenantTable)).thenReturn(tenantOrmTable);
        when(tableRegistry.getOrmTable(tenantTable)).thenReturn(tenantOrmTable);

        when(tableRegistry.getOrmTableOrThrow(AccountDto.class)).thenReturn(accountOrmTable);
        when(tableRegistry.getOrmTable(AccountDto.class)).thenReturn(accountOrmTable);
        when(tableRegistry.getOrmTableOrThrow(accountTable)).thenReturn(accountOrmTable);
        when(tableRegistry.getOrmTable(accountTable)).thenReturn(accountOrmTable);

        // SettingDto is a shared DTO
        when(tableRegistry.getOrmTable(SettingDto.class)).thenReturn(null);
        when(tableRegistry.getOrmTableInContext(SettingDto.class, TenantDto.class)).thenReturn(tenantSettingOrmTable);
        when(tableRegistry.getOrmTableInContext(SettingDto.class, AccountDto.class)).thenReturn(accountSettingOrmTable);
        when(tableRegistry.getOrmTableOrThrow(tenantSettingTable)).thenReturn(tenantSettingOrmTable);
        when(tableRegistry.getOrmTable(tenantSettingTable)).thenReturn(tenantSettingOrmTable);
        when(tableRegistry.getOrmTableOrThrow(accountSettingTable)).thenReturn(accountSettingOrmTable);
        when(tableRegistry.getOrmTable(accountSettingTable)).thenReturn(accountSettingOrmTable);

        // MetaDataCache stubs
        when(context.tableMetaDataCache().ensureTableMetaData(tenantTable)).thenReturn(tenantMeta);
        when(context.tableMetaDataCache().ensureTableMetaData(tenantSettingTable)).thenReturn(tenantSettingMeta);
        when(context.tableMetaDataCache().ensureTableMetaData(accountTable)).thenReturn(accountMeta);
        when(context.tableMetaDataCache().ensureTableMetaData(accountSettingTable)).thenReturn(accountSettingMeta);

        // SelectExpressionMapper stub
        when(context.selectExpressionMapper().toSelectExpression(any(), anyMap()))
                .thenAnswer(inv -> {
                    final Object arg = inv.getArgument(0);
                    if (arg instanceof SelectColumnSpec spec) {
                        final org.litebridge.db.spi.expression.ColumnExpression colExpr = mock(org.litebridge.db.spi.expression.ColumnExpression.class);
                        when(colExpr.column()).thenReturn(spec.getColumn());
                        when(colExpr.tableAlias()).thenReturn(spec.getTableAlias());
                        return colExpr;
                    }
                    return mock(SelectExpression.class);
                });

        // Build AST: Tenant -> Setting -> Account -> Setting
        final SelectNode selectNode = new SelectNode(TenantDto.class, null, null, null, null, null);
        final SelectCompilationContext compilationContext = new SelectCompilationContext(selectNode, null, context);

        final JoinNode join1 = new JoinNode(selectNode, Join.JoinType.INNER, SettingDto.class, null, null, null, null);
        final ConditionJoinUsingNode using1 = new ConditionJoinUsingNode(null, LogicOperator.AND, "setting", null);
        join1.setCondition(using1);
        compilationContext.addJoin(join1);
        compilationContext.addJoinUsingCondition(using1);

        final JoinNode join2 = new JoinNode(join1, Join.JoinType.INNER, AccountDto.class, null, null, null, null);
        final ConditionJoinUsingNode using2 = new ConditionJoinUsingNode(null, LogicOperator.AND, "accounts", null);
        join2.setCondition(using2);
        compilationContext.addJoin(join2);
        compilationContext.addJoinUsingCondition(using2);

        final JoinNode join3 = new JoinNode(join2, Join.JoinType.INNER, SettingDto.class, null, null, null, null);
        final ConditionJoinUsingNode using3 = new ConditionJoinUsingNode(null, LogicOperator.AND, "setting", null);
        join3.setCondition(using3);
        compilationContext.addJoin(join3);
        compilationContext.addJoinUsingCondition(using3);

        // When
        final Select select = (Select) compilationContext.toOperation();

        // Then
        assertNotNull(select);
        assertEquals(3, select.joins().size());

        final Join resultJoin1 = select.joins().get(0);
        final Join resultJoin2 = select.joins().get(1);
        final Join resultJoin3 = select.joins().get(2);

        assertEquals("tenant_settings", ((org.litebridge.db.spi.alias.AliasedTable) resultJoin1.target()).target().name());
        assertEquals("accounts", ((org.litebridge.db.spi.alias.AliasedTable) resultJoin2.target()).target().name());
        assertEquals("account_settings", ((org.litebridge.db.spi.alias.AliasedTable) resultJoin3.target()).target().name());

        // Verify the 3rd join's condition uses the Account table alias as left target
        final org.litebridge.db.spi.query.LogicCondition join3Condition = resultJoin3.conditions().conditions().getFirst();
        final org.litebridge.db.spi.expression.ColumnExpression leftExpr = (org.litebridge.db.spi.expression.ColumnExpression) join3Condition.condition().lhs();
        assertEquals("accounts", leftExpr.column().table().name());
        assertEquals(((org.litebridge.db.spi.alias.AliasedTable) resultJoin2.target()).alias(), leftExpr.tableAlias());
    }

    static class TenantDto {
    }

    static class AccountDto {
    }

    static class SettingDto {
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

//package org.litebridge.orm.engine;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.litebridge.convert.DefaultTypeConverter;
//import org.litebridge.db.spi.Column;
//import org.litebridge.db.spi.ColumnMetaData;
//import org.litebridge.db.spi.Table;
//import org.litebridge.db.spi.TableMetaData;
//import org.litebridge.db.spi.query.LogicOperator;
//import org.litebridge.db.spi.query.Operator;
//import org.litebridge.orm.api.delete.model.DeleteSpec;
//import org.litebridge.orm.api.dto.DtoJoinSpec;
//import org.litebridge.orm.api.dto.DtoSelectSpec;
//import org.litebridge.orm.api.select.SelectTerminal;
//import org.litebridge.orm.api.select.ast.ConditionGroupNode;
//import org.litebridge.orm.api.select.ast.ConditionNode;
//import org.litebridge.orm.api.select.ast.InsertNode;
//import org.litebridge.orm.api.select.ast.JoinNode;
//import org.litebridge.orm.api.select.ast.LimitNode;
//import org.litebridge.orm.api.select.ast.OrderByNode;
//import org.litebridge.orm.api.select.ast.SelectNode;
//import org.litebridge.orm.api.select.ast.SetNode;
//import org.litebridge.orm.api.select.ast.WhereNode;
//import org.litebridge.orm.api.select.model.ProtoExpressionResolver;
//import org.litebridge.orm.api.select.model.SelectExpressionMapper;
//import org.litebridge.orm.api.select.model.SelectSpec;
//import org.litebridge.orm.api.sql.SqlJoinSpec;
//import org.litebridge.orm.api.sql.SqlSelectSpec;
//import org.litebridge.orm.api.update.model.UpdateSpec;
//import org.litebridge.orm.expression.ColumnExpressionSpec;
//import org.litebridge.orm.expression.ExpressionSpec;
//import org.litebridge.orm.expression.select.SelectColumnSpec;
//import org.litebridge.orm.persistence.OrmTable;
//import org.litebridge.orm.persistence.TableMetaDataCache;
//import org.litebridge.orm.persistence.TableRegistry;
//import org.litebridge.orm.persistence.alias.AliasGenerator;
//import org.litebridge.orm.persistence.alias.DefaultAliasGenerator;
//import org.litebridge.orm.persistence.alias.NoOpAliasGenerator;
//import org.litebridge.tracking.FieldAccessor;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.junit.jupiter.api.Assertions.fail;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//class QueryCompilerTest {
//
//    private TableRegistry tableRegistry;
//    private AliasGenerator aliasGenerator;
//    private QueryCompiler compiler;
//    private LitebridgeContext litebridgeContext;
//
//    @BeforeEach
//    void setUp() {
//        tableRegistry = new TableRegistry();
//        aliasGenerator = new NoOpAliasGenerator();
//        litebridgeContext = mock(LitebridgeContext.class);
//        when(litebridgeContext.tableMetaDataCache()).thenReturn(mock(TableMetaDataCache.class));
//        compiler = new QueryCompiler(tableRegistry, mock(TableMetaDataCache.class), new DefaultTypeConverter(), aliasGenerator, mock(SelectExpressionMapper.class));
//    }
//
//    @Test
//    void testCompileSqlSelect() {
//        // Given
//        final Table table = new Table("test_table");
//        final SqlSelectSpec spec = new SqlSelectSpec(litebridgeContext, table);
//        setupSpec(spec);
//
//        final SelectColumnSpec selectExpr = new SelectColumnSpec(new Column(table, "col1"));
//        final SelectNode selectNode = new SelectNode(null, new ExpressionSpec[]{selectExpr}, null);
//
//        final ConditionNode condition = new ConditionNode(null, LogicOperator.AND, new SelectColumnSpec(new Column(table, "col2")), Operator.EQ, "val");
//        final WhereNode whereNode = new WhereNode(selectNode, condition);
//
//        final OrderByNode orderByNode = new OrderByNode(whereNode, new SelectColumnSpec(new Column(table, "col3")), true);
//        final LimitNode limitNode = new LimitNode(orderByNode, Optional.of(10), Optional.of(5));
//
//        // When
//        compiler.compile(limitNode, spec);
//
//        // Then
//        assertEquals(1, spec.getExpressions().size());
//        assertNotNull(spec.currentWhereConditionGroupSpec());
//        assertEquals(1, spec.getOrderBys().size());
//        assertEquals(10, spec.getLimit().getLimit().get());
//        assertEquals(5, spec.getLimit().getOffset().get());
//    }
//
//    @Test
//    void testCompileDelete() {
//        // Given
//        final Table table = new Table("test_table");
//        final DeleteSpec spec = new DeleteSpec(table, mock(SelectExpressionMapper.class));
//        final ConditionNode condition = new ConditionNode(null, LogicOperator.AND, new SelectColumnSpec(new Column(table, "col")), Operator.EQ, "val");
//        final WhereNode whereNode = new WhereNode(null, condition);
//
//        // When
//        compiler.compile(whereNode, spec);
//
//        // Then
//        assertNotNull(spec.currentConditionGroupSpec());
//    }
//
//    @Test
//    void testCompileUpdate() {
//        // Given
//        final Table table = new Table("test_table");
//        final UpdateSpec spec = new UpdateSpec(table, mock(SelectExpressionMapper.class));
//        final SetNode setNode = new SetNode(null, new Column(table, "col"), "val", true);
//        final ConditionNode condition = new ConditionNode(null, LogicOperator.AND, new SelectColumnSpec(new Column(table, "col2")), Operator.EQ, "val2");
//        final WhereNode whereNode = new WhereNode(setNode, condition);
//
//        // When
//        compiler.compile(whereNode, spec);
//
//        // Then
//        assertNotNull(spec.currentConditionGroupSpec());
//    }
//
//    @Test
//    void testCompileInsert() {
//        // Given
//        final Table table = new Table("test_table");
//        final SetNode setNode = new SetNode(null, new Column(table, "col"), "val", true);
//        final InsertNode insertNode = new InsertNode(setNode, "test_table", new String[0]);
//
//        // When/Then
//        compiler.compile(insertNode);
//    }
//
//    @Test
//    void testConditionGroupHandling() {
//        // Given
//        final Table table = new Table("test_table");
//        final SqlSelectSpec spec = new SqlSelectSpec(litebridgeContext, table);
//        setupSpec(spec);
//
//        final ConditionNode c1 = new ConditionNode(null, LogicOperator.AND, new SelectColumnSpec(new Column(table, "col1")), Operator.EQ, "v1");
//        final ConditionGroupNode group = new ConditionGroupNode(c1, LogicOperator.OR, new ConditionNode(null, LogicOperator.AND, new SelectColumnSpec(new Column(table, "col2")), Operator.EQ, "gv1"));
//        final WhereNode whereNode = new WhereNode(null, group);
//
//        // When
//        compiler.compile(whereNode, spec);
//
//        // Then
//        assertNotNull(spec.currentWhereConditionGroupSpec());
//    }
//
//    @Test
//    void testIllegalStateConditionGroupOutsideParent() {
//        // Given
//        final Table table = new Table("test_table");
//        final SqlSelectSpec spec = new SqlSelectSpec(litebridgeContext, table);
//        setupSpec(spec);
//        final ConditionGroupNode group = new ConditionGroupNode(null, LogicOperator.OR, new ConditionNode(null, LogicOperator.AND, new SelectColumnSpec(new Column(table, "col")), Operator.EQ, "v"));
//
//        // When/Then
//        NullPointerException npe = assertThrows(NullPointerException.class, () -> compiler.compile(group, spec));
//        assertTrue(npe.getMessage().contains("ConditionGroupNode outside of a parent context"));
//    }
//
//    @Test
//    void testCompileDtoSelect() {
//        // Given
//        final Class<TestDto> dtoClass = TestDto.class;
//        final Table table = new Table("test_table");
//        final OrmTable ormTable = createMockOrmTable(dtoClass, table, List.of());
//
//        final DtoSelectSpec spec = new DtoSelectSpec(dtoClass, ormTable, aliasGenerator, litebridgeContext);
//        setupSpec(spec);
//
//        final SelectColumnSpec expr = new SelectColumnSpec(new Column(table, "col"));
//        final SelectNode selectNode = new SelectNode(null, new ExpressionSpec[]{expr}, null);
//
//        // When
//        compiler.compile(selectNode, spec);
//
//        // Then
//        assertEquals(1, spec.getExpressions().size());
//        assertTrue(spec.getExpressions().get(0) instanceof ColumnExpressionSpec);
//    }
//
//    @Test
//    void testCompileDtoJoin() {
//        // Given
//        final Class<TestDto> sourceDtoClass = TestDto.class;
//        final Table sourceTable = new Table("source_table");
//        final OrmTable sourceOrmTable = createMockOrmTable(sourceDtoClass, sourceTable, List.of());
//        tableRegistry.addTable(sourceDtoClass, sourceOrmTable);
//
//        final DtoSelectSpec spec = new DtoSelectSpec(sourceDtoClass, sourceOrmTable, aliasGenerator, litebridgeContext);
//        setupSpec(spec);
//
//        final Class<AnotherDto> joinDtoClass = AnotherDto.class;
//        final Table joinTable = new Table("join_table");
//        final ColumnMetaData joinCol = new ColumnMetaData(joinTable, "join_col", false, java.sql.Types.VARCHAR);
//
//        final OrmTable joinOrmTable = createMockOrmTable(joinDtoClass, joinTable, List.of(joinCol));
//        tableRegistry.addTable(joinDtoClass, joinOrmTable);
//
//        final FieldAccessor accessor = mock(org.litebridge.tracking.FieldAccessor.class);
//        when(joinOrmTable.fieldForColumnNameOrNull("join_col")).thenReturn(accessor);
//
//        final JoinNode joinNode = new JoinNode(null, "INNER", joinDtoClass, sourceDtoClass, (String) null);
//
//        // When
//        compiler.compile(joinNode, spec);
//
//        // Then
//        assertEquals(1, spec.getJoins().size());
//        assertTrue(spec.getJoins().get(0) instanceof DtoJoinSpec);
//    }
//
//    @Test
//    void testCompileManyToManyJoin() {
//        // Given
//        final Class<TestDto> sourceDtoClass = TestDto.class;
//        final Table sourceTable = new Table("source_table");
//        final OrmTable sourceOrmTable = createMockOrmTable(sourceDtoClass, sourceTable, List.of());
//        tableRegistry.addTable(sourceDtoClass, sourceOrmTable);
//
//        final DtoSelectSpec spec = new DtoSelectSpec(sourceDtoClass, sourceOrmTable, aliasGenerator, litebridgeContext);
//        setupSpec(spec);
//
//        final Class<AnotherDto> joinDtoClass = AnotherDto.class;
//        final Table joinTable = new Table("join_table");
//        final OrmTable joinOrmTable = createMockOrmTable(joinDtoClass, joinTable, List.of());
//        tableRegistry.addTable(joinDtoClass, joinOrmTable);
//
//        final JoinNode joinNode = new JoinNode(null, "INNER", joinDtoClass, sourceDtoClass, "many_to_many_table");
//
//        // When
//        compiler.compile(joinNode, spec);
//
//        // Then
//        assertEquals(1, spec.getJoins().size());
//        assertTrue(spec.getJoins().get(0) instanceof SqlJoinSpec);
//    }
//
//    @Test
//    void testResolveAliasesInCondition() {
//        // Given
//        final Table table = new Table("test_table");
//        final SqlSelectSpec spec = new SqlSelectSpec(litebridgeContext, table);
//        setupSpec(spec);
//
//        final OrmTable ormTable = createMockOrmTable(TestDto.class, table, List.of());
//        tableRegistry.addTable(TestDto.class, ormTable);
//
//        final Column col = new Column(table, "col");
//        final ConditionNode condition = new ConditionNode(null, LogicOperator.AND, new SelectColumnSpec(col), Operator.EQ, "val");
//        final WhereNode whereNode = new WhereNode(null, condition);
//
//        // When
//        compiler.compile(whereNode, spec);
//
//        // Then
//        assertFalse(spec.currentWhereConditionGroupSpec().conditions().isEmpty());
//    }
//
//    @Test
//    @SuppressWarnings("unchecked")
//    void testResolveAliasesSelfJoinAmbiguity() throws Exception {
//        // Given
//        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(a -> a);
//
//        final Table sourceTable = new Table("test_table");
//        final OrmTable ormTable = createMockOrmTable(TestDto.class, sourceTable, List.of());
//        tableRegistry.addTable(TestDto.class, ormTable);
//
//        final Table sourceAlias = aliasGenerator.aliasTable(ormTable); // tt
//        final Table targetAlias = aliasGenerator.aliasTable(ormTable); // tt1
//
//        final QueryCompiler testCompiler = new QueryCompiler(tableRegistry, mock(TableMetaDataCache.class), new DefaultTypeConverter(), aliasGenerator, mock(SelectExpressionMapper.class));
//
//        final SqlSelectSpec spec = new SqlSelectSpec(litebridgeContext, sourceAlias);
//        setupSpec(spec);
//
//        final Field ahField = QueryCompiler.class.getDeclaredField("aliasHistory");
//        ahField.setAccessible(true);
//        final Map<Class<?>, List<Table>> aliasHistoryMap = (Map<Class<?>, List<Table>>) ahField.get(testCompiler);
//
//        final Field tomField = QueryCompiler.class.getDeclaredField("tableToOrmTableMap");
//        tomField.setAccessible(true);
//        final Map<Table, OrmTable> tableToOrmTableMap = (Map<Table, OrmTable>) tomField.get(testCompiler);
//
//        tableToOrmTableMap.put(sourceAlias, ormTable);
//        tableToOrmTableMap.put(targetAlias, ormTable);
//
//        aliasHistoryMap.computeIfAbsent(TestDto.class, k -> new ArrayList<>()).add(sourceAlias);
//        aliasHistoryMap.get(TestDto.class).add(targetAlias);
//
//        // Compile a condition that refers to the table without alias
//        ConditionNode cond = new ConditionNode(null, LogicOperator.AND, new SelectColumnSpec(new Column(sourceTable, "col")), Operator.EQ, "val");
//
//        // Use reflection to call resolveAliases(final @Nullable Object value, final @Nullable Table sourceAlias, final @Nullable Table targetAlias, boolean preferSource)
//        final Method resolveMethod = QueryCompiler.class.getDeclaredMethod("resolveAliases", Object.class, Table.class, Table.class, boolean.class);
//        resolveMethod.setAccessible(true);
//
//        // When/Then
//        // Case 1: preferSource is true (should prefer sourceAlias)
//        ExpressionSpec resolved = (ExpressionSpec) resolveMethod.invoke(testCompiler, cond.lhs(), sourceAlias, targetAlias, true);
//        assertEquals(sourceAlias, ((ColumnExpressionSpec) resolved).getColumn().table());
//
//        // Case 2: preferSource is false (should prefer targetAlias if it matches)
//        ExpressionSpec resolved2 = (ExpressionSpec) resolveMethod.invoke(testCompiler, cond.lhs(), sourceAlias, targetAlias, false);
//        assertEquals(targetAlias, ((ColumnExpressionSpec) resolved2).getColumn().table());
//    }
//
//    @Test
//    void testApplyNodeUnsupportedNode() {
//        // Given
//        final SqlSelectSpec spec = new SqlSelectSpec(litebridgeContext, new Table("t"));
//        final InsertNode unsupportedNode = new InsertNode(null, "t", new String[0]);
//
//        // When/Then
//        assertDoesNotThrow(() -> compiler.compile(unsupportedNode, spec));
//    }
//
//    @Test
//    void testGetTableDtoClass() throws Exception {
//        // Given
//        final Table table = new Table("test_table");
//        final OrmTable ormTable = createMockOrmTable(TestDto.class, table, List.of());
//        tableRegistry.addTable(TestDto.class, ormTable);
//
//        final Method getDtoMethod = QueryCompiler.class.getDeclaredMethod("getTableDtoClass", Table.class);
//        getDtoMethod.setAccessible(true);
//
//        // When/Then
//        assertEquals(TestDto.class, getDtoMethod.invoke(compiler, table));
//        assertNull(getDtoMethod.invoke(compiler, new Table("unknown")));
//    }
//
//    @Test
//    void testCreateSelectSpecUnsupported() {
//        // Given
//        final SelectTerminal<?> mockTerminal = mock(SelectTerminal.class);
//
//        final Method createSpecMethod;
//        try {
//            createSpecMethod = QueryCompiler.class.getDeclaredMethod("createSelectSpec", SelectTerminal.class);
//            createSpecMethod.setAccessible(true);
//
//            // When/Then
//            InvocationTargetException ite = assertThrows(java.lang.reflect.InvocationTargetException.class,
//                    () -> createSpecMethod.invoke(compiler, mockTerminal));
//            assertTrue(ite.getCause() instanceof IllegalArgumentException, "Cause should be IllegalArgumentException but was " + ite.getCause());
//            assertTrue(ite.getCause().getMessage().contains("Unsupported terminal type"), "Message was: " + ite.getCause().getMessage());
//        } catch (NoSuchMethodException e) {
//            fail(e);
//        }
//    }
//
//    private OrmTable createMockOrmTable(Class<?> dtoClass, Table table, List<ColumnMetaData> columns) {
//        OrmTable ormTable = mock(OrmTable.class);
//        when(ormTable.dtoClass()).thenReturn((Class) dtoClass);
//        TableMetaData metaData = new TableMetaData(table, List.of(), columns);
//        when(ormTable.getMetaData()).thenReturn(metaData);
//        when(ormTable.getDtoClassInterfaces()).thenReturn(Collections.emptySet());
//        when(ormTable.getContextTableRegistry()).thenReturn(new TableRegistry());
//        return ormTable;
//    }
//
//    private void setupSpec(SelectSpec spec) {
//        spec.setProtoExpressionResolver(mock(ProtoExpressionResolver.class));
//    }
//
//    private static class TestDto {
//    }
//
//    private static class AnotherDto {
//    }
//}

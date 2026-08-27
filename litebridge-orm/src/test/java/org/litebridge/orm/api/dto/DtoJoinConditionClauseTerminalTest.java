//package org.litebridge.orm.api.dto;
//
//import org.junit.jupiter.api.Test;
//import org.litebridge.commons.ClassUtils;
//import org.litebridge.db.spi.Column;
//import org.litebridge.db.spi.ColumnMetaData;
//import org.litebridge.db.spi.MappedFieldTarget;
//import org.litebridge.db.spi.Table;
//import org.litebridge.db.spi.TableMetaData;
//import org.litebridge.db.spi.alias.DefaultAliasTransformer;
//import org.litebridge.orm.api.select.ast.JoinNode;
//import org.litebridge.orm.engine.LitebridgeContext;
//import org.litebridge.orm.expression.select.SelectColumnSpec;
//import org.litebridge.orm.persistence.DtoConstructor;
//import org.litebridge.orm.persistence.OrmTable;
//import org.litebridge.orm.persistence.TableRegistry;
//import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
//import org.litebridge.orm.persistence.alias.AliasGenerator;
//import org.litebridge.orm.persistence.alias.DefaultAliasGenerator;
//import org.litebridge.tracking.ChangeTracker;
//import org.litebridge.tracking.ClassFieldAccessorCache;
//import org.litebridge.tracking.DirectFieldAccessor;
//import org.litebridge.tracking.FieldAccessor;
//
//import java.lang.invoke.MethodHandles;
//import java.sql.Types;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//class DtoJoinConditionClauseTerminalTest {
//
//    @Test
//    void and() {
//        // Given
//        final TestContext<TestDto> context = testContext();
//
//        // When
//        assertNotNull(context.terminal().and("myVar"));
//        assertNotNull(context.terminal().and(new SelectColumnSpec(mock(Column.class))));
//        assertNotNull(context.terminal().and(q -> q.where("myVar").eq("val")));
//    }
//
//    @Test
//    void or() {
//        // Given
//        final TestContext<TestDto> context = testContext();
//
//        // When
//        assertNotNull(context.terminal().or("myVar"));
//        assertNotNull(context.terminal().or(new SelectColumnSpec(mock(Column.class))));
//        assertNotNull(context.terminal().or(q -> q.where("myVar").eq("val")));
//    }
//
//    @Test
//    void where() {
//        // Given
//        final TestContext<TestDto> context = testContext();
//
//        final JoinNode joinNode = new JoinNode(context.dtoSelector().node(), "INNER", TestDto.class, TestDto.class, null)
//                .withCondition(new JoinNode(null, "INNER", null, null, null));
//        final DtoJoinConditionClauseTerminal<TestDto> terminal = new DtoJoinConditionClauseTerminal<>(joinNode, context.dtoSelector());
//
//        // When
//        assertNotNull(terminal.where("myVar"));
//        assertNotNull(terminal.where(new SelectColumnSpec(mock(Column.class))));
//    }
//
//    @Test
//    void join() {
//        // Given
//        final TestContext<TestDto> context = testContext();
//        final TableRegistry tableRegistry = mock(TableRegistry.class);
//        final OrmTable joinedTable = mock(OrmTable.class);
//        final TableMetaData metaData = mock(TableMetaData.class);
//        when(metaData.name()).thenReturn("JOINED_TABLE");
//        when(joinedTable.getMetaData()).thenReturn(metaData);
//        when(joinedTable.dtoClass()).thenReturn((Class) String.class);
//        when(tableRegistry.getTableOrThrow(String.class)).thenReturn(joinedTable);
//
//        final DtoSelector<TestDto> selector = new DtoSelector<>(
//                TestDto.class,
//                context.ormTable(),
//                tableRegistry,
//                mock(ClassFieldAccessorCache.class),
//                mock(DtoConstructor.class),
//                mock(TransactionalDatabaseProvider.class),
//                context.aliasGenerator(),
//                mock(LitebridgeContext.class),
//                null);
//
//        final JoinNode joinNode = new JoinNode(selector.node(), "INNER", TestDto.class, TestDto.class, null)
//                .withCondition(new JoinNode(null, "INNER", null, null, null));
//        final DtoJoinConditionClauseTerminal<TestDto> terminal = new DtoJoinConditionClauseTerminal<>(joinNode, selector);
//
//        // When
//        assertNotNull(terminal.join(String.class));
//    }
//
//    @Test
//    void groupBy() {
//        // Given
//        final TestContext<TestDto> context = testContext();
//
//        // When
//        assertNotNull(context.terminal().groupBy("myVar"));
//        assertNotNull(context.terminal().groupBy(new SelectColumnSpec(mock(Column.class))));
//    }
//
//    @Test
//    void orderBy() {
//        // Given
//        final TestContext<TestDto> context = testContext();
//
//        // When
//        assertNotNull(context.terminal().orderBy("myVar"));
//        assertNotNull(context.terminal().orderBy(new SelectColumnSpec(mock(Column.class))));
//    }
//
//    private static TestContext<TestDto> testContext() {
//        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
//        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
//        final TableMetaData tableMetaData = new TableMetaData(
//                "TEST_CATALOG",
//                "TEST_SCHEMA",
//                "TEST_TABLE",
//                List.of("MY_VAR"),
//                List.of(columnMetaData));
//        final FieldAccessor fieldAccessor = new DirectFieldAccessor(
//                ClassUtils.getField(TestDto.class, "myVar"),
//                MethodHandles.lookup());
//        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
//        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
//        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
//        final TableRegistry tableRegistry = new TableRegistry();
//        tableRegistry.addTable(TestDto.class, ormTable);
//        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
//        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
//        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());
//        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(
//                TestDto.class,
//                ormTable,
//                tableRegistry,
//                changeTracker.classFieldAccessorCache(),
//                dtoConstructor,
//                databaseProvider,
//                aliasGenerator,
//                mock(LitebridgeContext.class),
//                null);
//        final JoinNode joinNode = new JoinNode(dtoSelector.node(), "INNER", TestDto.class, TestDto.class, null)
//                .withCondition(new JoinNode(null, "INNER", null, null, null));
//        final DtoJoinConditionClauseTerminal<TestDto> terminal = new DtoJoinConditionClauseTerminal<>(
//                joinNode,
//                dtoSelector);
//
//        return new TestContext<>(ormTable, aliasGenerator, dtoSelector, terminal);
//    }
//
//    private static void setFieldValue(final Object obj, final String fieldName, final Object value) throws Exception {
//        final java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
//        field.setAccessible(true);
//        field.set(obj, value);
//    }
//
//    private record TestContext<DTO>(
//            OrmTable ormTable,
//            AliasGenerator aliasGenerator,
//            DtoSelector<DTO> dtoSelector,
//            DtoJoinConditionClauseTerminal<DTO> terminal) {
//    }
//
//    private static class TestDto {
//        private String myVar;
//    }
//}
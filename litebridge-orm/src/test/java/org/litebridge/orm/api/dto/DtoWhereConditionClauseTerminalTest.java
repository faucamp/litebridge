//package org.litebridge.orm.api.dto;
//
//import org.junit.jupiter.api.Test;
//import org.litebridge.commons.ClassUtils;
//import org.litebridge.commons.ObjectUtils;
//import org.litebridge.db.spi.ColumnMetaData;
//import org.litebridge.db.spi.MappedFieldTarget;
//import org.litebridge.db.spi.Table;
//import org.litebridge.db.spi.TableMetaData;
//import org.litebridge.db.spi.alias.DefaultAliasTransformer;
//import org.litebridge.orm.api.select.model.SelectSpec;
//import org.litebridge.orm.engine.LitebridgeContext;
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
//
//class DtoWhereConditionClauseTerminalTest {
//
//    @Test
//    void and() {
//        // Given
//        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
//        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
//        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
//        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
//        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
//        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
//        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
//        final TableRegistry tableRegistry = new TableRegistry();
//        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
//        tableRegistry.addTable(TestDto.class, ormTable);
//        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
//        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());
//        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, databaseProvider, aliasGenerator, mock(LitebridgeContext.class), null);
//        final SelectSpec selectSpec = ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
//        selectSpec.setTable(aliasGenerator.aliasTable(ormTable));
//
//        final DtoWhereConditionClauseTerminal<TestDto> terminal = new DtoWhereConditionClauseTerminal<>(dtoSelector);
//
//        // When
//        assertNotNull(terminal.and("myVar"));
//        assertNotNull(terminal.and(new org.litebridge.orm.expression.select.SelectColumnSpec(mock(org.litebridge.db.spi.Column.class))));
//        assertNotNull(terminal.and(q -> q.where("myVar").eq("val")));
//    }
//
//    @Test
//    void or() {
//        // Given
//        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
//        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
//        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
//        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
//        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
//        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
//        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
//        final TableRegistry tableRegistry = new TableRegistry();
//        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
//        tableRegistry.addTable(TestDto.class, ormTable);
//        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
//        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());
//        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, databaseProvider, aliasGenerator, mock(LitebridgeContext.class), null);
//        final SelectSpec selectSpec = ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
//        selectSpec.setTable(aliasGenerator.aliasTable(ormTable));
//
//        final DtoWhereConditionClauseTerminal<TestDto> terminal = new DtoWhereConditionClauseTerminal<>(dtoSelector);
//
//        // When
//        assertNotNull(terminal.or("myVar"));
//        assertNotNull(terminal.or(new org.litebridge.orm.expression.select.SelectColumnSpec(mock(org.litebridge.db.spi.Column.class))));
//        assertNotNull(terminal.or(q -> q.where("myVar").eq("val")));
//    }
//
//    @Test
//    void groupBy() {
//        // Given
//        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
//        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
//        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
//        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
//        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
//        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
//        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
//        final TableRegistry tableRegistry = new TableRegistry();
//        tableRegistry.addTable(TestDto.class, ormTable);
//        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
//        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
//        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());
//        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, databaseProvider, aliasGenerator, mock(LitebridgeContext.class), null);
//
//        final DtoWhereConditionClauseTerminal<TestDto> terminal = new DtoWhereConditionClauseTerminal<>(dtoSelector);
//
//        // When
//        assertNotNull(terminal.groupBy("myVar"));
//        assertNotNull(terminal.groupBy(new org.litebridge.orm.expression.select.SelectColumnSpec(mock(org.litebridge.db.spi.Column.class))));
//    }
//
//    @Test
//    void orderBy() {
//        // Given
//        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
//        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
//        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
//        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
//        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
//        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
//        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
//        final TableRegistry tableRegistry = new TableRegistry();
//        tableRegistry.addTable(TestDto.class, ormTable);
//        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
//        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
//        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());
//        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, databaseProvider, aliasGenerator, mock(LitebridgeContext.class), null);
//
//        final DtoWhereConditionClauseTerminal<TestDto> terminal = new DtoWhereConditionClauseTerminal<>(dtoSelector);
//
//        // When
//        assertNotNull(terminal.orderBy("myVar"));
//        assertNotNull(terminal.orderBy(new org.litebridge.orm.expression.select.SelectColumnSpec(mock(org.litebridge.db.spi.Column.class))));
//    }
//
//    private static class TestDto {
//        private String myVar;
//    }
//}
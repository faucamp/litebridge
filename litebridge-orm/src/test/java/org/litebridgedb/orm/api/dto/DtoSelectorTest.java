package org.litebridgedb.orm.api.dto;

import org.junit.jupiter.api.Test;
import org.litebridgedb.commons.ClassUtils;
import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.convert.DefaultTypeConverter;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.db.spi.MappedFieldTarget;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.alias.DefaultAliasTransformer;
import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.db.spi.query.Select;
import org.litebridgedb.db.spi.tx.ConnectionProvider;
import org.litebridgedb.db.spi.tx.TransactionManager;
import org.litebridgedb.orm.api.select.model.SelectSpec;
import org.litebridgedb.orm.config.LitebridgeConfig;
import org.litebridgedb.orm.expression.ProtoColumnExpression;
import org.litebridgedb.orm.expression.select.SelectField;
import org.litebridgedb.orm.expression.TestColumnExpressionFactory;
import org.litebridgedb.orm.persistence.DtoConstructor;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;
import org.litebridgedb.orm.persistence.alias.AliasGenerator;
import org.litebridgedb.orm.persistence.alias.DefaultAliasGenerator;
import org.litebridgedb.tracking.ChangeTracker;
import org.litebridgedb.tracking.ClassFieldAccessorCache;
import org.litebridgedb.tracking.DirectFieldAccessor;
import org.litebridgedb.tracking.FieldAccessor;

import java.lang.invoke.MethodHandles;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DtoSelectorTest {

    @Test
    void select() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionalDatabaseProvider transactionalDatabaseProvider = new TransactionalDatabaseProvider(mock(TransactionManager.class), databaseProvider);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, transactionalDatabaseProvider, aliasGenerator, new LitebridgeConfig());

        // When
        final DtoFromClauseTerminal<TestDto> result = dtoSelector.select(new ProtoColumnExpression(SelectField.class , "myVar"));

        // Then
        assertNotNull(result);
    }

    @Test
    void table() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionalDatabaseProvider transactionalDatabaseProvider = new TransactionalDatabaseProvider(mock(TransactionManager.class), databaseProvider);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, transactionalDatabaseProvider, aliasGenerator, new LitebridgeConfig());

        // When
        final OrmTable result = dtoSelector.table();

        // Then
        assertEquals(ormTable.dtoClass(), result.dtoClass());
        assertEquals(ormTable.getMetaData(), result.getMetaData());
    }

    @Test
    void one() throws Exception {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        when(sqlFunctionRegistry.selectColumnFactory()).thenReturn(new TestColumnExpressionFactory());
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        final TransactionalDatabaseProvider transactionalDatabaseProvider = new TransactionalDatabaseProvider(mock(TransactionManager.class), databaseProvider);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());
        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, transactionalDatabaseProvider, aliasGenerator, new LitebridgeConfig());
        final DtoSelectSpec selectSpec = (DtoSelectSpec) ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        final Table aliasedTable = aliasGenerator.aliasTable(ormTable);
        selectSpec.setTable(aliasedTable);
        final Column column = aliasGenerator.aliasColumn(aliasedTable, columnMetaData);
        selectSpec.setExpressions(List.of(new SelectField(fieldAccessor, column)));
        final Row row = new Row().withColumn(column, "testValue");
        when(databaseProvider.select(any(Select.class), any(ConnectionProvider.class))).thenReturn(List.of(row));
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());

        // When
        final Optional<TestDto> result = dtoSelector.one();

        // Then
        assertTrue(result.isPresent());
        assertEquals("testValue", result.get().myVar);
    }

    @Test
    void one_multipleRows() throws Exception {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        when(sqlFunctionRegistry.selectColumnFactory()).thenReturn(new TestColumnExpressionFactory());
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        final TransactionalDatabaseProvider transactionalDatabaseProvider = new TransactionalDatabaseProvider(mock(TransactionManager.class), databaseProvider);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());
        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, transactionalDatabaseProvider, aliasGenerator, new LitebridgeConfig());
        final DtoSelectSpec selectSpec = (DtoSelectSpec) ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        final Table aliasedTable = aliasGenerator.aliasTable(ormTable);
        selectSpec.setTable(aliasedTable);
        final Column column = aliasGenerator.aliasColumn(aliasedTable, columnMetaData);

        selectSpec.setExpressions(List.of(new SelectField(fieldAccessor, column)));
        final Row row1 = new Row().withColumn(column, "testValue1");
        final Row row2 = new Row().withColumn(column, "testValue2");
        when(databaseProvider.select(any(Select.class), any(ConnectionProvider.class))).thenReturn(List.of(row1, row2));
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());

        // When/Then
        assertThrows(IllegalStateException.class, dtoSelector::one);
    }

    @Test
    void one_noResults() throws Exception {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        when(sqlFunctionRegistry.selectColumnFactory()).thenReturn(new TestColumnExpressionFactory());
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        final TransactionalDatabaseProvider transactionalDatabaseProvider = new TransactionalDatabaseProvider(mock(TransactionManager.class), databaseProvider);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, transactionalDatabaseProvider, aliasGenerator, new LitebridgeConfig());
        final DtoSelectSpec selectSpec = (DtoSelectSpec) ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        final Table aliasedTable = aliasGenerator.aliasTable(ormTable);
        selectSpec.setTable(aliasedTable);
        final Column column = aliasGenerator.aliasColumn(aliasedTable, columnMetaData);
        selectSpec.setExpressions(List.of(new SelectField(fieldAccessor, column)));
        when(databaseProvider.select(any(Select.class), any(ConnectionProvider.class))).thenReturn(Collections.emptyList());

        // When
        final Optional<TestDto> result = dtoSelector.one();

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void oneOrThrow() throws Exception {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        when(sqlFunctionRegistry.selectColumnFactory()).thenReturn(new TestColumnExpressionFactory());
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        final TransactionalDatabaseProvider transactionalDatabaseProvider = new TransactionalDatabaseProvider(mock(TransactionManager.class), databaseProvider);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, transactionalDatabaseProvider, aliasGenerator, new LitebridgeConfig());
        final DtoSelectSpec selectSpec = (DtoSelectSpec) ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        final Table aliasedTable = aliasGenerator.aliasTable(ormTable);
        selectSpec.setTable(aliasedTable);
        final Column column = aliasGenerator.aliasColumn(aliasedTable, columnMetaData);
        selectSpec.setExpressions(List.of(new SelectField(fieldAccessor, column)));
        final Row row = new Row().withColumn(column, "testValue");
        when(databaseProvider.select(any(Select.class), any(ConnectionProvider.class))).thenReturn(List.of(row));
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());

        // When
        final TestDto result = dtoSelector.oneOrThrow();

        // Then
        assertEquals("testValue", result.myVar);
    }

    @Test
    void oneOrThrow_noResults() throws Exception {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        when(sqlFunctionRegistry.selectColumnFactory()).thenReturn(new TestColumnExpressionFactory());
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        final TransactionalDatabaseProvider transactionalDatabaseProvider = new TransactionalDatabaseProvider(mock(TransactionManager.class), databaseProvider);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());
        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, transactionalDatabaseProvider, aliasGenerator, new LitebridgeConfig());
        final DtoSelectSpec selectSpec = (DtoSelectSpec) ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        final Table aliasedTable = aliasGenerator.aliasTable(ormTable);
        selectSpec.setTable(aliasedTable);
        final Column column = aliasGenerator.aliasColumn(aliasedTable, columnMetaData);
        selectSpec.setExpressions(List.of(new SelectField(fieldAccessor, column)));
        when(databaseProvider.select(any(Select.class), any(ConnectionProvider.class))).thenReturn(Collections.emptyList());

        // When/Then
        assertThrows(NoSuchElementException.class, dtoSelector::oneOrThrow);
    }

    @Test
    void first() throws Exception {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        when(sqlFunctionRegistry.selectColumnFactory()).thenReturn(new TestColumnExpressionFactory());
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        final TransactionalDatabaseProvider transactionalDatabaseProvider = new TransactionalDatabaseProvider(mock(TransactionManager.class), databaseProvider);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());
        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, transactionalDatabaseProvider, aliasGenerator, new LitebridgeConfig());
        final DtoSelectSpec selectSpec = (DtoSelectSpec) ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        final Table aliasedTable = aliasGenerator.aliasTable(ormTable);
        selectSpec.setTable(aliasedTable);
        final Column column = aliasGenerator.aliasColumn(aliasedTable, columnMetaData);
        selectSpec.setExpressions(List.of(new SelectField(fieldAccessor, column)));
        final Row row = new Row().withColumn(column, "testValue");
        when(databaseProvider.select(any(Select.class), any(ConnectionProvider.class))).thenReturn(List.of(row));
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());

        // When
        final Optional<TestDto> result = dtoSelector.first();

        // Then
        assertTrue(result.isPresent());
        assertEquals("testValue", result.get().myVar);
    }

    @Test
    void first_noResults() throws Exception {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        when(sqlFunctionRegistry.selectColumnFactory()).thenReturn(new TestColumnExpressionFactory());
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        final TransactionalDatabaseProvider transactionalDatabaseProvider = new TransactionalDatabaseProvider(mock(TransactionManager.class), databaseProvider);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, transactionalDatabaseProvider, aliasGenerator, new LitebridgeConfig());
        final DtoSelectSpec selectSpec = (DtoSelectSpec) ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        final Table aliasedTable = aliasGenerator.aliasTable(ormTable);
        selectSpec.setTable(aliasedTable);
        final Column column = aliasGenerator.aliasColumn(aliasedTable, columnMetaData);
        selectSpec.setExpressions(List.of(new SelectField(fieldAccessor, column)));
        when(databaseProvider.select(any(Select.class), any(ConnectionProvider.class))).thenReturn(Collections.emptyList());

        // When
        final Optional<TestDto> result = dtoSelector.first();

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void first_multipleRows() throws Exception {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        when(sqlFunctionRegistry.selectColumnFactory()).thenReturn(new TestColumnExpressionFactory());
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        final TransactionalDatabaseProvider transactionalDatabaseProvider = new TransactionalDatabaseProvider(mock(TransactionManager.class), databaseProvider);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());
        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, transactionalDatabaseProvider, aliasGenerator, new LitebridgeConfig());
        final DtoSelectSpec selectSpec = (DtoSelectSpec) ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        final Table aliasedTable = aliasGenerator.aliasTable(ormTable);
        selectSpec.setTable(aliasedTable);
        final Column column = aliasGenerator.aliasColumn(aliasedTable, columnMetaData);
        selectSpec.setExpressions(List.of(new SelectField(fieldAccessor, column)));
        final Row row1 = new Row().withColumn(column, "testValue1");
        final Row row2 = new Row().withColumn(column, "testValue2");
        when(databaseProvider.select(any(Select.class), any(ConnectionProvider.class))).thenReturn(List.of(row1, row2));
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());

        // When
        final Optional<TestDto> result = dtoSelector.first();

        // Then
        assertTrue(result.isPresent());
        assertEquals("testValue1", result.get().myVar);
    }

    @Test
    void firstOrThrow() throws Exception {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        when(sqlFunctionRegistry.selectColumnFactory()).thenReturn(new TestColumnExpressionFactory());
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        final TransactionalDatabaseProvider transactionalDatabaseProvider = new TransactionalDatabaseProvider(mock(TransactionManager.class), databaseProvider);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, transactionalDatabaseProvider, aliasGenerator, new LitebridgeConfig());
        final DtoSelectSpec selectSpec = (DtoSelectSpec) ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        final Table aliasedTable = aliasGenerator.aliasTable(ormTable);
        selectSpec.setTable(aliasedTable);
        final Column column = aliasGenerator.aliasColumn(aliasedTable, columnMetaData);
        selectSpec.setExpressions(List.of(new SelectField(fieldAccessor, column)));
        final Row row = new Row().withColumn(column, "testValue");
        when(databaseProvider.select(any(Select.class), any(ConnectionProvider.class))).thenReturn(List.of(row));
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());

        // When
        final TestDto result = dtoSelector.firstOrThrow();

        // Then
        assertEquals("testValue", result.myVar);
    }

    @Test
    void firstOrThrow_noResults() throws Exception {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        when(sqlFunctionRegistry.selectColumnFactory()).thenReturn(new TestColumnExpressionFactory());
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        final TransactionalDatabaseProvider transactionalDatabaseProvider = new TransactionalDatabaseProvider(mock(TransactionManager.class), databaseProvider);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());
        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, transactionalDatabaseProvider, aliasGenerator, new LitebridgeConfig());
        final DtoSelectSpec selectSpec = (DtoSelectSpec) ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        final Table aliasedTable = aliasGenerator.aliasTable(ormTable);
        selectSpec.setTable(aliasedTable);
        final Column column = aliasGenerator.aliasColumn(aliasedTable, columnMetaData);
        selectSpec.setExpressions(List.of(new SelectField(fieldAccessor, column)));
        when(databaseProvider.select(any(Select.class), any(ConnectionProvider.class))).thenReturn(Collections.emptyList());

        // When/Then
        assertThrows(NoSuchElementException.class, dtoSelector::firstOrThrow);
    }

    @Test
    void list() throws Exception {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
        final TableRegistry tableRegistry = new TableRegistry();
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        when(sqlFunctionRegistry.selectColumnFactory()).thenReturn(new TestColumnExpressionFactory());
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        final TransactionalDatabaseProvider transactionalDatabaseProvider = new TransactionalDatabaseProvider(mock(TransactionManager.class), databaseProvider);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());
        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, transactionalDatabaseProvider, aliasGenerator, new LitebridgeConfig());
        final DtoSelectSpec selectSpec = (DtoSelectSpec) ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        final Table aliasedTable = aliasGenerator.aliasTable(ormTable);
        selectSpec.setTable(aliasedTable);
        final Column column = aliasGenerator.aliasColumn(aliasedTable, columnMetaData);
        selectSpec.setExpressions(List.of(new SelectField(fieldAccessor, column)));
        final Row row1 = new Row().withColumn(column, "testValue1");
        final Row row2 = new Row().withColumn(column, "testValue2");
        when(databaseProvider.select(any(Select.class), any(ConnectionProvider.class))).thenReturn(List.of(row1, row2));
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());

        // When
        final List<TestDto> result = dtoSelector.list();

        // Then
        assertEquals(2, result.size());
        assertEquals("testValue1", result.getFirst().myVar);
        assertEquals("testValue2", result.getLast().myVar);
    }

    @Test
    void stream() throws Exception {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        when(sqlFunctionRegistry.selectColumnFactory()).thenReturn(new TestColumnExpressionFactory());
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        final TransactionalDatabaseProvider transactionalDatabaseProvider = new TransactionalDatabaseProvider(mock(TransactionManager.class), databaseProvider);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, transactionalDatabaseProvider, aliasGenerator, new LitebridgeConfig());
        final DtoSelectSpec selectSpec = (DtoSelectSpec) ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        final Table aliasedTable = aliasGenerator.aliasTable(ormTable);
        selectSpec.setTable(aliasedTable);
        final Column column = aliasGenerator.aliasColumn(aliasedTable, columnMetaData);
        selectSpec.setExpressions(List.of(new SelectField(fieldAccessor, column)));
        final Row row1 = new Row().withColumn(column, "testValue1");
        final Row row2 = new Row().withColumn(column, "testValue2");
        when(databaseProvider.select(any(Select.class), any(ConnectionProvider.class))).thenReturn(List.of(row1, row2));
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);

        // When
        final Stream<TestDto> result = dtoSelector.stream();

        // Then
        final List<TestDto> testDtos = result.toList();
        assertEquals(2, testDtos.size());
        assertEquals("testValue1", testDtos.getFirst().myVar);
        assertEquals("testValue2", testDtos.getLast().myVar);
    }

    @Test
    void toSql() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionalDatabaseProvider transactionalDatabaseProvider = new TransactionalDatabaseProvider(mock(TransactionManager.class), databaseProvider);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        when(sqlFunctionRegistry.selectColumnFactory()).thenReturn(new TestColumnExpressionFactory());

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, transactionalDatabaseProvider, aliasGenerator, new LitebridgeConfig());
        final SelectSpec selectSpec = dtoSelector.selectSpec();
        selectSpec.setTable(aliasGenerator.aliasTable(ormTable));
        selectSpec.setExpressions(List.of(new SelectField(fieldAccessor, columnMetaData.toColumn())));

        // When
        final String result = dtoSelector.toSql();

        // Then
        verify(databaseProvider).toSql(any(Select.class), any(ConnectionProvider.class));
    }

    private static class TestDto {
        private String myVar;
    }
}
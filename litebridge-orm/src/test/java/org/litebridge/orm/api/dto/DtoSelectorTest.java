package org.litebridge.orm.api.dto;

import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.Aliased;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.query.Select;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.persistence.DefaultDtoMapper;
import org.litebridge.orm.persistence.DtoAliasRegistry;
import org.litebridge.orm.persistence.DtoMapper;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.FieldAccessor;
import org.litebridge.tracking.FieldAccessorImpl;

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
import static org.junit.jupiter.api.Assertions.assertSame;
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
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final Map<FieldAccessor, ColumnMetaData> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker();
        final OrmTable ormTable = new OrmTable(tableMetaData, fieldColumnMap, changeTracker);
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TypeConverter typeConverter = new DefaultTypeConverter();
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final DtoMapper dtoMapper = new DefaultDtoMapper(tableRegistry, typeConverter, dtoAliasRegistry);

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, databaseProvider, dtoMapper, dtoAliasRegistry);

        // When
        final DtoFromClauseTerminal<TestDto> result = dtoSelector.select("myVar");

        // Then
        assertNotNull(result);
    }

    @Test
    void select_aliased() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final Map<FieldAccessor, ColumnMetaData> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker();
        final OrmTable ormTable = new OrmTable(tableMetaData, fieldColumnMap, changeTracker);
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TypeConverter typeConverter = new DefaultTypeConverter();
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final DtoMapper dtoMapper = new DefaultDtoMapper(tableRegistry, typeConverter, dtoAliasRegistry);

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, databaseProvider, dtoMapper, dtoAliasRegistry);
        final Aliased aliased = new Aliased("myVar", "myVarAlias");

        // When
        final DtoFromClauseTerminal<TestDto> result = dtoSelector.select(aliased);

        // Then
        assertNotNull(result);
    }

    @Test
    void table() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final Map<FieldAccessor, ColumnMetaData> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker();
        final OrmTable ormTable = new OrmTable(tableMetaData, fieldColumnMap, changeTracker);
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TypeConverter typeConverter = new DefaultTypeConverter();
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final DtoMapper dtoMapper = new DefaultDtoMapper(tableRegistry, typeConverter, dtoAliasRegistry);

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, databaseProvider, dtoMapper, dtoAliasRegistry);

        // When
        final OrmTable result = dtoSelector.table();

        // Then
        assertSame(ormTable, result);
    }

    @Test
    void one() throws Exception {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final Map<FieldAccessor, ColumnMetaData> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker();
        final OrmTable ormTable = new OrmTable(tableMetaData, fieldColumnMap, changeTracker);
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TypeConverter typeConverter = new DefaultTypeConverter();
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final DtoMapper dtoMapper = new DefaultDtoMapper(tableRegistry, typeConverter, dtoAliasRegistry);

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, databaseProvider, dtoMapper, dtoAliasRegistry);
        final SelectSpec selectSpec = ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        selectSpec.setTable(tableMetaData);
        final Row row = new Row().withColumn(columnMetaData, "testValue");
        when(databaseProvider.select(any(Select.class))).thenReturn(List.of(row));

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
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final Map<FieldAccessor, ColumnMetaData> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker();
        final OrmTable ormTable = new OrmTable(tableMetaData, fieldColumnMap, changeTracker);
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TypeConverter typeConverter = new DefaultTypeConverter();
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final DtoMapper dtoMapper = new DefaultDtoMapper(tableRegistry, typeConverter, dtoAliasRegistry);

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, databaseProvider, dtoMapper, dtoAliasRegistry);
        final SelectSpec selectSpec = ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        selectSpec.setTable(tableMetaData);
        final Row row1 = new Row().withColumn(columnMetaData, "testValue1");
        final Row row2 = new Row().withColumn(columnMetaData, "testValue2");
        when(databaseProvider.select(any(Select.class))).thenReturn(List.of(row1, row2));

        // When/Then
        assertThrows(IllegalStateException.class, dtoSelector::one);
    }

    @Test
    void one_noResults() throws Exception {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final Map<FieldAccessor, ColumnMetaData> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker();
        final OrmTable ormTable = new OrmTable(tableMetaData, fieldColumnMap, changeTracker);
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TypeConverter typeConverter = new DefaultTypeConverter();
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final DtoMapper dtoMapper = new DefaultDtoMapper(tableRegistry, typeConverter, dtoAliasRegistry);

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, databaseProvider, dtoMapper, dtoAliasRegistry);
        final SelectSpec selectSpec = ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        selectSpec.setTable(tableMetaData);
        when(databaseProvider.select(any(Select.class))).thenReturn(Collections.emptyList());

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
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final Map<FieldAccessor, ColumnMetaData> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker();
        final OrmTable ormTable = new OrmTable(tableMetaData, fieldColumnMap, changeTracker);
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TypeConverter typeConverter = new DefaultTypeConverter();
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final DtoMapper dtoMapper = new DefaultDtoMapper(tableRegistry, typeConverter, dtoAliasRegistry);

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, databaseProvider, dtoMapper, dtoAliasRegistry);
        final SelectSpec selectSpec = ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        selectSpec.setTable(tableMetaData);
        final Row row = new Row().withColumn(columnMetaData, "testValue");
        when(databaseProvider.select(any(Select.class))).thenReturn(List.of(row));

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
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final Map<FieldAccessor, ColumnMetaData> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker();
        final OrmTable ormTable = new OrmTable(tableMetaData, fieldColumnMap, changeTracker);
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TypeConverter typeConverter = new DefaultTypeConverter();
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final DtoMapper dtoMapper = new DefaultDtoMapper(tableRegistry, typeConverter, dtoAliasRegistry);

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, databaseProvider, dtoMapper, dtoAliasRegistry);
        final SelectSpec selectSpec = ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        selectSpec.setTable(tableMetaData);
        when(databaseProvider.select(any(Select.class))).thenReturn(Collections.emptyList());

        // When/Then
        assertThrows(NoSuchElementException.class, dtoSelector::oneOrThrow);
    }

    @Test
    void first() throws Exception {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final Map<FieldAccessor, ColumnMetaData> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker();
        final OrmTable ormTable = new OrmTable(tableMetaData, fieldColumnMap, changeTracker);
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TypeConverter typeConverter = new DefaultTypeConverter();
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final DtoMapper dtoMapper = new DefaultDtoMapper(tableRegistry, typeConverter, dtoAliasRegistry);

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, databaseProvider, dtoMapper, dtoAliasRegistry);
        final SelectSpec selectSpec = ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        selectSpec.setTable(tableMetaData);
        final Row row = new Row().withColumn(columnMetaData, "testValue");
        when(databaseProvider.select(any(Select.class))).thenReturn(List.of(row));

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
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final Map<FieldAccessor, ColumnMetaData> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker();
        final OrmTable ormTable = new OrmTable(tableMetaData, fieldColumnMap, changeTracker);
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TypeConverter typeConverter = new DefaultTypeConverter();
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final DtoMapper dtoMapper = new DefaultDtoMapper(tableRegistry, typeConverter, dtoAliasRegistry);

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, databaseProvider, dtoMapper, dtoAliasRegistry);
        final SelectSpec selectSpec = ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        selectSpec.setTable(tableMetaData);
        when(databaseProvider.select(any(Select.class))).thenReturn(Collections.emptyList());

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
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final Map<FieldAccessor, ColumnMetaData> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker();
        final OrmTable ormTable = new OrmTable(tableMetaData, fieldColumnMap, changeTracker);
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TypeConverter typeConverter = new DefaultTypeConverter();
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final DtoMapper dtoMapper = new DefaultDtoMapper(tableRegistry, typeConverter, dtoAliasRegistry);

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, databaseProvider, dtoMapper, dtoAliasRegistry);
        final SelectSpec selectSpec = ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        selectSpec.setTable(tableMetaData);
        final Row row1 = new Row().withColumn(columnMetaData, "testValue1");
        final Row row2 = new Row().withColumn(columnMetaData, "testValue2");
        when(databaseProvider.select(any(Select.class))).thenReturn(List.of(row1, row2));

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
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final Map<FieldAccessor, ColumnMetaData> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker();
        final OrmTable ormTable = new OrmTable(tableMetaData, fieldColumnMap, changeTracker);
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TypeConverter typeConverter = new DefaultTypeConverter();
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final DtoMapper dtoMapper = new DefaultDtoMapper(tableRegistry, typeConverter, dtoAliasRegistry);

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, databaseProvider, dtoMapper, dtoAliasRegistry);
        final SelectSpec selectSpec = ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        selectSpec.setTable(tableMetaData);
        final Row row = new Row().withColumn(columnMetaData, "testValue");
        when(databaseProvider.select(any(Select.class))).thenReturn(List.of(row));

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
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final Map<FieldAccessor, ColumnMetaData> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker();
        final OrmTable ormTable = new OrmTable(tableMetaData, fieldColumnMap, changeTracker);
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TypeConverter typeConverter = new DefaultTypeConverter();
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final DtoMapper dtoMapper = new DefaultDtoMapper(tableRegistry, typeConverter, dtoAliasRegistry);

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, databaseProvider, dtoMapper, dtoAliasRegistry);
        final SelectSpec selectSpec = ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        selectSpec.setTable(tableMetaData);
        when(databaseProvider.select(any(Select.class))).thenReturn(Collections.emptyList());

        // When/Then
        assertThrows(NoSuchElementException.class, dtoSelector::firstOrThrow);
    }

    @Test
    void list() throws Exception {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final Map<FieldAccessor, ColumnMetaData> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker();
        final OrmTable ormTable = new OrmTable(tableMetaData, fieldColumnMap, changeTracker);
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TypeConverter typeConverter = new DefaultTypeConverter();
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final DtoMapper dtoMapper = new DefaultDtoMapper(tableRegistry, typeConverter, dtoAliasRegistry);

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, databaseProvider, dtoMapper, dtoAliasRegistry);
        final SelectSpec selectSpec = ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        selectSpec.setTable(tableMetaData);
        final Row row1 = new Row().withColumn(columnMetaData, "testValue1");
        final Row row2 = new Row().withColumn(columnMetaData, "testValue2");
        when(databaseProvider.select(any(Select.class))).thenReturn(List.of(row1, row2));

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
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final Map<FieldAccessor, ColumnMetaData> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker();
        final OrmTable ormTable = new OrmTable(tableMetaData, fieldColumnMap, changeTracker);
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TypeConverter typeConverter = new DefaultTypeConverter();
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final DtoMapper dtoMapper = new DefaultDtoMapper(tableRegistry, typeConverter, dtoAliasRegistry);

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, databaseProvider, dtoMapper, dtoAliasRegistry);
        final SelectSpec selectSpec = ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        selectSpec.setTable(tableMetaData);
        final Row row1 = new Row().withColumn(columnMetaData, "testValue1");
        final Row row2 = new Row().withColumn(columnMetaData, "testValue2");
        when(databaseProvider.select(any(Select.class))).thenReturn(List.of(row1, row2));

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
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final Map<FieldAccessor, ColumnMetaData> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker();
        final OrmTable ormTable = new OrmTable(tableMetaData, fieldColumnMap, changeTracker);
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TypeConverter typeConverter = new DefaultTypeConverter();
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final DtoMapper dtoMapper = new DefaultDtoMapper(tableRegistry, typeConverter, dtoAliasRegistry);

        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, databaseProvider, dtoMapper, dtoAliasRegistry);
        final SelectSpec selectSpec = ObjectUtils.getFieldValue(dtoSelector, "selectSpec", SelectSpec.class);
        selectSpec.setTable(tableMetaData);

        // When
        final String result = dtoSelector.toSql();

        // Then
        verify(databaseProvider).toSql(any(Select.class));
    }

    private static class TestDto {
        private String myVar;
    }
}
package org.litebridge.orm;

import org.junit.jupiter.api.Test;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.Aliased;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.api.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.spec.ColumnSpec;
import org.litebridge.orm.api.spec.ColumnSpecImpl;
import org.litebridge.orm.api.spec.FieldSpec;
import org.litebridge.orm.api.spec.FieldSpecImpl;
import org.litebridge.orm.api.spec.TableSpec;
import org.litebridge.orm.api.sql.SqlFromClause;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class LitebridgeTest {

    @Test
    void register() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final Litebridge litebridge = new Litebridge(databaseProvider);
        final FieldSpec fieldSpec = new FieldSpecImpl("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpecImpl("MY_VAR", false, null, null);
        final Map<FieldSpec, ColumnSpec> fieldColumnSpecMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnSpecMap);
        final ColumnMetaData columnMetaData = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);

        when(databaseProvider.getTableMetaData(tableSpec)).thenReturn(new TableMetaData(tableSpec, List.of("MY_VAR"), List.of(columnMetaData)));

        // When
        litebridge.register(TestDto.class, tableSpec);

        // Then
        final TableRegistry tableRegistry = ObjectUtils.getFieldValue(litebridge, "tableRegistry", TableRegistry.class);
        final OrmTable result = tableRegistry.getTableOrThrow(TestDto.class);
        assertNotNull(result);
    }

    @Test
    void track() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final Litebridge litebridge = new Litebridge(databaseProvider);
        final FieldSpec fieldSpec = new FieldSpecImpl("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpecImpl("MY_VAR", false, null, null);
        final Map<FieldSpec, ColumnSpec> fieldColumnSpecMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnSpecMap);
        final ColumnMetaData columnMetaData = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        when(databaseProvider.getTableMetaData(tableSpec)).thenReturn(new TableMetaData(tableSpec, List.of("MY_VAR"), List.of(columnMetaData)));
        litebridge.register(TestDto.class, tableSpec);

        final TestDto testDto = new TestDto();

        // When
        litebridge.track(testDto);

        // Then
        final TableRegistry tableRegistry = ObjectUtils.getFieldValue(litebridge, "tableRegistry", TableRegistry.class);
        final OrmTable table = tableRegistry.getTableOrThrow(TestDto.class);
        assertNotNull(table.getTrackedDto(testDto));
    }

    @Test
    void save() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final Litebridge litebridge = new Litebridge(databaseProvider);
        final FieldSpec fieldSpec = new FieldSpecImpl("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpecImpl("MY_VAR", false, null, null);
        final Map<FieldSpec, ColumnSpec> fieldColumnSpecMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnSpecMap);
        final ColumnMetaData columnMetaData = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        when(databaseProvider.getTableMetaData(tableSpec)).thenReturn(new TableMetaData(tableSpec, List.of("MY_VAR"), List.of(columnMetaData)));
        litebridge.register(TestDto.class, tableSpec);
        final TestDto testDto = new TestDto();
        litebridge.track(testDto);

        // When
        litebridge.save(testDto);

        // Then
        verify(databaseProvider).getTableMetaData(tableSpec);
        verifyNoMoreInteractions(databaseProvider);
    }

    @Test
    void select_dto() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final Litebridge litebridge = new Litebridge(databaseProvider);
        final FieldSpec fieldSpec = new FieldSpecImpl("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpecImpl("MY_VAR", false, null, null);
        final Map<FieldSpec, ColumnSpec> fieldColumnSpecMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnSpecMap);
        final ColumnMetaData columnMetaData = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        when(databaseProvider.getTableMetaData(tableSpec)).thenReturn(new TableMetaData(tableSpec, List.of("MY_VAR"), List.of(columnMetaData)));
        litebridge.register(TestDto.class, tableSpec);

        // When
        final DtoFromClauseTerminal<TestDto> result = litebridge.select(TestDto.class);

        // Then
        assertNotNull(result);
    }

    @Test
    void select_columns() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final Litebridge litebridge = new Litebridge(databaseProvider);
        final FieldSpec fieldSpec = new FieldSpecImpl("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpecImpl("MY_VAR", false, null, null);
        final Map<FieldSpec, ColumnSpec> fieldColumnSpecMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnSpecMap);
        final ColumnMetaData columnMetaData = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        when(databaseProvider.getTableMetaData(tableSpec)).thenReturn(new TableMetaData(tableSpec, List.of("MY_VAR"), List.of(columnMetaData)));
        litebridge.register(TestDto.class, tableSpec);

        // When
        final SqlFromClause result = litebridge.select("MY_VAR");

        // Then
        assertNotNull(result);
    }

    @Test
    void select_allColumns() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final Litebridge litebridge = new Litebridge(databaseProvider);
        final FieldSpec fieldSpec = new FieldSpecImpl("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpecImpl("MY_VAR", false, null, null);
        final Map<FieldSpec, ColumnSpec> fieldColumnSpecMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnSpecMap);
        final ColumnMetaData columnMetaData = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        when(databaseProvider.getTableMetaData(tableSpec)).thenReturn(new TableMetaData(tableSpec, List.of("MY_VAR"), List.of(columnMetaData)));
        litebridge.register(TestDto.class, tableSpec);

        // When
        final SqlFromClause result = litebridge.select();

        // Then
        assertNotNull(result);
    }

    @Test
    void select_aliased() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final Litebridge litebridge = new Litebridge(databaseProvider);
        final FieldSpec fieldSpec = new FieldSpecImpl("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpecImpl("MY_VAR", false, null, null);
        final Map<FieldSpec, ColumnSpec> fieldColumnSpecMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnSpecMap);
        final ColumnMetaData columnMetaData = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        when(databaseProvider.getTableMetaData(tableSpec)).thenReturn(new TableMetaData(tableSpec, List.of("MY_VAR"), List.of(columnMetaData)));
        litebridge.register(TestDto.class, tableSpec);

        final Aliased aliased = new Aliased("TEST_COLUMN", "testAlias");

        // When
        final SqlFromClause result = litebridge.select(aliased);

        // Then
        assertNotNull(result);
    }

    @Test
    void toDto() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final Litebridge litebridge = new Litebridge(databaseProvider);
        final FieldSpec fieldSpec = new FieldSpecImpl("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpecImpl("MY_VAR", false, null, null);
        final Map<FieldSpec, ColumnSpec> fieldColumnSpecMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnSpecMap);
        final ColumnMetaData columnMetaData = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        when(databaseProvider.getTableMetaData(tableSpec)).thenReturn(new TableMetaData(tableSpec, List.of("MY_VAR"), List.of(columnMetaData)));
        litebridge.register(TestDto.class, tableSpec);
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());

        final Row row = new Row().withColumn(columnMetaData, "testValue");

        // When
        final TestDto result = litebridge.toDto(row, TestDto.class);

        // Then
        assertNotNull(result);
        assertEquals("testValue", result.myVar);
    }

    private static class TestDto {
        private String myVar;
    }
}
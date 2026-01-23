package org.litebridge.orm;

import org.junit.jupiter.api.Test;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.Aliased;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.Update;
import org.litebridge.orm.api.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.spec.ColumnMapping;
import org.litebridge.orm.api.spec.ColumnSpec;
import org.litebridge.orm.api.spec.FieldSpec;
import org.litebridge.orm.api.spec.TableSpec;
import org.litebridge.orm.api.sql.SqlFromClause;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
import org.mockito.ArgumentCaptor;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
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
        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldSpec, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
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
        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldSpec, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
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
        final FieldSpec fieldSpecMyId = new FieldSpec("myId", false);
        final ColumnSpec columnSpecMyId = new ColumnSpec("MY_ID", true, "LB.TEST_SEQ", null);
        final FieldSpec fieldSpecMyVar = new FieldSpec("myVar", false);
        final ColumnSpec columnSpecMyVar = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldSpec, ColumnMapping> fieldColumnMap = Map.of(
                fieldSpecMyId, columnSpecMyId,
                fieldSpecMyVar, columnSpecMyVar);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final ColumnMetaData columnMetaDataMyId = new ColumnMetaData(tableSpec, "MY_ID", false, Types.NUMERIC, 10);
        final ColumnMetaData columnMetaDataMyVar = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        final TableMetaData tableMetaData = new TableMetaData(tableSpec, List.of("MY_ID"), List.of(columnMetaDataMyId, columnMetaDataMyVar));
        when(databaseProvider.getTableMetaData(tableSpec)).thenReturn(tableMetaData);

        litebridge.register(TestDto.class, tableSpec);
        final TestDto testDto = new TestDto();
        testDto.myId = 123L;
        testDto.myVar = "testValue";

        // When
        litebridge.save(testDto);

        // Then
        verify(databaseProvider).getTableMetaData(tableSpec);
        final ArgumentCaptor<Insert> insertArgumentCaptor = ArgumentCaptor.forClass(Insert.class);
        verify(databaseProvider).insert(insertArgumentCaptor.capture());

        final Insert insert = insertArgumentCaptor.getValue();
        assertEquals(tableMetaData, insert.table());
        assertEquals(2, insert.columns().size());
        assertEquals("MY_ID", insert.columns().getFirst().name());
        assertEquals("MY_VAR", insert.columns().getLast().name());
        assertEquals(1, insert.rows().size());
        assertEquals(2, insert.rows().getFirst().columns().size());
        assertEquals("MY_ID", insert.rows().getFirst().columns().getFirst().column().name());
        assertEquals(123L, insert.rows().getFirst().columns().getFirst().value());
        assertEquals("MY_VAR", insert.rows().getFirst().columns().getLast().column().name());
        assertEquals("testValue", insert.rows().getFirst().columns().getLast().value());
    }

    @Test
    void save_noChanges() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final Litebridge litebridge = new Litebridge(databaseProvider);
        final FieldSpec fieldSpecMyId = new FieldSpec("myId", false);
        final ColumnSpec columnSpecMyId = new ColumnSpec("MY_ID", true, "LB.TEST_SEQ", null);
        final FieldSpec fieldSpecMyVar = new FieldSpec("myVar", false);
        final ColumnSpec columnSpecMyVar = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldSpec, ColumnMapping> fieldColumnMap = Map.of(
                fieldSpecMyId, columnSpecMyId,
                fieldSpecMyVar, columnSpecMyVar);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final ColumnMetaData columnMetaDataMyId = new ColumnMetaData(tableSpec, "MY_ID", false, Types.NUMERIC, 10);
        final ColumnMetaData columnMetaDataMyVar = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        final TableMetaData tableMetaData = new TableMetaData(tableSpec, List.of("MY_ID"), List.of(columnMetaDataMyId, columnMetaDataMyVar));
        when(databaseProvider.getTableMetaData(tableSpec)).thenReturn(tableMetaData);

        litebridge.register(TestDto.class, tableSpec);
        final TestDto testDto = new TestDto();
        testDto.myId = 123L;
        testDto.myVar = "testValue";
        litebridge.track(testDto);

        // When
        litebridge.save(testDto);

        // Then
        verify(databaseProvider).getTableMetaData(tableSpec);
        verifyNoMoreInteractions(databaseProvider);
    }

    @Test
    void insert() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final Litebridge litebridge = new Litebridge(databaseProvider);
        final FieldSpec fieldSpecMyId = new FieldSpec("myId", false);
        final ColumnSpec columnSpecMyId = new ColumnSpec("MY_ID", true, "LB.TEST_SEQ", null);
        final FieldSpec fieldSpecMyVar = new FieldSpec("myVar", false);
        final ColumnSpec columnSpecMyVar = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldSpec, ColumnMapping> fieldColumnMap = Map.of(
                fieldSpecMyId, columnSpecMyId,
                fieldSpecMyVar, columnSpecMyVar);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final ColumnMetaData columnMetaDataMyId = new ColumnMetaData(tableSpec, "MY_ID", false, Types.NUMERIC, 10);
        final ColumnMetaData columnMetaDataMyVar = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        final TableMetaData tableMetaData = new TableMetaData(tableSpec, List.of("MY_ID"), List.of(columnMetaDataMyId, columnMetaDataMyVar));
        when(databaseProvider.getTableMetaData(tableSpec)).thenReturn(tableMetaData);
        when(databaseProvider.insert(any(Insert.class))).thenReturn(new InsertResult(1, List.of(123L)));
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());

        litebridge.register(TestDto.class, tableSpec);
        final TestDto testDto = new TestDto();
        testDto.myVar = "testValue";

        // When
        litebridge.insert(testDto);

        // Then
        verify(databaseProvider).getTableMetaData(tableSpec);
        final ArgumentCaptor<Insert> insertArgumentCaptor = ArgumentCaptor.forClass(Insert.class);
        verify(databaseProvider).insert(insertArgumentCaptor.capture());

        final Insert insert = insertArgumentCaptor.getValue();
        assertEquals(tableMetaData, insert.table());
        assertEquals(2, insert.columns().size());
        assertEquals("MY_ID", insert.columns().getFirst().name());
        assertEquals("MY_VAR", insert.columns().getLast().name());
        assertEquals(1, insert.rows().size());
        assertEquals(2, insert.rows().getFirst().columns().size());
        assertEquals("MY_ID", insert.rows().getFirst().columns().getFirst().column().name());
        assertNull(insert.rows().getFirst().columns().getFirst().value());
        assertEquals("MY_VAR", insert.rows().getFirst().columns().getLast().column().name());
        assertEquals("testValue", insert.rows().getFirst().columns().getLast().value());

        assertEquals(123L, testDto.myId);
    }

    @Test
    void update() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final Litebridge litebridge = new Litebridge(databaseProvider);
        final FieldSpec fieldSpecMyId = new FieldSpec("myId", false);
        final ColumnSpec columnSpecMyId = new ColumnSpec("MY_ID", true, "LB.TEST_SEQ", null);
        final FieldSpec fieldSpecMyVar = new FieldSpec("myVar", false);
        final ColumnSpec columnSpecMyVar = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldSpec, ColumnMapping> fieldColumnMap = Map.of(
                fieldSpecMyId, columnSpecMyId,
                fieldSpecMyVar, columnSpecMyVar);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final ColumnMetaData columnMetaDataMyId = new ColumnMetaData(tableSpec, "MY_ID", false, Types.NUMERIC, 10);
        final ColumnMetaData columnMetaDataMyVar = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        final TableMetaData tableMetaData = new TableMetaData(tableSpec, List.of("MY_ID"), List.of(columnMetaDataMyId, columnMetaDataMyVar));
        when(databaseProvider.getTableMetaData(tableSpec)).thenReturn(tableMetaData);

        litebridge.register(TestDto.class, tableSpec);
        final TestDto testDto = new TestDto();
        testDto.myId = 123L;
        testDto.myVar = "initialValue";
        litebridge.track(testDto);
        testDto.myVar = "updatedValue";

        // When
        litebridge.update(testDto);

        // Then
        verify(databaseProvider).getTableMetaData(tableSpec);
        final ArgumentCaptor<Update> updateArgumentCaptor = ArgumentCaptor.forClass(Update.class);
        verify(databaseProvider).update(updateArgumentCaptor.capture());

        final Update update = updateArgumentCaptor.getValue();
        assertEquals(tableMetaData, update.table());
        assertEquals(1, update.columnValues().size());
        assertEquals("MY_VAR", update.columnValues().getFirst().column().name());
        assertEquals("updatedValue", update.columnValues().getFirst().value());
        assertEquals(1, update.where().size());
        assertEquals("MY_ID", update.where().getFirst().column().name());
        assertEquals(123L, update.where().getFirst().value());
    }

    @Test
    void select_dto() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final Litebridge litebridge = new Litebridge(databaseProvider);
        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldSpec, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
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
        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldSpec, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
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
        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldSpec, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
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
        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldSpec, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
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
        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldSpec, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
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
        private Long myId;
        private String myVar;
    }
}
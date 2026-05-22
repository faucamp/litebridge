package org.litebridgedb.orm;

import org.junit.jupiter.api.Test;
import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.convert.DefaultTypeConverter;
import org.litebridgedb.db.spi.Aliased;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.impl.DefaultSequenceColumnValueGenerator;
import org.litebridgedb.db.spi.tx.ConnectionProvider;
import org.litebridgedb.db.spi.tx.TransactionManager;
import org.litebridgedb.db.spi.update.Insert;
import org.litebridgedb.db.spi.update.InsertResult;
import org.litebridgedb.db.spi.update.Update;
import org.litebridgedb.orm.api.dto.DtoFromClauseTerminal;
import org.litebridgedb.orm.api.spec.ColumnMapping;
import org.litebridgedb.orm.api.spec.ColumnSpec;
import org.litebridgedb.orm.api.spec.DtoTableSpec;
import org.litebridgedb.orm.api.spec.FieldMapping;
import org.litebridgedb.orm.api.spec.FieldSpec;
import org.litebridgedb.orm.api.spec.TableSpec;
import org.litebridgedb.orm.api.sql.SqlFromClause;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.tx.DefaultTransactionManager;
import org.mockito.ArgumentCaptor;

import javax.sql.DataSource;
import java.lang.invoke.MethodHandles;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class LitebridgeTest {

    @Test
    void register() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final TransactionManager transactionManager = new DefaultTransactionManager(dataSource);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldMapping, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final ColumnMetaData columnMetaData = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        final DtoTableSpec dtoTableSpec = new DtoTableSpec(TestDto.class, tableSpec);

        when(databaseProvider.tableMetaData(eq(tableSpec), any(ConnectionProvider.class))).thenReturn(new TableMetaData(tableSpec, List.of("MY_VAR"), List.of(columnMetaData)));

        // When
        litebridge.register(dtoTableSpec);

        // Then
        final TableRegistry tableRegistry = ObjectUtils.getFieldValue(litebridge, "tableRegistry", TableRegistry.class);
        final OrmTable result = tableRegistry.getTableOrThrow(TestDto.class);
        assertNotNull(result);
    }

    @Test
    void track() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final TransactionManager transactionManager = new DefaultTransactionManager(dataSource);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldMapping, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final ColumnMetaData columnMetaData = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        final DtoTableSpec dtoTableSpec = new DtoTableSpec(TestDto.class, tableSpec);
        when(databaseProvider.tableMetaData(eq(tableSpec), any(ConnectionProvider.class))).thenReturn(new TableMetaData(tableSpec, List.of("MY_VAR"), List.of(columnMetaData)));
        litebridge.register(dtoTableSpec);

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
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final FieldSpec fieldSpecMyId = new FieldSpec("myId", false);
        final ColumnSpec columnSpecMyId = new ColumnSpec("MY_ID", true, new DefaultSequenceColumnValueGenerator("LB.TEST_SEQ"), null);
        final FieldSpec fieldSpecMyVar = new FieldSpec("myVar", false);
        final ColumnSpec columnSpecMyVar = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldMapping, ColumnMapping> fieldColumnMap = Map.of(
                fieldSpecMyId, columnSpecMyId,
                fieldSpecMyVar, columnSpecMyVar);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final ColumnMetaData columnMetaDataMyId = new ColumnMetaData(tableSpec, "MY_ID", false, Types.NUMERIC, 10);
        final ColumnMetaData columnMetaDataMyVar = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        final TableMetaData tableMetaData = new TableMetaData(tableSpec, List.of("MY_ID"), List.of(columnMetaDataMyId, columnMetaDataMyVar));
        final DtoTableSpec dtoTableSpec = new DtoTableSpec(TestDto.class, tableSpec);
        when(databaseProvider.tableMetaData(eq(tableSpec), any(ConnectionProvider.class))).thenReturn(tableMetaData);
        when(databaseProvider.insert(any(Insert.class), any(ConnectionProvider.class))).thenReturn(new InsertResult(1, Collections.emptyMap()));

        litebridge.register(dtoTableSpec);
        final TestDto testDto = new TestDto();
        testDto.myId = 123L;
        testDto.myVar = "testValue";

        // When
        litebridge.save(testDto);

        // Then
        verify(databaseProvider).tableMetaData(eq(tableSpec), any(ConnectionProvider.class));
        final ArgumentCaptor<Insert> insertArgumentCaptor = ArgumentCaptor.forClass(Insert.class);
        verify(databaseProvider).insert(insertArgumentCaptor.capture(), any(ConnectionProvider.class));

        final Insert insert = insertArgumentCaptor.getValue();
        assertEquals(tableMetaData.toTable(), insert.table());
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
        final DataSource dataSource = mock(DataSource.class);
        final TransactionManager transactionManager = new DefaultTransactionManager(dataSource);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final FieldSpec fieldSpecMyId = new FieldSpec("myId", false);
        final ColumnSpec columnSpecMyId = new ColumnSpec("MY_ID", true, new DefaultSequenceColumnValueGenerator("LB.TEST_SEQ"), null);
        final FieldSpec fieldSpecMyVar = new FieldSpec("myVar", false);
        final ColumnSpec columnSpecMyVar = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldMapping, ColumnMapping> fieldColumnMap = Map.of(
                fieldSpecMyId, columnSpecMyId,
                fieldSpecMyVar, columnSpecMyVar);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final ColumnMetaData columnMetaDataMyId = new ColumnMetaData(tableSpec, "MY_ID", false, Types.NUMERIC, 10);
        final ColumnMetaData columnMetaDataMyVar = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        final TableMetaData tableMetaData = new TableMetaData(tableSpec, List.of("MY_ID"), List.of(columnMetaDataMyId, columnMetaDataMyVar));
        final DtoTableSpec dtoTableSpec = new DtoTableSpec(TestDto.class, tableSpec);
        when(databaseProvider.tableMetaData(eq(tableSpec), any(ConnectionProvider.class))).thenReturn(tableMetaData);

        litebridge.register(dtoTableSpec);
        final TestDto testDto = new TestDto();
        testDto.myId = 123L;
        testDto.myVar = "testValue";
        litebridge.track(testDto);

        // When
        litebridge.save(testDto);

        // Then
        verify(databaseProvider).tableMetaData(eq(tableSpec), any(ConnectionProvider.class));
        verifyNoMoreInteractions(databaseProvider);
    }

    @Test
    void insert() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final FieldSpec fieldSpecMyId = new FieldSpec("myId", false);
        final ColumnSpec columnSpecMyId = new ColumnSpec("MY_ID", true, new DefaultSequenceColumnValueGenerator("LB.TEST_SEQ"), null);
        final FieldSpec fieldSpecMyVar = new FieldSpec("myVar", false);
        final ColumnSpec columnSpecMyVar = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldMapping, ColumnMapping> fieldColumnMap = Map.of(
                fieldSpecMyId, columnSpecMyId,
                fieldSpecMyVar, columnSpecMyVar);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final ColumnMetaData columnMetaDataMyId = new ColumnMetaData(tableSpec, "MY_ID", false, Types.NUMERIC, 10);
        final ColumnMetaData columnMetaDataMyVar = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        final TableMetaData tableMetaData = new TableMetaData(tableSpec, List.of("MY_ID"), List.of(columnMetaDataMyId, columnMetaDataMyVar));
        final DtoTableSpec dtoTableSpec = new DtoTableSpec(TestDto.class, tableSpec);
        when(databaseProvider.tableMetaData(eq(tableSpec), any(ConnectionProvider.class))).thenReturn(tableMetaData);
        when(databaseProvider.insert(any(Insert.class), any(ConnectionProvider.class))).thenReturn(new InsertResult(1, Map.of(columnMetaDataMyId, 123L)));
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());

        litebridge.register(dtoTableSpec);
        final TestDto testDto = new TestDto();
        testDto.myVar = "testValue";

        // When
        litebridge.insert(testDto);

        // Then
        verify(databaseProvider).tableMetaData(eq(tableSpec), any(ConnectionProvider.class));
        final ArgumentCaptor<Insert> insertArgumentCaptor = ArgumentCaptor.forClass(Insert.class);
        verify(databaseProvider).insert(insertArgumentCaptor.capture(), any(ConnectionProvider.class));

        final Insert insert = insertArgumentCaptor.getValue();
        assertEquals(tableMetaData.toTable(), insert.table());
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
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final FieldSpec fieldSpecMyId = new FieldSpec("myId", false);
        final ColumnSpec columnSpecMyId = new ColumnSpec("MY_ID", true, new DefaultSequenceColumnValueGenerator("LB.TEST_SEQ"), null);
        final FieldSpec fieldSpecMyVar = new FieldSpec("myVar", false);
        final ColumnSpec columnSpecMyVar = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldMapping, ColumnMapping> fieldColumnMap = Map.of(
                fieldSpecMyId, columnSpecMyId,
                fieldSpecMyVar, columnSpecMyVar);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final ColumnMetaData columnMetaDataMyId = new ColumnMetaData(tableSpec, "MY_ID", false, Types.NUMERIC, 10);
        final ColumnMetaData columnMetaDataMyVar = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        final TableMetaData tableMetaData = new TableMetaData(tableSpec, List.of("MY_ID"), List.of(columnMetaDataMyId, columnMetaDataMyVar));
        final DtoTableSpec dtoTableSpec = new DtoTableSpec(TestDto.class, tableSpec);
        when(databaseProvider.tableMetaData(eq(tableSpec), any(ConnectionProvider.class))).thenReturn(tableMetaData);

        litebridge.register(dtoTableSpec);
        final TestDto testDto = new TestDto();
        testDto.myId = 123L;
        testDto.myVar = "initialValue";
        litebridge.track(testDto);
        testDto.myVar = "updatedValue";

        // When
        litebridge.update(testDto);

        // Then
        verify(databaseProvider).tableMetaData(eq(tableSpec), any(ConnectionProvider.class));
        final ArgumentCaptor<Update> updateArgumentCaptor = ArgumentCaptor.forClass(Update.class);
        verify(databaseProvider).update(updateArgumentCaptor.capture(), any(ConnectionProvider.class));

        final Update update = updateArgumentCaptor.getValue();
        assertEquals(tableMetaData.toTable(), update.table());
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
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldMapping, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final ColumnMetaData columnMetaData = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        final DtoTableSpec dtoTableSpec = new DtoTableSpec(TestDto.class, tableSpec);
        when(databaseProvider.tableMetaData(eq(tableSpec), any(ConnectionProvider.class))).thenReturn(new TableMetaData(tableSpec, List.of("MY_VAR"), List.of(columnMetaData)));
        litebridge.register(dtoTableSpec);

        // When
        final DtoFromClauseTerminal<TestDto> result = litebridge.select(TestDto.class);

        // Then
        assertNotNull(result);
    }

    @Test
    void select_columns() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldMapping, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final ColumnMetaData columnMetaData = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        final DtoTableSpec dtoTableSpec = new DtoTableSpec(TestDto.class, tableSpec);
        when(databaseProvider.tableMetaData(eq(tableSpec), any(ConnectionProvider.class))).thenReturn(new TableMetaData(tableSpec, List.of("MY_VAR"), List.of(columnMetaData)));
        litebridge.register(dtoTableSpec);

        // When
        final SqlFromClause result = litebridge.select("MY_VAR");

        // Then
        assertNotNull(result);
    }

    @Test
    void select_allColumns() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldMapping, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final ColumnMetaData columnMetaData = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        final DtoTableSpec dtoTableSpec = new DtoTableSpec(TestDto.class, tableSpec);
        when(databaseProvider.tableMetaData(eq(tableSpec), any(ConnectionProvider.class))).thenReturn(new TableMetaData(tableSpec, List.of("MY_VAR"), List.of(columnMetaData)));
        litebridge.register(dtoTableSpec);

        // When
        final SqlFromClause result = litebridge.select();

        // Then
        assertNotNull(result);
    }

    @Test
    void select_aliased() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldMapping, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final ColumnMetaData columnMetaData = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        final DtoTableSpec dtoTableSpec = new DtoTableSpec(TestDto.class, tableSpec);
        when(databaseProvider.tableMetaData(eq(tableSpec), any(ConnectionProvider.class))).thenReturn(new TableMetaData(tableSpec, List.of("MY_VAR"), List.of(columnMetaData)));
        litebridge.register(dtoTableSpec);

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
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldMapping, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final ColumnMetaData columnMetaData = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        when(databaseProvider.tableMetaData(eq(tableSpec), any(ConnectionProvider.class))).thenReturn(new TableMetaData(tableSpec, List.of("MY_VAR"), List.of(columnMetaData)));
        final DtoTableSpec dtoTableSpec = new DtoTableSpec(TestDto.class, tableSpec);
        litebridge.register(dtoTableSpec);
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());

        final Row row = new Row().withColumn(columnMetaData.toColumn(), "testValue");

        // When
        final TestDto result = litebridge.toDto(row, TestDto.class);

        // Then
        assertNotNull(result);
        assertEquals("testValue", result.myVar);
    }

    @Test
    void delete() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final FieldSpec fieldSpec = new FieldSpec("myId", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_ID", false, null, null);
        final Map<FieldMapping, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final ColumnMetaData idColumn = new ColumnMetaData(tableSpec, "MY_ID", false, Types.BIGINT);
        final DtoTableSpec dtoTableSpec = new DtoTableSpec(TestDto.class, tableSpec);
        when(databaseProvider.tableMetaData(eq(tableSpec), any(ConnectionProvider.class))).thenReturn(new TableMetaData(tableSpec, List.of("MY_ID"), List.of(idColumn)));
        litebridge.register(dtoTableSpec);

        final TestDto testDto = new TestDto();
        testDto.myId = 1L;

        // When
        litebridge.delete(testDto);

        // Then
        verify(databaseProvider).delete(any(), any());
    }

    @Test
    void delete_class() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR", false, null, null);
        final Map<FieldMapping, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final ColumnMetaData columnMetaData = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        final DtoTableSpec dtoTableSpec = new DtoTableSpec(TestDto.class, tableSpec);
        when(databaseProvider.tableMetaData(eq(tableSpec), any(ConnectionProvider.class))).thenReturn(new TableMetaData(tableSpec, List.of("MY_VAR"), List.of(columnMetaData)));
        litebridge.register(dtoTableSpec);

        // When
        litebridge.delete(TestDto.class);

        // Then
        verify(databaseProvider).delete(any(), any());
    }

    @Test
    void transaction() {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);

        // When
        final org.litebridgedb.orm.api.tx.TransactionContext result = litebridge.transaction();

        // Then
        assertNotNull(result);
    }

    @Test
    void constructors() {
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager transactionManager = mock(TransactionManager.class);

        assertNotNull(new Litebridge(databaseProvider, transactionManager));
        assertNotNull(new Litebridge(databaseProvider, transactionManager, MethodHandles.lookup()));
    }

    @Test
    void delete_overloads() throws Exception {
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);

        litebridge.delete("MY_TABLE");
        litebridge.delete("MY_TABLE", q -> q.where("COL").eq("VAL"));

        verify(databaseProvider, org.mockito.Mockito.atLeastOnce()).delete(any(), any());
    }

    @Test
    void update_overloads() throws Exception {
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);

        litebridge.update("MY_TABLE", q -> q.set("COL").to("VAL").where("ID").eq(1));

        verify(databaseProvider).update(any(), any());
    }

    private static class TestDto {
        private Long myId;
        private String myVar;
    }
}
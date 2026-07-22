package org.litebridge.orm;

import org.junit.jupiter.api.Test;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.alias.DefaultAliasTransformer;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.LiteralExpression;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.impl.DefaultSequenceColumnValueGenerator;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.Update;
import org.litebridge.orm.annotation.Column;
import org.litebridge.orm.annotation.Table;
import org.litebridge.orm.api.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.select.FromClauseStart;
import org.litebridge.orm.api.select.FromClauseStartTypeOverride;
import org.litebridge.orm.api.spec.ColumnMapping;
import org.litebridge.orm.api.spec.ColumnSpec;
import org.litebridge.orm.api.spec.DtoTableSpec;
import org.litebridge.orm.api.spec.FieldMapping;
import org.litebridge.orm.api.spec.FieldSpec;
import org.litebridge.orm.api.spec.TableSpec;
import org.litebridge.orm.api.tx.TransactionContext;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.engine.RegistrationEngine;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.TestColumnExpressionFactory;
import org.litebridge.orm.expression.TestSelectReferenceExpressionFactory;
import org.litebridge.orm.expression.function.aggregate.CountSpec;
import org.litebridge.orm.expression.intent.ConvertIntent;
import org.litebridge.orm.nativesql.NativeSqlContext;
import org.litebridge.orm.persistence.EntityDtoMapper;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.PersistenceFacade;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.tx.DefaultTransactionManager;
import org.mockito.ArgumentCaptor;

import javax.sql.DataSource;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR");
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
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR");
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
        final ColumnSpec columnSpecMyId = new ColumnSpec("MY_ID", new DefaultSequenceColumnValueGenerator("LB.TEST_SEQ"), null);
        final FieldSpec fieldSpecMyVar = new FieldSpec("myVar", false);
        final ColumnSpec columnSpecMyVar = new ColumnSpec("MY_VAR");
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
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final FieldSpec fieldSpecMyId = new FieldSpec("myId", false);
        final ColumnSpec columnSpecMyId = new ColumnSpec("MY_ID", new DefaultSequenceColumnValueGenerator("LB.TEST_SEQ"), null);
        final FieldSpec fieldSpecMyVar = new FieldSpec("myVar", false);
        final ColumnSpec columnSpecMyVar = new ColumnSpec("MY_VAR");
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
        final ColumnSpec columnSpecMyId = new ColumnSpec("MY_ID", new DefaultSequenceColumnValueGenerator("LB.TEST_SEQ"), null);
        final FieldSpec fieldSpecMyVar = new FieldSpec("myVar", false);
        final ColumnSpec columnSpecMyVar = new ColumnSpec("MY_VAR");
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
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
        when(selectRegistry.column()).thenReturn(new TestColumnExpressionFactory());
        when(selectRegistry.literal()).thenReturn(LiteralExpression::new);
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final FieldSpec fieldSpecMyId = new FieldSpec("myId", false);
        final ColumnSpec columnSpecMyId = new ColumnSpec("MY_ID", new DefaultSequenceColumnValueGenerator("LB.TEST_SEQ"), null);
        final FieldSpec fieldSpecMyVar = new FieldSpec("myVar", false);
        final ColumnSpec columnSpecMyVar = new ColumnSpec("MY_VAR");
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
        assertEquals(1, update.where().conditions().size());
        assertInstanceOf(ColumnExpression.class, update.where().conditions().getFirst().condition().lhs());
        assertEquals("MY_ID", ((ColumnExpression) update.where().conditions().getFirst().condition().lhs()).column().name());
        assertEquals(123L, ((LiteralExpression) update.where().conditions().getFirst().condition().rhs()).value());
    }

    @Test
    void select_dto() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
        when(selectRegistry.column()).thenReturn(new TestColumnExpressionFactory());
        when(selectRegistry.literal()).thenReturn(LiteralExpression::new);
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        when(databaseProvider.getAliasTransformer()).thenReturn(new DefaultAliasTransformer());
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR");
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
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
        when(selectRegistry.column()).thenReturn(new TestColumnExpressionFactory());
        when(selectRegistry.literal()).thenReturn(LiteralExpression::new);
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR");
        final Map<FieldMapping, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final ColumnMetaData columnMetaData = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        final DtoTableSpec dtoTableSpec = new DtoTableSpec(TestDto.class, tableSpec);
        when(databaseProvider.tableMetaData(eq(tableSpec), any(ConnectionProvider.class))).thenReturn(new TableMetaData(tableSpec, List.of("MY_VAR"), List.of(columnMetaData)));
        litebridge.register(dtoTableSpec);

        // When
        final FromClauseStart result = litebridge.select("MY_VAR");

        // Then
        assertNotNull(result);
    }

    @Test
    void select_allColumns() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
        when(selectRegistry.column()).thenReturn(new TestColumnExpressionFactory());
        when(selectRegistry.literal()).thenReturn(LiteralExpression::new);
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR");
        final Map<FieldMapping, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final ColumnMetaData columnMetaData = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        final DtoTableSpec dtoTableSpec = new DtoTableSpec(TestDto.class, tableSpec);
        when(databaseProvider.tableMetaData(eq(tableSpec), any(ConnectionProvider.class))).thenReturn(new TableMetaData(tableSpec, List.of("MY_VAR"), List.of(columnMetaData)));
        litebridge.register(dtoTableSpec);

        // When
        final FromClauseStart result = litebridge.select();

        // Then
        assertNotNull(result);
    }

//    @Test
//    void select_aliased() throws Exception {
//        // Given
//        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
//        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
//        when(sqlFunctionRegistry.selectColumnFactory()).thenReturn(new TestColumnExpressionFactory());
//        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
//        final DataSource dataSource = mock(DataSource.class);
//        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
//        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
//        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR");
//        final Map<FieldMapping, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
//        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
//        final ColumnMetaData columnMetaData = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
//        final DtoTableSpec dtoTableSpec = new DtoTableSpec(TestDto.class, tableSpec);
//        when(databaseProvider.tableMetaData(eq(tableSpec), any(ConnectionProvider.class))).thenReturn(new TableMetaData(tableSpec, List.of("MY_VAR"), List.of(columnMetaData)));
//        litebridge.register(dtoTableSpec);
//
//        final Aliased aliased = new Aliased("TEST_COLUMN", "testAlias");
//
//        // When
//        final FromClauseStart result = litebridge.select(aliased);
//
//        // Then
//        assertNotNull(result);
//    }

    @Test
    void toDto() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR");
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
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
        when(selectRegistry.column()).thenReturn(new TestColumnExpressionFactory());
        when(selectRegistry.literal()).thenReturn(LiteralExpression::new);
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final FieldSpec fieldSpec = new FieldSpec("myId", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_ID");
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
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR");
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
    void nativeSql() {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);

        // When
        final NativeSqlContext result = litebridge.nativeSql();

        // Then
        assertNotNull(result);
    }

    @Test
    void transaction() {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);

        // When
        final TransactionContext result = litebridge.transaction();

        // Then
        assertNotNull(result);
    }

    @Test
    void constructors() {
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final DataSource dataSource = mock(DataSource.class);
        final LitebridgeConfig config = new LitebridgeConfig();
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        assertNotNull(new Litebridge(databaseProvider, dataSource));
        assertNotNull(new Litebridge(databaseProvider, dataSource, config));
        assertNotNull(new Litebridge(databaseProvider, dataSource, config, lookup));
        assertNotNull(new Litebridge(databaseProvider, transactionManager));
        assertNotNull(new Litebridge(databaseProvider, transactionManager, config));
        assertNotNull(new Litebridge(databaseProvider, transactionManager, lookup));
        assertNotNull(new Litebridge(databaseProvider, transactionManager, config, lookup));
    }

    @Test
    void register_entityClasses() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final RegistrationEngine registrationEngine = mock(RegistrationEngine.class);
        setFieldValue(litebridge, "registrationEngine", registrationEngine);

        // When
        assertThrows(IllegalArgumentException.class, () -> litebridge.register(new Class<?>[0]));
        litebridge.register(new Class<?>[]{TestEntity.class});

        // Then
        verify(registrationEngine).register(eq(new Class<?>[]{TestEntity.class}));
    }

    @Test
    void register_dtoTableSpecs() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final RegistrationEngine registrationEngine = mock(RegistrationEngine.class);
        setFieldValue(litebridge, "registrationEngine", registrationEngine);
        final DtoTableSpec spec = mock(DtoTableSpec.class);

        // When
        litebridge.register(new DtoTableSpec[]{spec});

        // Then
        verify(registrationEngine).register(any(DtoTableSpec[].class));
    }

    @Test
    void register_lambda() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final RegistrationEngine registrationEngine = mock(RegistrationEngine.class);
        setFieldValue(litebridge, "registrationEngine", registrationEngine);

        // When
        litebridge.register(TestDto.class, rc -> rc.mapToTable("TEST"));

        // Then
        verify(registrationEngine).register(eq(TestDto.class), any(Function.class));
    }

    @Test
    void track_notRegistered() {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final TestDto dto = new TestDto();

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> litebridge.track(dto));
        assertThrows(NullPointerException.class, () -> litebridge.track(null));
    }

    @Test
    void save_multiple() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final PersistenceFacade persistenceFacade = mock(PersistenceFacade.class);
        setFieldValue(litebridge, "persistenceFacade", persistenceFacade);

        final TestDto dto1 = new TestDto();
        final TestDto dto2 = new TestDto();

        // When
        litebridge.save(dto1, dto2);

        // Then
        verify(persistenceFacade).save(any(Collection.class));
    }

    @Test
    void save_collection() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final PersistenceFacade persistenceFacade = mock(PersistenceFacade.class);
        setFieldValue(litebridge, "persistenceFacade", persistenceFacade);

        final List<Object> dtos = List.of(new TestDto());

        // When
        litebridge.save(dtos);

        // Then
        verify(persistenceFacade).save(dtos);
    }

    @Test
    void save_exception() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final PersistenceFacade persistenceFacade = mock(PersistenceFacade.class);
        setFieldValue(litebridge, "persistenceFacade", persistenceFacade);
        org.mockito.Mockito.doThrow(new SQLException("Test")).when(persistenceFacade).save(any(Object.class));

        // When / Then
        assertThrows(IllegalStateException.class, () -> litebridge.save(new Object()));
    }

    @Test
    void save_collection_exception() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final PersistenceFacade persistenceFacade = mock(PersistenceFacade.class);
        setFieldValue(litebridge, "persistenceFacade", persistenceFacade);
        org.mockito.Mockito.doThrow(new SQLException("Test")).when(persistenceFacade).save(any(Collection.class));

        // When / Then
        assertThrows(IllegalStateException.class, () -> litebridge.save(List.of()));
    }

    @Test
    void insert_exception() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final PersistenceFacade persistenceFacade = mock(PersistenceFacade.class);
        setFieldValue(litebridge, "persistenceFacade", persistenceFacade);
        org.mockito.Mockito.doThrow(new SQLException("Test")).when(persistenceFacade).insert(any(Object.class));

        // When / Then
        assertThrows(IllegalStateException.class, () -> litebridge.insert(new Object()));
    }

    @Test
    void update_exception() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final PersistenceFacade persistenceFacade = mock(PersistenceFacade.class);
        setFieldValue(litebridge, "persistenceFacade", persistenceFacade);
        org.mockito.Mockito.doThrow(new SQLException("Test")).when(persistenceFacade).update(any(Object.class));

        // When / Then
        assertThrows(IllegalStateException.class, () -> litebridge.update(new Object()));
    }

    @Test
    void delete_exception() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final PersistenceFacade persistenceFacade = mock(PersistenceFacade.class);
        setFieldValue(litebridge, "persistenceFacade", persistenceFacade);
        org.mockito.Mockito.doThrow(new SQLException("Test")).when(persistenceFacade).delete(any(Object.class));

        // When / Then
        assertThrows(IllegalStateException.class, () -> litebridge.delete(new Object()));
    }

    @Test
    void select_dto_context() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final FromClauseEngine fromClauseEngine = mock(FromClauseEngine.class);
        setFieldValue(litebridge, "fromClauseEngine", fromClauseEngine);

        // When
        litebridge.select(TestDto.class, String.class);

        // Then
        verify(fromClauseEngine).from(eq(TestDto.class), eq(String.class));
    }

    @Test
    void select_expressions() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final ExpressionSpec expression = new org.litebridge.orm.meta.QueryField(TestDto.class, "myVar");

        // When
        final FromClauseStart result = litebridge.select(new ExpressionSpec[]{expression});

        // Then
        assertNotNull(result);
    }

    @Test
    void select_typeOverride() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);

        final CountSpec override = new CountSpec();

        // When
        final FromClauseStartTypeOverride<Long> result = litebridge.select(override);

        // Then
        assertNotNull(result);
    }

    @Test
    void select_convertIntent() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);

        final ConvertIntent<String> convertIntent = new ConvertIntent<>(new ExpressionSpec[0], String.class);

        // When
        final FromClauseStartTypeOverride<String> result = litebridge.select(convertIntent);

        // Then
        assertNotNull(result);
    }

    @Test
    void toDto_empty() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);

        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR");
        final Map<FieldMapping, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final ColumnMetaData columnMetaData = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        when(databaseProvider.tableMetaData(eq(tableSpec), any(ConnectionProvider.class))).thenReturn(new TableMetaData(tableSpec, List.of("MY_VAR"), List.of(columnMetaData)));
        final DtoTableSpec dtoTableSpec = new DtoTableSpec(TestDto.class, tableSpec);
        litebridge.register(dtoTableSpec);
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());

        final Row row = new Row(); // Empty row
        
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> litebridge.toDto(row, TestDto.class));
    }

    @Test
    void entityDtoMapper() {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);

        // When
        final EntityDtoMapper<TestDto> mapper = litebridge.entityDtoMapper(TestDto.class, List.of());

        // Then
        assertNotNull(mapper);
    }
    
    @Test
    void update_lambda() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        
        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR");
        final Map<FieldMapping, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final ColumnMetaData columnMetaData = new ColumnMetaData(tableSpec, "MY_VAR", false, Types.VARCHAR, 10);
        when(databaseProvider.tableMetaData(eq(tableSpec), any(ConnectionProvider.class))).thenReturn(new TableMetaData(tableSpec, List.of("MY_VAR"), List.of(columnMetaData)));
        final DtoTableSpec dtoTableSpec = new DtoTableSpec(TestDto.class, tableSpec);
        litebridge.register(dtoTableSpec);

        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
        when(selectRegistry.column()).thenReturn(new TestColumnExpressionFactory());
        when(selectRegistry.literal()).thenReturn(LiteralExpression::new);
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        when(databaseProvider.getAliasTransformer()).thenReturn(new DefaultAliasTransformer());

        // When
        litebridge.update(TestDto.class, u -> u.set("myVar").to("newVal"));

        // Then
        verify(databaseProvider).update(any(), any());
    }

    @Test
    void delete_overloads() throws Exception {
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
        when(selectRegistry.column()).thenReturn(new TestColumnExpressionFactory());
        when(selectRegistry.reference()).thenReturn(new TestSelectReferenceExpressionFactory());
        when(selectRegistry.literal()).thenReturn(LiteralExpression::new);
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);

        litebridge.delete("MY_TABLE");
        litebridge.delete("MY_TABLE", q -> q.where("COL").eq("VAL"));

        verify(databaseProvider, org.mockito.Mockito.atLeastOnce()).delete(any(), any());
    }

    @Test
    void update_overloads() throws Exception {
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
        when(selectRegistry.column()).thenReturn(new TestColumnExpressionFactory());
        when(selectRegistry.reference()).thenReturn(new TestSelectReferenceExpressionFactory());
        when(selectRegistry.literal()).thenReturn(LiteralExpression::new);
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);

        litebridge.update("MY_TABLE", q -> q.set("COL").to("VAL").where("ID").eq(1));

        verify(databaseProvider).update(any(), any());
    }

    private static void setFieldValue(final Object obj, final String fieldName, final Object value) throws Exception {
        final java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    private static class TestDto {
        private Long myId;
        private String myVar;
    }

    @Table("TEST_ENTITY")
    private static class TestEntity {
        @Column("ID")
        private Long id;
    }
}
package org.litebridge.orm;

import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;
import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.impl.expression.BindValueExpressionImpl;
import org.litebridge.db.spi.impl.expression.LiteralExpressionImpl;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.spec.ColumnMapping;
import org.litebridge.orm.api.spec.ColumnSpec;
import org.litebridge.orm.api.spec.DtoTableSpec;
import org.litebridge.orm.api.spec.FieldMapping;
import org.litebridge.orm.api.spec.FieldSpec;
import org.litebridge.orm.api.spec.TableSpec;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.expression.TestColumnExpressionFactory;
import org.litebridge.orm.persistence.PersistenceFacade;

import javax.sql.DataSource;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.litebridge.orm.util.DatabaseProviderTestUtil.mockDatabaseProviderWithMetaData;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LitebridgeTest {

    private static final LabelGenerator labelGenerator = new LabelGenerator();

    @Test
    void constructors() {
        // Given
        final DatabaseProvider databaseProvider = mockDatabaseProviderWithMetaData();
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final DataSource dataSource = mock(DataSource.class);
        final LitebridgeConfig config = new LitebridgeConfig();
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        // When / Then
        assertNotNull(new Litebridge(databaseProvider, dataSource));
        assertNotNull(new Litebridge(databaseProvider, dataSource, config));
        assertNotNull(new Litebridge(databaseProvider, dataSource, config, lookup));
        assertNotNull(new Litebridge(databaseProvider, dataSource, null, lookup));
        assertNotNull(new Litebridge(databaseProvider, transactionManager));
        assertNotNull(new Litebridge(databaseProvider, transactionManager, config));
        assertNotNull(new Litebridge(databaseProvider, transactionManager, lookup));
        assertNotNull(new Litebridge(databaseProvider, transactionManager, config, lookup));
        assertNotNull(new Litebridge(databaseProvider, transactionManager, null, lookup));
    }

    @Test
    void merge() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mockDatabaseProviderWithMetaData();
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final PersistenceFacade persistenceFacade = mock(PersistenceFacade.class);
        setFieldValue(litebridge, "persistenceFacade", persistenceFacade);

        final TestDto testDto = new TestDto();

        // When
        litebridge.merge(testDto);

        // Then
        verify(persistenceFacade).merge(testDto);
    }

    @Test
    void merge_exception() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mockDatabaseProviderWithMetaData();
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);
        final PersistenceFacade persistenceFacade = mock(PersistenceFacade.class);
        setFieldValue(litebridge, "persistenceFacade", persistenceFacade);
        doThrow(new SQLException("Test")).when(persistenceFacade).merge(any(Object.class));

        final TestDto testDto = new TestDto();

        // When / Then
        final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> litebridge.merge(testDto));
        assertEquals("Failed to merge DTO: " + testDto, exception.getMessage());
    }

    @Test
    void mergeInto_sql() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mockDatabaseProviderWithMetaData();
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
        when(selectRegistry.column()).thenReturn(new TestColumnExpressionFactory());
        when(selectRegistry.literal()).thenReturn((value, alias) -> new LiteralExpressionImpl(value, alias, labelGenerator));
        when(selectRegistry.bindValue()).thenReturn((index, size, columnType, alias) -> new BindValueExpressionImpl(index, size, columnType, alias, labelGenerator));
        when(databaseProvider.sqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        when(databaseProvider.typeConverter()).thenReturn(new DefaultTypeConverter());
        final DatabaseProviderMetaData providerMetaData = new DatabaseProviderMetaData(true, DatabaseProviderMetaData.MergeCapability.USING_VALUES, DatabaseProviderMetaData.InsertCapability.NATIVE_MULTIROW);
        when(databaseProvider.metaData()).thenReturn(providerMetaData);
        final TableMetaData tableMetaData = mock(TableMetaData.class);
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(tableMetaData);
        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(tableMetaData.hasColumn(anyString())).thenReturn(true);
        when(tableMetaData.column(anyString())).thenReturn(columnMetaData);
        when(columnMetaData.getDataType()).thenReturn(Types.VARCHAR);
        when(tableMetaData.table()).thenReturn(new Table("MY_TABLE"));
        when(databaseProvider.executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), any(ConnectionProvider.class))).thenReturn(new UpdateResult(1));

        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);

        // When
        final UpdateResult result = litebridge.mergeInto("MY_TABLE", m -> m.using("OTHER_TABLE").on("ID").eq(1).whenMatched(u -> u.update(us -> us.set("COL").to("VAL"))));

        // Then
        assertNotNull(result);
        verify(databaseProvider).executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), any(ConnectionProvider.class));
    }

    @Test
    void mergeInto_dto() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mockDatabaseProviderWithMetaData();
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
        when(selectRegistry.column()).thenReturn(new TestColumnExpressionFactory());
        when(selectRegistry.literal()).thenReturn((value, alias) -> new LiteralExpressionImpl(value, alias, labelGenerator));
        when(selectRegistry.bindValue()).thenReturn((index, size, columnType, alias) -> new BindValueExpressionImpl(index, size, columnType, alias, labelGenerator));
        when(databaseProvider.sqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        when(databaseProvider.typeConverter()).thenReturn(new DefaultTypeConverter());
        final DatabaseProviderMetaData providerMetaData = new DatabaseProviderMetaData(true, DatabaseProviderMetaData.MergeCapability.USING_VALUES, DatabaseProviderMetaData.InsertCapability.NATIVE_MULTIROW);
        when(databaseProvider.metaData()).thenReturn(providerMetaData);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);

        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR");
        final Map<FieldMapping, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR, 10);
        final TableMetaData tableMetaData = new TableMetaData(table, List.of("MY_VAR"), List.of(columnMetaData));
        when(databaseProvider.tableMetaData(eq(table), any(ConnectionProvider.class))).thenReturn(tableMetaData);
        when(databaseProvider.tableMetaData(any(Table.class), any(TransactionManager.class))).thenReturn(tableMetaData);
        when(databaseProvider.executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), any(ConnectionProvider.class))).thenReturn(new UpdateResult(1));

        final DtoTableSpec dtoTableSpec = new DtoTableSpec(TestDto.class, tableSpec);
        litebridge.register(dtoTableSpec);

        // When
        final UpdateResult result = litebridge.mergeInto(TestDto.class, m -> m.using(TestDto.class).on("myVar").eq("VAL").whenMatched(u -> u.update(us -> us.set("myVar").to("newVal"))));

        // Then
        assertNotNull(result);
        verify(databaseProvider).executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), any(ConnectionProvider.class));
    }

    @Test
    void mergeInto_dto_withContextDtoClass() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = mockDatabaseProviderWithMetaData();
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
        when(selectRegistry.column()).thenReturn(new TestColumnExpressionFactory());
        when(selectRegistry.literal()).thenReturn((value, alias) -> new LiteralExpressionImpl(value, alias, labelGenerator));
        when(selectRegistry.bindValue()).thenReturn((index, size, columnType, alias) -> new BindValueExpressionImpl(index, size, columnType, alias, labelGenerator));
        when(databaseProvider.sqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        when(databaseProvider.typeConverter()).thenReturn(new DefaultTypeConverter());
        final DatabaseProviderMetaData providerMetaData = new DatabaseProviderMetaData(true, DatabaseProviderMetaData.MergeCapability.USING_VALUES, DatabaseProviderMetaData.InsertCapability.NATIVE_MULTIROW);
        when(databaseProvider.metaData()).thenReturn(providerMetaData);
        final DataSource dataSource = mock(DataSource.class);
        final Litebridge litebridge = new Litebridge(databaseProvider, dataSource);

        final FieldSpec fieldSpec = new FieldSpec("myVar", false);
        final ColumnSpec columnSpec = new ColumnSpec("MY_VAR");
        final Map<FieldMapping, ColumnMapping> fieldColumnMap = Map.of(fieldSpec, columnSpec);
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnMap);
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR, 10);
        final TableMetaData tableMetaData = new TableMetaData(table, List.of("MY_VAR"), List.of(columnMetaData));
        when(databaseProvider.tableMetaData(eq(table), any(ConnectionProvider.class))).thenReturn(tableMetaData);
        when(databaseProvider.tableMetaData(any(Table.class), any(TransactionManager.class))).thenReturn(tableMetaData);
        when(databaseProvider.executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), any(ConnectionProvider.class))).thenReturn(new UpdateResult(1));

        final DtoTableSpec dtoTableSpec = new DtoTableSpec(TestDto.class, tableSpec);
        litebridge.register(dtoTableSpec);

        // When
        final UpdateResult result = litebridge.mergeInto(TestDto.class, TestContextDto.class, m -> m.using(TestDto.class).on("myVar").eq("VAL").whenMatched(u -> u.update(us -> us.set("myVar").to("newVal"))));

        // Then
        assertNotNull(result);
        verify(databaseProvider).executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), any(ConnectionProvider.class));
    }

    private static void setFieldValue(final Object obj, final String fieldName, final Object value) throws Exception {
        final Field field = ClassUtils.getField(obj.getClass(), fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    private static class TestDto {
        private Long myId;
        private String myVar;
    }

    private static class TestContextDto {
        private String extra;
    }
}

package org.litebridge.orm.api.dto;

import org.junit.jupiter.api.Test;
import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.expression.ColumnExpressionFactory;
import org.litebridge.db.spi.expression.LiteralExpressionFactory;
import org.litebridge.db.spi.expression.SelectReferenceExpressionFactory;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.SelectNode;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.expression.select.SelectFieldSpec;
import org.litebridge.orm.persistence.DtoConstructor;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.orm.persistence.alias.NoOpAliasGenerator;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;

import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DtoSelectorTest {

    private LitebridgeContext createMockContext() {
        final LitebridgeConfig config = new LitebridgeConfig();
        final FromClauseEngine fromClauseEngine = mock(FromClauseEngine.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select select = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(select);
        when(select.column()).thenReturn(mock(ColumnExpressionFactory.class));
        when(select.reference()).thenReturn(mock(SelectReferenceExpressionFactory.class));
        when(select.literal()).thenReturn(mock(LiteralExpressionFactory.class));
        return new LitebridgeContext(config, fromClauseEngine, sqlFunctionRegistry, mock(QueryPlanCache.class), new NoOpAliasGenerator(), mock(TableMetaDataCache.class), new DefaultTypeConverter());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSelect() {
        // Given
        final OrmTable ormTable = mock(OrmTable.class);
        final Table table = new Table("", null, "TEST", "t1");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(table);
        when(ormTable.dtoClass()).thenReturn((Class) Object.class);

        final DtoSelector<Object> selector = new DtoSelector<>(Object.class, ormTable, mock(TableRegistry.class), mock(ClassFieldAccessorCache.class), mock(DtoConstructor.class), mock(TransactionalDatabaseProvider.class), new NoOpAliasGenerator(), createMockContext(), null);

        // When
        final SelectColumnSpec expr = new SelectColumnSpec(new org.litebridge.db.spi.Column(table, "COL"));
        final DtoFromClauseTerminal<Object> terminal = selector.select(expr);

        // Then
        assertNotNull(terminal);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSelectAll() {
        // Given
        final OrmTable ormTable = mock(OrmTable.class);
        final Table table = new Table("", null, "TEST", "t1");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(table);
        when(ormTable.dtoClass()).thenReturn((Class) Object.class);

        final ColumnMetaData col1 = new ColumnMetaData(table, "COL1", true, Types.VARCHAR);
        final FieldAccessor field1 = mock(FieldAccessor.class);
        when(field1.name()).thenReturn("field1");
        when(ormTable.mappedFieldTargets()).thenReturn(List.of(Map.entry(field1, (MappedFieldTarget) col1)));
        when(ormTable.getFieldForColumnName("COL1")).thenReturn(field1);

        final DtoSelector<Object> selector = new DtoSelector<>(Object.class, ormTable, mock(TableRegistry.class), mock(ClassFieldAccessorCache.class), mock(DtoConstructor.class), mock(TransactionalDatabaseProvider.class), new NoOpAliasGenerator(), createMockContext(), null);

        // When
        final DtoFromClauseTerminal<Object> terminal = selector.select();

        // Then
        assertNotNull(terminal);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testListAndUnwrap() throws Exception {
        // Given
        final OrmTable ormTable = mock(OrmTable.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        when(databaseProvider.getTypeConverter()).thenReturn(new org.litebridge.db.spi.convert.TypeConverter() {
            @Override
            public <T> T convert(Object value, Class<T> targetType) {
                return (T) Integer.valueOf(123);
            }

            @Override
            public Object convert(Object value, int dbDataType) {
                return 123;
            }

            @Override
            public Class<?> getClassForSqlType(int sqlType) {
                return Object.class;
            }

            @Override
            public int getSqlDataType(Class<?> fieldType) {
                return 0;
            }
        });
        when(databaseProvider.getAliasTransformer()).thenReturn(new org.litebridge.db.spi.alias.DefaultAliasTransformer());

        final Table table = new Table("", null, "TEST", "t1");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(table);
        when(ormTable.dtoClass()).thenReturn((Class) String.class);
        when(ormTable.getDtoClassInterfaces()).thenReturn(Set.of());

        final DtoSelector<Integer> selector = new DtoSelector<>(Integer.class, ormTable, mock(TableRegistry.class), mock(ClassFieldAccessorCache.class), mock(DtoConstructor.class), databaseProvider, new NoOpAliasGenerator(), createMockContext(), null);
        final org.litebridge.orm.api.dto.DtoFromClauseTerminal<Integer> terminal = selector.select();

        final Row row = new Row().withColumn(new Column(table, "COL"), "123");
        when(databaseProvider.toSql(any(), any())).thenReturn("SELECT 1");
        when(databaseProvider.select(any(), any())).thenReturn(List.of(row));

        // When
        final List<Integer> results = terminal.list();

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        Object first = results.get(0);
        assertEquals(Integer.class, first.getClass());
        assertEquals(123, first);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFetchOneDtoErrors() throws Exception {
        // Given
        final OrmTable ormTable = mock(OrmTable.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        when(databaseProvider.getTypeConverter()).thenReturn(new org.litebridge.convert.DefaultTypeConverter());
        when(databaseProvider.getAliasTransformer()).thenReturn(new org.litebridge.db.spi.alias.DefaultAliasTransformer());

        final Table table = new Table("", null, "TEST", "t1");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(table);
        when(ormTable.dtoClass()).thenReturn((Class) String.class);
        when(ormTable.getDtoClassInterfaces()).thenReturn(Set.of());

        final FieldAccessor field1 = mock(FieldAccessor.class);
        when(field1.name()).thenReturn("field1");
        when(field1.type()).thenReturn((Class) String.class);
        when(field1.dtoClass()).thenReturn((Class) String.class);
        final ColumnMetaData col1 = new ColumnMetaData(table, "COL1", true, Types.VARCHAR);

        // Define a Primary Key to ensure rows are treated as distinct DTOs
        final ColumnMetaData pkCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        when(metaData.primaryKey()).thenReturn(List.of(pkCol));
        when(ormTable.getColumnMetaData("ID")).thenReturn(pkCol);
        when(ormTable.getFieldForColumnName("ID")).thenReturn(field1);

        when(ormTable.mappedFieldTargets()).thenReturn(List.of(Map.entry(field1, (MappedFieldTarget) pkCol), Map.entry(field1, (MappedFieldTarget) col1)));
        when(ormTable.getFieldForColumnName("COL1")).thenReturn(field1);

        final DtoConstructor constructor = mock(DtoConstructor.class);
        when(constructor.newInstance(any(), any())).thenReturn(new DtoConstructor.ConstructionResult<>("result1", true), new DtoConstructor.ConstructionResult<>("result2", true));

        final DtoSelector<String> selector = new DtoSelector<>(String.class, ormTable, mock(TableRegistry.class), mock(ClassFieldAccessorCache.class), constructor, databaseProvider, new NoOpAliasGenerator(), createMockContext(), null);
        selector.select();

        // Return 2 rows with different PK values to trigger error in oneOrNull
        final Row row1 = new Row().withColumn(pkCol.toColumn(), 1L).withColumn(col1.toColumn(), "1");
        final Row row2 = new Row().withColumn(pkCol.toColumn(), 2L).withColumn(col1.toColumn(), "2");
        when(databaseProvider.toSql(any(), any())).thenReturn("SELECT 1");
        when(databaseProvider.select(any(), any())).thenReturn(List.of(row1, row2));

        // When / Then
        assertThrows(IllegalStateException.class, selector::oneOrNull);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFirstOrNull() throws Exception {
        // Given
        final OrmTable ormTable = mock(OrmTable.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        when(databaseProvider.getTypeConverter()).thenReturn(new org.litebridge.convert.DefaultTypeConverter());
        when(databaseProvider.getAliasTransformer()).thenReturn(new org.litebridge.db.spi.alias.DefaultAliasTransformer());

        final Table table = new Table("", null, "TEST", "t1");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(table);
        when(ormTable.dtoClass()).thenReturn((Class) String.class);
        when(ormTable.getDtoClassInterfaces()).thenReturn(Set.of());
        when(metaData.primaryKey()).thenReturn(List.of());

        final FieldAccessor field1 = mock(FieldAccessor.class);
        when(field1.name()).thenReturn("field1");
        when(field1.type()).thenReturn((Class) String.class);
        when(field1.dtoClass()).thenReturn((Class) String.class);
        final ColumnMetaData col1 = new ColumnMetaData(table, "COL1", true, Types.VARCHAR);
        when(ormTable.getColumnMetaData("COL1")).thenReturn(col1);
        when(ormTable.mappedFieldTargets()).thenReturn(List.of(Map.entry(field1, (MappedFieldTarget) col1)));
        when(ormTable.getFieldForColumnName("COL1")).thenReturn(field1);

        final DtoConstructor constructor = mock(DtoConstructor.class);
        when(constructor.newInstance(any(), any())).thenReturn(new DtoConstructor.ConstructionResult<>("result", true));

        final DtoSelector<String> selector = new DtoSelector<>(String.class, ormTable, mock(TableRegistry.class), mock(ClassFieldAccessorCache.class), constructor, databaseProvider, new NoOpAliasGenerator(), createMockContext(), null);
        final org.litebridge.orm.api.dto.DtoFromClauseTerminal<String> terminal = selector.select(new SelectFieldSpec(field1, col1.toColumn()));

        final Row row1 = new Row().withColumn(col1.toColumn(), "1");
        when(databaseProvider.toSql(any(), any())).thenReturn("SELECT 1");
        when(databaseProvider.select(any(), any())).thenReturn(List.of(row1));

        // When
        final String result = terminal.firstOrNull();

        // Then
        assertEquals("result", result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testCaching() throws Exception {
        // Given
        final OrmTable ormTable = mock(OrmTable.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        when(databaseProvider.getTypeConverter()).thenReturn(new org.litebridge.convert.DefaultTypeConverter());
        when(databaseProvider.getAliasTransformer()).thenReturn(new org.litebridge.db.spi.alias.DefaultAliasTransformer());

        final Table table = new Table("", null, "TEST", "t1");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.name()).thenReturn("TEST");
        when(metaData.toTable()).thenReturn(table);
        when(ormTable.dtoClass()).thenReturn((Class) String.class);
        when(ormTable.getDtoClassInterfaces()).thenReturn(Set.of());

        final QueryPlanCache cache = new QueryPlanCache();
        final LitebridgeContext context = new LitebridgeContext(new LitebridgeConfig(), mock(FromClauseEngine.class), mock(SqlFunctionRegistry.class), cache, new NoOpAliasGenerator(), mock(TableMetaDataCache.class), new DefaultTypeConverter());

        final QueryNode node = new SelectNode(null, new ExpressionSpec[0], null);
        final DtoSelector<String> selector = new DtoSelector<>(String.class, ormTable, mock(TableRegistry.class), mock(ClassFieldAccessorCache.class), mock(DtoConstructor.class), databaseProvider, new NoOpAliasGenerator(), context, node);

        when(databaseProvider.toSql(any(), any())).thenReturn("SELECT * FROM TEST WHERE ID = ?");
        when(databaseProvider.select(any(), any())).thenReturn(List.of());

        // When
        selector.list(); // First call
        selector.list(); // Second call (same structure)

        // Then
        verify(databaseProvider, times(1)).toSql(any(), any());
        verify(databaseProvider, times(2)).select(any(), any());
    }
}

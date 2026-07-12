package org.litebridge.orm.api.dto;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.expression.ColumnExpressionFactory;
import org.litebridge.db.spi.expression.LiteralExpressionFactory;
import org.litebridge.db.spi.expression.SelectReferenceExpressionFactory;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.expression.select.SelectFieldSpec;
import org.litebridge.orm.persistence.DtoConstructor;
import org.litebridge.orm.persistence.OrmTable;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
        return new LitebridgeContext(config, fromClauseEngine, sqlFunctionRegistry);
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

        final DtoSelector<Object> selector = new DtoSelector<>(Object.class, ormTable, mock(TableRegistry.class), mock(ClassFieldAccessorCache.class), mock(DtoConstructor.class), mock(TransactionalDatabaseProvider.class), new NoOpAliasGenerator(), createMockContext());

        // When
        final SelectColumnSpec expr = new SelectColumnSpec(new org.litebridge.db.spi.Column(table, "COL"));
        final DtoFromClauseTerminal<Object> terminal = selector.select(expr);

        // Then
        assertNotNull(terminal);
        assertEquals(1, selector.selectSpec().getExpressions().size());
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

        final DtoSelector<Object> selector = new DtoSelector<>(Object.class, ormTable, mock(TableRegistry.class), mock(ClassFieldAccessorCache.class), mock(DtoConstructor.class), mock(TransactionalDatabaseProvider.class), new NoOpAliasGenerator(), createMockContext());

        // When
        final DtoFromClauseTerminal<Object> terminal = selector.select();

        // Then
        assertNotNull(terminal);
        assertFalse(selector.selectSpec().getExpressions().isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testListAndUnwrap() throws Exception {
        // Given
        final OrmTable ormTable = mock(OrmTable.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        when(databaseProvider.getTypeConverter()).thenReturn(new org.litebridge.convert.DefaultTypeConverter());

        final Table table = new Table("", null, "TEST", "t1");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(table);
        when(ormTable.dtoClass()).thenReturn((Class) String.class);
        when(ormTable.getDtoClassInterfaces()).thenReturn(Set.of());

        final DtoSelector<Integer> selector = new DtoSelector<>(Integer.class, ormTable, mock(TableRegistry.class), mock(ClassFieldAccessorCache.class), mock(DtoConstructor.class), databaseProvider, new NoOpAliasGenerator(), createMockContext());
        selector.select();

        final Row row = new Row().withColumn(new org.litebridge.db.spi.Column(table, "COL"), "123");
        when(databaseProvider.select(any(), any())).thenReturn(List.of(row));

        // When
        final List<Integer> results = selector.list();

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(123, results.get(0));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFetchOneDtoErrors() throws Exception {
        // Given
        final OrmTable ormTable = mock(OrmTable.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        when(databaseProvider.getTypeConverter()).thenReturn(new org.litebridge.convert.DefaultTypeConverter());

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

        final DtoSelector<String> selector = new DtoSelector<>(String.class, ormTable, mock(TableRegistry.class), mock(ClassFieldAccessorCache.class), constructor, databaseProvider, new NoOpAliasGenerator(), createMockContext());
        selector.select();

        // Return 2 rows with different PK values to trigger error in oneOrNull
        final Row row1 = new Row().withColumn(pkCol.toColumn(), 1L).withColumn(col1.toColumn(), "1");
        final Row row2 = new Row().withColumn(pkCol.toColumn(), 2L).withColumn(col1.toColumn(), "2");
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

        final DtoSelector<String> selector = new DtoSelector<>(String.class, ormTable, mock(TableRegistry.class), mock(ClassFieldAccessorCache.class), constructor, databaseProvider, new NoOpAliasGenerator(), createMockContext());
        selector.select();
        selector.selectSpec().addExpressions(List.of(new SelectFieldSpec(field1, col1.toColumn())));

        final Row row1 = new Row().withColumn(col1.toColumn(), "1");
        when(databaseProvider.select(any(), any())).thenReturn(List.of(row1));

        // When
        final String result = selector.firstOrNull();

        // Then
        assertEquals("result", result);
        assertEquals(1, selector.selectSpec().getLimit().getLimit().orElse(0));
    }
}

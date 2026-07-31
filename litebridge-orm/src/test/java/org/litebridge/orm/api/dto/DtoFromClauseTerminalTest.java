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
import org.litebridge.orm.engine.QueryPlanCache;
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
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DtoFromClauseTerminalTest {

    private LitebridgeContext createMockContext() {
        final LitebridgeConfig config = new LitebridgeConfig();
        final FromClauseEngine fromClauseEngine = mock(FromClauseEngine.class);
//        when(fromClauseEngine.resolve(any(), any())).thenReturn(mock(From.class));
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select select = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(select);
        when(select.column()).thenReturn(mock(ColumnExpressionFactory.class));
        when(select.reference()).thenReturn(mock(SelectReferenceExpressionFactory.class));
        when(select.literal()).thenReturn(mock(LiteralExpressionFactory.class));
        return new LitebridgeContext(config, fromClauseEngine, sqlFunctionRegistry, mock(QueryPlanCache.class), new NoOpAliasGenerator());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testWithId() throws Exception {
        // Given
        final OrmTable ormTable = mock(OrmTable.class);
        final Table table = new Table("", null, "TEST", "t1");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(table);
        when(ormTable.dtoClass()).thenReturn((Class) TestDto.class);
        when(ormTable.getDtoClassInterfaces()).thenReturn(Set.of());

        final ColumnMetaData pkCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        when(metaData.primaryKey()).thenReturn(List.of(pkCol));

        final FieldAccessor pkField = mock(FieldAccessor.class);
        when(pkField.name()).thenReturn("id");
        when(pkField.type()).thenReturn((Class) Long.class);
        when(pkField.dtoClass()).thenReturn((Class) TestDto.class);
        when(ormTable.getFieldForColumnName("ID")).thenReturn(pkField);
        when(ormTable.getColumnForFieldName("id")).thenReturn(pkCol);
        when(ormTable.getColumnMetaData("ID")).thenReturn(pkCol);
        when(ormTable.mappedFieldTargets()).thenReturn(List.of(Map.entry(pkField, (MappedFieldTarget) pkCol)));

        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        when(databaseProvider.getTypeConverter()).thenReturn(new org.litebridge.convert.DefaultTypeConverter());
        when(databaseProvider.getAliasTransformer()).thenReturn(new org.litebridge.db.spi.alias.DefaultAliasTransformer());
        when(databaseProvider.prepareSql(any(), any())).thenReturn(new org.litebridge.db.spi.sql.PreparedSql("SELECT 1", List.of()));
        when(databaseProvider.select(any(), any(), any())).thenReturn(List.of(new Row().withColumn(pkCol.toColumn(), 1L)));

        final DtoConstructor constructor = mock(DtoConstructor.class);
        when(constructor.newInstance(any(), any())).thenReturn(new DtoConstructor.ConstructionResult<>(new TestDto(), true));

        final DtoSelector<TestDto> selector = new DtoSelector<>(TestDto.class, ormTable, mock(TableRegistry.class), mock(ClassFieldAccessorCache.class), constructor, databaseProvider, new NoOpAliasGenerator(), createMockContext(), null);
        final DtoFromClauseTerminal<TestDto> terminal = selector.select();

        // When
        final Optional<TestDto> result = terminal.withId(1L);

        // Then
        assertTrue(result.isPresent());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testWithIdComposite() throws Exception {
        // Given
        final OrmTable ormTable = mock(OrmTable.class);
        final Table table = new Table("", null, "TEST", "t1");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(table);
        when(ormTable.dtoClass()).thenReturn((Class) TestDto.class);
        when(ormTable.getDtoClassInterfaces()).thenReturn(Set.of());

        final ColumnMetaData pk1 = new ColumnMetaData(table, "ID1", false, Types.BIGINT);
        final ColumnMetaData pk2 = new ColumnMetaData(table, "ID2", false, Types.BIGINT);
        when(metaData.primaryKey()).thenReturn(List.of(pk1, pk2));

        final FieldAccessor f1 = mock(FieldAccessor.class);
        when(f1.name()).thenReturn("id1");
        when(f1.type()).thenReturn((Class) Long.class);
        when(f1.dtoClass()).thenReturn((Class) TestDto.class);
        final FieldAccessor f2 = mock(FieldAccessor.class);
        when(f2.name()).thenReturn("id2");
        when(f2.type()).thenReturn((Class) Long.class);
        when(f2.dtoClass()).thenReturn((Class) TestDto.class);

        when(ormTable.getFieldForColumnName("ID1")).thenReturn(f1);
        when(ormTable.getFieldForColumnName("ID2")).thenReturn(f2);
        when(ormTable.getColumnForFieldName("id1")).thenReturn(pk1);
        when(ormTable.getColumnForFieldName("id2")).thenReturn(pk2);
        when(ormTable.getColumnMetaData("ID1")).thenReturn(pk1);
        when(ormTable.getColumnMetaData("ID2")).thenReturn(pk2);
        when(ormTable.mappedFieldTargets()).thenReturn(List.of(Map.entry(f1, (MappedFieldTarget) pk1), Map.entry(f2, (MappedFieldTarget) pk2)));

        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        when(databaseProvider.getTypeConverter()).thenReturn(new org.litebridge.convert.DefaultTypeConverter());
        when(databaseProvider.getAliasTransformer()).thenReturn(new org.litebridge.db.spi.alias.DefaultAliasTransformer());
        when(databaseProvider.prepareSql(any(), any())).thenReturn(new org.litebridge.db.spi.sql.PreparedSql("SELECT 1", List.of()));
        when(databaseProvider.select(any(), any(), any())).thenReturn(List.of(new Row().withColumn(pk1.toColumn(), 1L).withColumn(pk2.toColumn(), 2L)));

        final DtoConstructor constructor = mock(DtoConstructor.class);
        when(constructor.newInstance(any(), any())).thenReturn(new DtoConstructor.ConstructionResult<>(new TestDto(), true));

        final DtoSelector<TestDto> selector = new DtoSelector<>(TestDto.class, ormTable, mock(TableRegistry.class), mock(ClassFieldAccessorCache.class), constructor, databaseProvider, new NoOpAliasGenerator(), createMockContext(), null);
        final DtoFromClauseTerminal<TestDto> terminal = selector.select();

        // When / Then
        assertNotNull(terminal.withId(List.of(1L, 2L)));
        assertNotNull(terminal.withId(new Object[]{1L, 2L}));
        assertNotNull(terminal.withId(Map.of("id1", 1L, "id2", 2L)));

        assertThrows(IllegalArgumentException.class, () -> terminal.withId(List.of(1L)));
        assertThrows(IllegalArgumentException.class, () -> terminal.withId("invalid"));

        assertTrue(terminal.withIdOrNull(List.of(1L, 2L)) != null);
        assertNotNull(terminal.withIdOrThrow(List.of(1L, 2L)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testWhereJoinGroupByOrderBy() throws Exception {
        final OrmTable ormTable = mock(OrmTable.class);
        final Table table = new Table("", null, "TEST", "t1");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(table);
        when(ormTable.dtoClass()).thenReturn((Class) TestDto.class);

        final ColumnMetaData col1 = new ColumnMetaData(table, "COL1", true, Types.VARCHAR);
        when(ormTable.getColumnForFieldName("field1")).thenReturn(col1);
        when(ormTable.getContextTableRegistry()).thenReturn(mock(TableRegistry.class));

        final DtoSelector<TestDto> selector = new DtoSelector<>(TestDto.class, ormTable, mock(TableRegistry.class), mock(ClassFieldAccessorCache.class), mock(DtoConstructor.class), mock(TransactionalDatabaseProvider.class), new NoOpAliasGenerator(), createMockContext(), null);
        final DtoFromClauseTerminal<TestDto> terminal = selector.select();

        assertNotNull(terminal.where("field1"));
        assertNotNull(terminal.where(new org.litebridge.orm.expression.select.SelectColumnSpec(mock(org.litebridge.db.spi.Column.class))));
        assertNotNull(terminal.where(q -> q.where("field1").eq("val")));

        final OrmTable joinTable = mock(OrmTable.class);
        when(joinTable.getMetaData()).thenReturn(metaData);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        when(tableRegistry.getTableOrThrow(String.class)).thenReturn(joinTable);
        // Ensure SelectExpressionMapper is set by calling select()
        final DtoSelector<TestDto> selectorWithRegistry = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, mock(ClassFieldAccessorCache.class), mock(DtoConstructor.class), mock(TransactionalDatabaseProvider.class), new NoOpAliasGenerator(), createMockContext(), null);
        final DtoFromClauseTerminal<TestDto> terminalWithRegistry = selectorWithRegistry.select();

        assertNotNull(terminalWithRegistry.join(String.class));

        assertNotNull(terminal.groupBy("field1"));
        assertNotNull(terminal.orderBy("field1"));
    }

    private static class TestDto {
        private Long id;
        private Long id1;
        private Long id2;
    }
}

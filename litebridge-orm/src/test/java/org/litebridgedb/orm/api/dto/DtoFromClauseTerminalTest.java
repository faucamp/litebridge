package org.litebridgedb.orm.api.dto;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.expression.LiteralExpressionFactory;
import org.litebridgedb.db.spi.expression.SelectReferenceExpressionFactory;
import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.orm.engine.FromClauseEngine;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.persistence.DtoConstructor;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;
import org.litebridgedb.orm.persistence.alias.NoOpAliasGenerator;
import org.litebridgedb.tracking.ClassFieldAccessorCache;
import org.litebridgedb.tracking.FieldAccessor;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DtoFromClauseTerminalTest {

    @Test
    @SuppressWarnings("unchecked")
    void testWithId() {
        final OrmTable ormTable = mock(OrmTable.class);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        when(context.fromClauseEngine()).thenReturn(mock(FromClauseEngine.class));
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectFunctions = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectFunctions);
        when(selectFunctions.reference()).thenReturn(mock(SelectReferenceExpressionFactory.class));
        when(selectFunctions.literal()).thenReturn(mock(LiteralExpressionFactory.class));
        when(context.sqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);

        when(ormTable.dtoClass()).thenReturn((Class) Object.class);
        final Table table = new Table("", null, "TEST", "t1");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(table);
        when(metaData.name()).thenReturn("TEST");
        when(metaData.primaryKey()).thenReturn(List.of()); // Will cause error later but tested separately
        
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);

        final ColumnMetaData pkCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        when(metaData.primaryKey()).thenReturn(List.of(pkCol));
        final FieldAccessor pkField = mock(FieldAccessor.class);
        when(pkField.name()).thenReturn("id");
        when(ormTable.getFieldForColumnName("ID")).thenReturn(pkField);
        when(ormTable.getColumnForFieldName("id")).thenReturn(pkCol);

        final DtoSelector<Object> selector = new DtoSelector<>(Object.class, ormTable, mock(TableRegistry.class), mock(ClassFieldAccessorCache.class), mock(DtoConstructor.class), databaseProvider, new NoOpAliasGenerator(), context);
        final DtoFromClauseTerminal<Object> terminal = selector.select();

        assertNotNull(terminal.withId(1L));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testWithIdComposite() {
        final OrmTable ormTable = mock(OrmTable.class);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        when(context.fromClauseEngine()).thenReturn(mock(FromClauseEngine.class));
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectFunctions = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectFunctions);
        when(selectFunctions.reference()).thenReturn(mock(SelectReferenceExpressionFactory.class));
        when(selectFunctions.literal()).thenReturn(mock(LiteralExpressionFactory.class));
        when(context.sqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);

        when(ormTable.dtoClass()).thenReturn((Class) Object.class);
        final Table table = new Table("", null, "TEST", "t1");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(table);

        final ColumnMetaData pk1 = new ColumnMetaData(table, "ID1", false, Types.BIGINT);
        final ColumnMetaData pk2 = new ColumnMetaData(table, "ID2", false, Types.BIGINT);
        when(metaData.primaryKey()).thenReturn(List.of(pk1, pk2));
        
        final FieldAccessor f1 = mock(FieldAccessor.class);
        when(f1.name()).thenReturn("id1");
        final FieldAccessor f2 = mock(FieldAccessor.class);
        when(f2.name()).thenReturn("id2");
        
        when(ormTable.getFieldForColumnName("ID1")).thenReturn(f1);
        when(ormTable.getFieldForColumnName("ID2")).thenReturn(f2);
        when(ormTable.getColumnForFieldName("id1")).thenReturn(pk1);
        when(ormTable.getColumnForFieldName("id2")).thenReturn(pk2);

        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final DtoSelector<Object> selector = new DtoSelector<>(Object.class, ormTable, mock(TableRegistry.class), mock(ClassFieldAccessorCache.class), mock(DtoConstructor.class), databaseProvider, new NoOpAliasGenerator(), context);
        final DtoFromClauseTerminal<Object> terminal = selector.select();

        // List
        assertNotNull(terminal.withId(List.of(1L, 2L)));
        // Array
        assertNotNull(terminal.withId(new Object[]{1L, 2L}));
        // Map
        assertNotNull(terminal.withId(Map.of("id1", 1L, "id2", 2L)));
        
        // Invalid count
        assertThrows(IllegalArgumentException.class, () -> terminal.withId(List.of(1L)));
        // Invalid type
        assertThrows(IllegalArgumentException.class, () -> terminal.withId("invalid"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testJoin() {
        final OrmTable ormTable = mock(OrmTable.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        when(context.fromClauseEngine()).thenReturn(mock(FromClauseEngine.class));

        final Table table = new Table("", null, "TEST", "t1");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(table);
        when(ormTable.dtoClass()).thenReturn((Class) Object.class);

        final TableRegistry contextTableRegistry = mock(TableRegistry.class);
        when(ormTable.getContextTableRegistry()).thenReturn(contextTableRegistry);

        final DtoSelector<Object> selector = new DtoSelector<>(Object.class, ormTable, tableRegistry, mock(ClassFieldAccessorCache.class), mock(DtoConstructor.class), mock(TransactionalDatabaseProvider.class), new NoOpAliasGenerator(), context);
        final DtoFromClauseTerminal<Object> terminal = selector.select();

        final OrmTable joinTable = mock(OrmTable.class);
        when(joinTable.getMetaData()).thenReturn(metaData);
        when(tableRegistry.getTableOrThrow(String.class)).thenReturn(joinTable);

        assertNotNull(terminal.join(String.class));
        
        final OrmTable inlineJoinTable = mock(OrmTable.class);
        when(inlineJoinTable.getMetaData()).thenReturn(metaData);
        when(contextTableRegistry.getTable(Integer.class)).thenReturn(inlineJoinTable);
        assertNotNull(terminal.join(Integer.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGroupByOrderBy() {
        final OrmTable ormTable = mock(OrmTable.class);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        when(context.fromClauseEngine()).thenReturn(mock(FromClauseEngine.class));

        final Table table = new Table("", null, "TEST", "t1");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(table);
        when(ormTable.dtoClass()).thenReturn((Class) Object.class);
        final ColumnMetaData col1 = new ColumnMetaData(table, "COL1", true, Types.VARCHAR);
        when(ormTable.getColumnForFieldName("field1")).thenReturn(col1);

        final DtoSelector<Object> selector = new DtoSelector<>(Object.class, ormTable, mock(TableRegistry.class), mock(ClassFieldAccessorCache.class), mock(DtoConstructor.class), mock(TransactionalDatabaseProvider.class), new NoOpAliasGenerator(), context);
        final DtoFromClauseTerminal<Object> terminal = selector.select();

        assertNotNull(terminal.groupBy("field1"));
        assertNotNull(terminal.orderBy("field1"));
    }
}

package org.litebridgedb.orm.api.dto;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.orm.engine.FromClauseEngine;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.persistence.DtoConstructor;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;
import org.litebridgedb.orm.persistence.alias.AliasGenerator;
import org.litebridgedb.tracking.ClassFieldAccessorCache;

import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.orm.persistence.alias.NoOpAliasGenerator;
import org.litebridgedb.db.spi.MappedFieldTarget;
import org.litebridgedb.tracking.FieldAccessor;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DtoSelectorTest {

    @Test
    @SuppressWarnings("unchecked")
    void testSelect() {
        final OrmTable ormTable = mock(OrmTable.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ClassFieldAccessorCache cache = mock(ClassFieldAccessorCache.class);
        final DtoConstructor constructor = mock(DtoConstructor.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final AliasGenerator aliasGenerator = new NoOpAliasGenerator();
        final LitebridgeContext context = mock(LitebridgeContext.class);

        when(ormTable.dtoClass()).thenReturn((Class) Object.class);
        final Table table = new Table("", null, "TEST", "t1");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(table);

        final DtoSelector<Object> selector = new DtoSelector<>(Object.class, ormTable, tableRegistry, cache, constructor, databaseProvider, aliasGenerator, context);
        
        final SelectColumnSpec expr = new SelectColumnSpec(new org.litebridgedb.db.spi.Column(table, "COL"));
        final DtoFromClauseTerminal<Object> terminal = selector.select(expr);
        
        assertNotNull(terminal);
        assertEquals(1, selector.selectSpec().getExpressions().size());
        assertEquals(expr, selector.selectSpec().getExpressions().get(0));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSelectAll() {
        final OrmTable ormTable = mock(OrmTable.class);
        final AliasGenerator aliasGenerator = new NoOpAliasGenerator();
        final LitebridgeContext context = mock(LitebridgeContext.class);

        when(ormTable.dtoClass()).thenReturn((Class) Object.class);
        final Table table = new Table("", null, "TEST", "t1");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(table);

        final ColumnMetaData col1 = new ColumnMetaData(table, "COL1", true, Types.VARCHAR);
        final FieldAccessor field1 = mock(FieldAccessor.class);
        when(field1.name()).thenReturn("field1");
        
        when(ormTable.mappedFieldTargets()).thenReturn(List.of(Map.entry(field1, (MappedFieldTarget) col1)));
        when(ormTable.getFieldForColumnName("COL1")).thenReturn(field1);

        final DtoSelector<Object> selector = new DtoSelector<>(Object.class, ormTable, mock(TableRegistry.class), mock(ClassFieldAccessorCache.class), mock(DtoConstructor.class), mock(TransactionalDatabaseProvider.class), aliasGenerator, context);

        final DtoFromClauseTerminal<Object> terminal = selector.select();
        assertNotNull(terminal);
        assertEquals(1, selector.selectSpec().getExpressions().size());
    }
}

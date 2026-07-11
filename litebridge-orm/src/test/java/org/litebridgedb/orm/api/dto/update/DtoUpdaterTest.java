package org.litebridgedb.orm.api.dto.update;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.SelectExpressionMapper;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DtoUpdaterTest {

    @Test
    @SuppressWarnings("unchecked")
    void testWhere() {
        final OrmTable ormTable = mock(OrmTable.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final SelectExpressionMapper mapper = mock(SelectExpressionMapper.class);
        final LitebridgeContext context = mock(LitebridgeContext.class);

        final Table table = new Table("TEST");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(table);
        final ColumnMetaData col = new ColumnMetaData(table, "COL", true, Types.VARCHAR);
        when(ormTable.getColumnForFieldName("field")).thenReturn(col);

        final DtoUpdater<Object> updater = new DtoUpdater<>(Object.class, ormTable, databaseProvider, mapper, context);
        
        final DtoUpdateWhereConditionClause<Object> clause = updater.where("field");
        assertNotNull(clause);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSet() {
        final OrmTable ormTable = mock(OrmTable.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final SelectExpressionMapper mapper = mock(SelectExpressionMapper.class);
        final LitebridgeContext context = mock(LitebridgeContext.class);

        final Table table = new Table("TEST");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(table);
        final ColumnMetaData col = new ColumnMetaData(table, "COL", true, Types.VARCHAR);
        when(ormTable.getColumnForFieldName("field")).thenReturn(col);

        final DtoUpdater<Object> updater = new DtoUpdater<>(Object.class, ormTable, databaseProvider, mapper, context);
        
        assertNotNull(updater.set("field"));
        assertNotNull(updater.set(new SelectColumnSpec(new org.litebridgedb.db.spi.Column(table, "COL"))));
    }
}

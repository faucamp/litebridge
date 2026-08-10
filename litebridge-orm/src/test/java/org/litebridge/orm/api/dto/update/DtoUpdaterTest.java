package org.litebridge.orm.api.dto.update;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.QueryCompiler;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;

import java.sql.Types;

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
    void testWhereTerminal() {
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
        when(ormTable.getColumnForFieldName("field2")).thenReturn(col);

        when(context.queryPlanCache()).thenReturn(mock(QueryPlanCache.class));
        when(context.createQueryCompiler()).thenReturn(mock(QueryCompiler.class));
        final TableMetaDataCache tableMetaDataCache = mock(TableMetaDataCache.class);
        when(context.tableMetaDataCache()).thenReturn(tableMetaDataCache);
        when(tableMetaDataCache.ensureTableMetaData(any())).thenReturn(metaData);

        final DtoUpdater<Object> updater = new DtoUpdater<>(Object.class, ormTable, databaseProvider, mapper, context);

        final DtoUpdateWhereConditionClauseTerminalImpl<Object> terminal = (DtoUpdateWhereConditionClauseTerminalImpl<Object>) updater.where("field").eq("value");
        assertNotNull(terminal.and("field2"));
        assertNotNull(terminal.and(new SelectColumnSpec(new Column(table, "COL"))));
        assertNotNull(terminal.and(q -> q.where("field").eq("value")));
        assertNotNull(terminal.or("field2"));
        assertNotNull(terminal.or(new SelectColumnSpec(new Column(table, "COL"))));
        assertNotNull(terminal.or(q -> q.where("field").eq("value")));
        assertNotNull(terminal.updateSpec());

        // execute() - might need more mocking but let's try
        try {
            when(databaseProvider.update(any(), any())).thenReturn(mock(UpdateResult.class));
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        }
        assertNotNull(terminal.execute());
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
        assertNotNull(updater.set(new SelectColumnSpec(new Column(table, "COL"))));
    }
}

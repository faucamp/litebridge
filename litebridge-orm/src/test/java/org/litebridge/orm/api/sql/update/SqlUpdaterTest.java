package org.litebridge.orm.api.sql.update;

import org.junit.jupiter.api.Test;
import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.orm.persistence.alias.NoOpAliasGenerator;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SqlUpdaterTest {

    @Test
    void execute() throws SQLException {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final UpdateResult expectedResult = mock(UpdateResult.class);
        when(databaseProvider.update(any(), any())).thenReturn(expectedResult);
        final Table table = new Table("cat", "sch", "tab");
        final LitebridgeContext litebridgeContext = createLitebridgeContext();
        when(litebridgeContext.tableMetaDataCache().ensureTableMetaData(any(Table.class))).thenReturn(mock(TableMetaData.class));
        final SqlUpdater updater = new SqlUpdater(table, databaseProvider, mock(SelectExpressionMapper.class), litebridgeContext);

        // When
        UpdateResult result = updater.execute();

        // Then
        assertEquals(expectedResult, result);
        verify(databaseProvider).update(any(PreparedSql.class), any());
    }

    @Test
    void execute_exception() throws SQLException {
        // Given
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        when(databaseProvider.update(any(), any())).thenThrow(new SQLException("DB error"));
        Table table = new Table("cat", "sch", "tab");
        final LitebridgeContext litebridgeContext = createLitebridgeContext();
        when(litebridgeContext.tableMetaDataCache().ensureTableMetaData(any(Table.class))).thenReturn(mock(TableMetaData.class));
        final SqlUpdater updater = new SqlUpdater(table, databaseProvider, mock(SelectExpressionMapper.class), litebridgeContext);

        // When / Then
        assertThrows(IllegalStateException.class, updater::execute);
    }

    @Test
    void where() {
        // Given
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        Table table = new Table("cat", "sch", "tab");
        SqlUpdater updater = new SqlUpdater(table, databaseProvider, mock(SelectExpressionMapper.class), createLitebridgeContext());

        // When
        SqlUpdateWhereConditionClause result = updater.where("col1");

        // Then
        assertNotNull(result);
    }

    @Test
    void set() {
        // Given
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        Table table = new Table("cat", "sch", "tab");
        SqlUpdater updater = new SqlUpdater(table, databaseProvider, mock(SelectExpressionMapper.class), createLitebridgeContext());

        // When
        SqlUpdateSetStep result = updater.set("col1");

        // Then
        assertNotNull(result);
    }

    private LitebridgeContext createLitebridgeContext() {
        return new LitebridgeContext(new LitebridgeConfig(), mock(FromClauseEngine.class), mock(SqlFunctionRegistry.class), new QueryPlanCache(), new NoOpAliasGenerator(), mock(TableMetaDataCache.class), new DefaultTypeConverter(), mock(SelectExpressionMapper.class));
    }
}

package org.litebridge.orm.api.sql.update;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.update.Update;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.orm.persistence.alias.NoOpAliasGenerator;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SqlUpdaterTest {

    private LitebridgeContext createRealContext() {
        return new LitebridgeContext(new LitebridgeConfig(), mock(FromClauseEngine.class), mock(SqlFunctionRegistry.class), new QueryPlanCache(), new NoOpAliasGenerator());
    }

    @Test
    void execute() throws SQLException {
        // Given
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        UpdateResult expectedResult = mock(UpdateResult.class);
        when(databaseProvider.update(any(), any())).thenReturn(expectedResult);
        Table table = new Table("cat", "sch", "tab");
        SqlUpdater updater = new SqlUpdater(table, databaseProvider, mock(SelectExpressionMapper.class), createRealContext());

        // When
        UpdateResult result = updater.execute();

        // Then
        assertEquals(expectedResult, result);
        verify(databaseProvider).update(argThat((Update u) -> u.table().equals(table)), any());
    }

    @Test
    void execute_exception() throws SQLException {
        // Given
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        when(databaseProvider.update(any(), any())).thenThrow(new SQLException("DB error"));
        Table table = new Table("cat", "sch", "tab");
        SqlUpdater updater = new SqlUpdater(table, databaseProvider, mock(SelectExpressionMapper.class), createRealContext());

        // When / Then
        assertThrows(IllegalStateException.class, updater::execute);
    }

    @Test
    void where() {
        // Given
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        Table table = new Table("cat", "sch", "tab");
        SqlUpdater updater = new SqlUpdater(table, databaseProvider, mock(SelectExpressionMapper.class), createRealContext());

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
        SqlUpdater updater = new SqlUpdater(table, databaseProvider, mock(SelectExpressionMapper.class), createRealContext());

        // When
        SqlUpdateSetStep result = updater.set("col1");

        // Then
        assertNotNull(result);
    }
}

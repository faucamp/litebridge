package org.litebridge.orm.api.sql.delete;

import org.junit.jupiter.api.Test;
import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.Table;
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

class SqlDeletorTest {

    @Test
    void execute() throws SQLException {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final UpdateResult expectedResult = mock(UpdateResult.class);
        when(databaseProvider.delete(any(), any())).thenReturn(expectedResult);
        final Table table = new Table("cat", "sch", "tab");
        final SqlDeletor deletor = new SqlDeletor(table, createLitebridgeContext());

        // When
        final UpdateResult result = deletor.execute();

        // Then
        assertEquals(expectedResult, result);
        verify(databaseProvider).delete(any(PreparedSql.class), any());
    }

    @Test
    void execute_exception() throws SQLException {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        when(databaseProvider.delete(any(), any())).thenThrow(new SQLException("DB error"));
        final Table table = new Table("cat", "sch", "tab");
        final SqlDeletor deletor = new SqlDeletor(table, createLitebridgeContext());

        // When / Then
        assertThrows(IllegalStateException.class, deletor::execute);
    }

    @Test
    void where() {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final Table table = new Table("cat", "sch", "tab");
        final SqlDeletor deletor = new SqlDeletor(table, createLitebridgeContext());

        // When
        final SqlDeleteWhereConditionClause result = deletor.where("col1");

        // Then
        assertNotNull(result);
    }

    private LitebridgeContext createLitebridgeContext() {
        return new LitebridgeContext(new LitebridgeConfig(), mock(FromClauseEngine.class), mock(SqlFunctionRegistry.class), new QueryPlanCache(), new NoOpAliasGenerator(), mock(TableMetaDataCache.class), new DefaultTypeConverter(), mock(SelectExpressionMapper.class));
    }
}

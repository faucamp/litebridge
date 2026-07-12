package org.litebridge.orm.api.sql.delete;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SqlDeletorTest {

    @Test
    void execute() throws SQLException {
        // Given
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        UpdateResult expectedResult = mock(UpdateResult.class);
        when(databaseProvider.delete(any(), any())).thenReturn(expectedResult);
        Table table = new Table("cat", "sch", "tab");
        SqlDeletor deletor = new SqlDeletor(table, databaseProvider, mock(SelectExpressionMapper.class), mock(LitebridgeContext.class));

        // When
        UpdateResult result = deletor.execute();

        // Then
        assertEquals(expectedResult, result);
        verify(databaseProvider).delete(argThat(d -> d.table().equals(table)), any());
    }

    @Test
    void execute_exception() throws SQLException {
        // Given
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        when(databaseProvider.delete(any(), any())).thenThrow(new SQLException("DB error"));
        Table table = new Table("cat", "sch", "tab");
        SqlDeletor deletor = new SqlDeletor(table, databaseProvider, mock(SelectExpressionMapper.class), mock(LitebridgeContext.class));

        // When / Then
        assertThrows(IllegalStateException.class, deletor::execute);
    }

    @Test
    void where() {
        // Given
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        Table table = new Table("cat", "sch", "tab");
        SqlDeletor deletor = new SqlDeletor(table, databaseProvider, mock(SelectExpressionMapper.class), mock(LitebridgeContext.class));

        // When
        SqlDeleteWhereConditionClause result = deletor.where("col1");

        // Then
        assertNotNull(result);
    }
}

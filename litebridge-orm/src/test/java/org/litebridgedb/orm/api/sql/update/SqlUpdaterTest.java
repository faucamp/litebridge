package org.litebridgedb.orm.api.sql.update;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.update.UpdateResult;
import org.litebridgedb.orm.api.select.impl.LitebridgeContext;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;

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

    @Test
    void execute() throws SQLException {
        // Given
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        UpdateResult expectedResult = mock(UpdateResult.class);
        when(databaseProvider.update(any(), any())).thenReturn(expectedResult);
        Table table = new Table("cat", "sch", "tab");
        SqlUpdater updater = new SqlUpdater(table, databaseProvider, mock(LitebridgeContext.class));

        // When
        UpdateResult result = updater.execute();

        // Then
        assertEquals(expectedResult, result);
        verify(databaseProvider).update(argThat(u -> u.table().equals(table)), any());
    }

    @Test
    void execute_exception() throws SQLException {
        // Given
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        when(databaseProvider.update(any(), any())).thenThrow(new SQLException("DB error"));
        Table table = new Table("cat", "sch", "tab");
        SqlUpdater updater = new SqlUpdater(table, databaseProvider, mock(LitebridgeContext.class));

        // When / Then
        assertThrows(IllegalStateException.class, updater::execute);
    }

    @Test
    void where() {
        // Given
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        Table table = new Table("cat", "sch", "tab");
        SqlUpdater updater = new SqlUpdater(table, databaseProvider, mock(LitebridgeContext.class));

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
        SqlUpdater updater = new SqlUpdater(table, databaseProvider, mock(LitebridgeContext.class));

        // When
        SqlUpdateSetStep result = updater.set("col1");

        // Then
        assertNotNull(result);
    }
}

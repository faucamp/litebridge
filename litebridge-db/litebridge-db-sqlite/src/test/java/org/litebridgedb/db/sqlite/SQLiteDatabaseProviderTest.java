package org.litebridgedb.db.sqlite;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.impl.AbstractDatabaseProvider;
import org.litebridgedb.db.spi.tx.ManagedConnection;
import org.mockito.Mockito;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SQLiteDatabaseProviderTest {

    @Test
    void createPreparedStatementUsingConnection_withGeneratedKeys() throws SQLException {
        // Given
        SQLiteDatabaseProvider provider = new SQLiteDatabaseProvider();
        ManagedConnection mockConnection = Mockito.mock(ManagedConnection.class);
        PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        AbstractDatabaseProvider.PreparedSql mockPreparedSql = new AbstractDatabaseProvider.PreparedSql("SELECT * FROM test", null);
        TableMetaData mockTableMetaData = mock(TableMetaData.class);

        when(mockConnection.prepareStatement(mockPreparedSql.sql(), PreparedStatement.RETURN_GENERATED_KEYS))
                .thenReturn(mockPreparedStatement);

        // When
        PreparedStatement result = provider.createPreparedStatementUsingConnection(
                mockPreparedSql, true, mockTableMetaData, mockConnection);

        // Then
        assertNotNull(result);
        verify(mockConnection, times(1))
                .prepareStatement(mockPreparedSql.sql(), PreparedStatement.RETURN_GENERATED_KEYS);
    }

    @Test
    void createPreparedStatementUsingConnection_withoutGeneratedKeys() throws SQLException {
        // Given
        SQLiteDatabaseProvider provider = new SQLiteDatabaseProvider();
        ManagedConnection mockConnection = Mockito.mock(ManagedConnection.class);
        PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        AbstractDatabaseProvider.PreparedSql mockPreparedSql = new AbstractDatabaseProvider.PreparedSql("SELECT * FROM test", null);
        TableMetaData mockTableMetaData = mock(TableMetaData.class);

        when(mockConnection.prepareStatement(mockPreparedSql.sql())).thenReturn(mockPreparedStatement);

        // When
        PreparedStatement result = provider.createPreparedStatementUsingConnection(
                mockPreparedSql, false, mockTableMetaData, mockConnection);

        // Then
        assertNotNull(result);
        verify(mockConnection, times(1))
                .prepareStatement(mockPreparedSql.sql());
    }

    @Test
    void extractGeneratedKeys() throws SQLException {
        // Given
        SQLiteDatabaseProvider provider = new SQLiteDatabaseProvider();
        TableMetaData mockTableMetaData = mock(TableMetaData.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        ColumnMetaData mockColumnMetaData = mock(ColumnMetaData.class);

        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getObject(1)).thenReturn(123L);
        when(mockTableMetaData.primaryKey()).thenReturn(List.of(mockColumnMetaData));
        when(mockColumnMetaData.isAutoIncrement()).thenReturn(true);
        when(mockColumnMetaData.name()).thenReturn("id");

        // When
        Map<ColumnMetaData, Object> result = provider.extractGeneratedKeys(mockTableMetaData, mockPreparedStatement);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(123L, result.get(mockColumnMetaData));
    }

    @Test
    void getLogger() {
        // Given
        final SQLiteDatabaseProvider provider = new SQLiteDatabaseProvider();

        assertNotNull(provider.getLogger());
    }
}

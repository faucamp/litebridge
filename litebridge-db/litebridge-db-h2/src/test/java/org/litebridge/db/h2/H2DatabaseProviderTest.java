package org.litebridge.db.h2;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.impl.AbstractDatabaseProvider;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.mockito.Mockito;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class H2DatabaseProviderTest {

    @Test
    void createPreparedStatementUsingConnection_withGeneratedKeys() throws SQLException {
        // Given
        H2DatabaseProvider provider = new H2DatabaseProvider();
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
        H2DatabaseProvider provider = new H2DatabaseProvider();
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
    void getLogger() {
        // Given
        final H2DatabaseProvider provider = new H2DatabaseProvider();

        assertNotNull(provider.getLogger());
    }
}
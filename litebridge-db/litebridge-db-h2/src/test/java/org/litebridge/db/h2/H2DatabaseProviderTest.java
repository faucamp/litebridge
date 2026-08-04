package org.litebridge.db.h2;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.impl.DefaultSequenceColumnValueGenerator;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.mockito.Mockito;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class H2DatabaseProviderTest {

    @Test
    void createPreparedStatementUsingConnection_withGeneratedKeys() throws SQLException {
        // Given
        final H2DatabaseProvider provider = new H2DatabaseProvider();
        final ManagedConnection mockConnection = Mockito.mock(ManagedConnection.class);
        final PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        final PreparedSql mockPreparedSql = new PreparedSql("SELECT * FROM test", null);

        when(mockConnection.prepareStatement(mockPreparedSql.sql(), PreparedStatement.RETURN_GENERATED_KEYS))
                .thenReturn(mockPreparedStatement);

        // When
        final PreparedStatement result = provider.createPreparedStatementUsingConnection(mockPreparedSql, mockConnection);

        // Then
        assertNotNull(result);
        verify(mockConnection, times(1))
                .prepareStatement(mockPreparedSql.sql(), PreparedStatement.RETURN_GENERATED_KEYS);
    }

    @Test
    void createPreparedStatementUsingConnection_withoutGeneratedKeys() throws SQLException {
        // Given
        final H2DatabaseProvider provider = new H2DatabaseProvider();
        final ManagedConnection mockConnection = Mockito.mock(ManagedConnection.class);
        final PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        final PreparedSql mockPreparedSql = new PreparedSql("SELECT * FROM test", null);

        when(mockConnection.prepareStatement(mockPreparedSql.sql())).thenReturn(mockPreparedStatement);

        // When
        final PreparedStatement result = provider.createPreparedStatementUsingConnection(mockPreparedSql, mockConnection);

        // Then
        assertNotNull(result);
        verify(mockConnection, times(1))
                .prepareStatement(mockPreparedSql.sql());
    }

    @Test
    void getSequenceColumnValueGenerator() {
        // Given
        final H2DatabaseProvider databaseProvider = new H2DatabaseProvider();

        // When
        final SequenceColumnValueGenerator result = databaseProvider.getSequenceColumnValueGenerator("test_sequence");

        // Then
        assertInstanceOf(DefaultSequenceColumnValueGenerator.class, result);
    }

    @Test
    void getLogger() {
        // Given
        final H2DatabaseProvider provider = new H2DatabaseProvider();

        assertNotNull(provider.getLogger());
    }
}
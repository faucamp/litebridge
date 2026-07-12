package org.litebridge.db.postgres;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.mockito.Mockito;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostgresDatabaseProviderTest {

    @Test
    void createPreparedStatementUsingConnection_withGeneratedKeys() throws SQLException {
        // Given
        final PostgresDatabaseProvider provider = new PostgresDatabaseProvider();
        final ManagedConnection mockConnection = Mockito.mock(ManagedConnection.class);
        final PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        final PreparedSql mockPreparedSql = new PreparedSql("SELECT * FROM test", null);
        final TableMetaData mockTableMetaData = mock(TableMetaData.class);

        when(mockConnection.prepareStatement(mockPreparedSql.sql(), Statement.RETURN_GENERATED_KEYS))
                .thenReturn(mockPreparedStatement);

        // When
        final PreparedStatement result = provider.createPreparedStatementUsingConnection(
                mockPreparedSql, true, mockTableMetaData, mockConnection);

        // Then
        assertNotNull(result);
        verify(mockConnection, times(1))
                .prepareStatement(mockPreparedSql.sql(), Statement.RETURN_GENERATED_KEYS);
    }

    @Test
    void createPreparedStatementUsingConnection_withoutGeneratedKeys() throws SQLException {
        // Given
        final PostgresDatabaseProvider provider = new PostgresDatabaseProvider();
        final ManagedConnection mockConnection = Mockito.mock(ManagedConnection.class);
        final PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        final PreparedSql mockPreparedSql = new PreparedSql("SELECT * FROM test", null);
        final TableMetaData mockTableMetaData = mock(TableMetaData.class);

        when(mockConnection.prepareStatement(mockPreparedSql.sql())).thenReturn(mockPreparedStatement);

        // When
        final PreparedStatement result = provider.createPreparedStatementUsingConnection(
                mockPreparedSql, false, mockTableMetaData, mockConnection);

        // Then
        assertNotNull(result);
        verify(mockConnection, times(1))
                .prepareStatement(mockPreparedSql.sql());
    }

    @Test
    void getSequenceColumnValueGenerator() {
        // Given
        final PostgresDatabaseProvider databaseProvider = new PostgresDatabaseProvider();

        // When
        final SequenceColumnValueGenerator result = databaseProvider.getSequenceColumnValueGenerator("test_sequence");

        // Then
        assertInstanceOf(PostgresSequenceColumnValueGenerator.class, result);
    }

    @Test
    void getLogger() {
        // Given
        final PostgresDatabaseProvider provider = new PostgresDatabaseProvider();

        // Then
        assertNotNull(provider.getLogger());
    }
}
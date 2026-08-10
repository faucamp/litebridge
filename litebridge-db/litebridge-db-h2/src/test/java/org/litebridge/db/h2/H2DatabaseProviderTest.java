package org.litebridge.db.h2;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.impl.DefaultSequenceColumnValueGenerator;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.mockito.Mockito;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class H2DatabaseProviderTest {

    @Test
    void createPreparedStatementUsingConnection_withNullMetaData() throws SQLException {
        // Given
        final H2DatabaseProvider provider = new H2DatabaseProvider();
        final ManagedConnection mockConnection = Mockito.mock(ManagedConnection.class);
        final PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        final PreparedSql mockPreparedSql = new PreparedSql("SELECT * FROM test", Collections.emptyList(), null, null);

        when(mockConnection.prepareStatement(mockPreparedSql.sql())).thenReturn(mockPreparedStatement);

        // When
        final PreparedStatement result = provider.createPreparedStatementUsingConnection(mockPreparedSql, mockConnection);

        // Then
        assertNotNull(result);
        verify(mockConnection, times(1)).prepareStatement(mockPreparedSql.sql());
        verify(mockConnection, never()).prepareStatement(anyString(), anyInt());
    }

    @Test
    void createPreparedStatementUsingConnection_withGeneratedKeys() throws SQLException {
        // Given
        final H2DatabaseProvider provider = new H2DatabaseProvider();
        final ManagedConnection mockConnection = Mockito.mock(ManagedConnection.class);
        final PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        final UpdateMetaData updateMetaData = new UpdateMetaData(true, Collections.emptyList(), new String[0]);
        final PreparedSql mockPreparedSql = new PreparedSql("INSERT INTO test (name) VALUES (?)", Collections.emptyList(), null, updateMetaData);

        when(mockConnection.prepareStatement(mockPreparedSql.sql(), Statement.RETURN_GENERATED_KEYS)).thenReturn(mockPreparedStatement);

        // When
        final PreparedStatement result = provider.createPreparedStatementUsingConnection(mockPreparedSql, mockConnection);

        // Then
        assertNotNull(result);
        verify(mockConnection, times(1)).prepareStatement(mockPreparedSql.sql(), Statement.RETURN_GENERATED_KEYS);
    }

    @Test
    void createPreparedStatementUsingConnection_withoutGeneratedKeys() throws SQLException {
        // Given
        final H2DatabaseProvider provider = new H2DatabaseProvider();
        final ManagedConnection mockConnection = Mockito.mock(ManagedConnection.class);
        final PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        final UpdateMetaData updateMetaData = new UpdateMetaData(false, Collections.emptyList(), new String[0]);
        final PreparedSql mockPreparedSql = new PreparedSql("UPDATE test SET name = ? WHERE id = ?", Collections.emptyList(), null, updateMetaData);

        when(mockConnection.prepareStatement(mockPreparedSql.sql())).thenReturn(mockPreparedStatement);

        // When
        final PreparedStatement result = provider.createPreparedStatementUsingConnection(mockPreparedSql, mockConnection);

        // Then
        assertNotNull(result);
        verify(mockConnection, times(1)).prepareStatement(mockPreparedSql.sql());
        verify(mockConnection, never()).prepareStatement(anyString(), anyInt());
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
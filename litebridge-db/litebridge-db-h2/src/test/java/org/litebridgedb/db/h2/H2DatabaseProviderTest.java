package org.litebridgedb.db.h2;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridgedb.db.spi.impl.DefaultSequenceColumnValueGenerator;
import org.litebridgedb.db.spi.sql.PreparedSql;
import org.litebridgedb.db.spi.tx.ManagedConnection;
import org.mockito.Mockito;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
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
        final TableMetaData mockTableMetaData = mock(TableMetaData.class);

        when(mockConnection.prepareStatement(mockPreparedSql.sql(), PreparedStatement.RETURN_GENERATED_KEYS))
                .thenReturn(mockPreparedStatement);

        // When
        final PreparedStatement result = provider.createPreparedStatementUsingConnection(
                mockPreparedSql, true, mockTableMetaData, mockConnection);

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
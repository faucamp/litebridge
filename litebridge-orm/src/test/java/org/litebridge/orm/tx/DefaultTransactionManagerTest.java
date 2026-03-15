package org.litebridge.orm.tx;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.litebridge.db.spi.tx.TransactionException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class DefaultTransactionManagerTest {

    @Test
    void begin() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);

        // When
        transactionManager.begin();

        // Then
        assertTrue(transactionManager.isTransactionActive());
        verify(dataSource).getConnection();
        verify(connection).setAutoCommit(false);
    }

    @Test
    void begin_nested() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);

        transactionManager.begin();

        // When
        transactionManager.begin();

        // Then
        assertTrue(transactionManager.isTransactionActive());
        verify(dataSource).getConnection();
        verify(connection).setAutoCommit(false);
        verifyNoMoreInteractions(dataSource, connection);
    }

    @Test
    void begin_getConnection_throwsSQLException() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final SQLException sqlException = new SQLException("Test exception");
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenThrow(sqlException);

        // Then
        final TransactionException transactionException = assertThrows(TransactionException.class, transactionManager::begin);
        assertSame(sqlException, transactionException.getCause());
        assertFalse(transactionManager.isTransactionActive());
    }

    @Test
    void begin_closesConnectionWhenSetAutoCommitFails() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final SQLException sqlException = new SQLException("Test exception");
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        doThrow(sqlException).when(connection).setAutoCommit(false);

        // Then
        final TransactionException transactionException = assertThrows(TransactionException.class, transactionManager::begin);
        assertSame(sqlException, transactionException.getCause());
        verify(connection).close();
        assertFalse(transactionManager.isTransactionActive());
    }

    @Test
    void begin_connectionClose_throwsSQLException() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final SQLException innerException = new SQLException("Inner test exception");
        final SQLException outerException = new SQLException("Outer test exception");
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        doThrow(innerException).when(connection).setAutoCommit(false);
        doThrow(outerException).when(connection).close();

        // Then
        final TransactionException transactionException = assertThrows(TransactionException.class, transactionManager::begin);
        assertSame(innerException, transactionException.getCause());
        assertFalse(transactionManager.isTransactionActive());
    }

    @Test
    void connection() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);

        // When
        transactionManager.begin();
        final ManagedConnection managedConnection = transactionManager.connection();

        // Then
        assertInstanceOf(ManagedConnection.class, managedConnection);
    }

    @Test
    void connection_withoutTransaction() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);

        // When
        final ManagedConnection managedConnection = transactionManager.connection();

        // Then
        assertInstanceOf(ManagedConnection.class, managedConnection);
    }

    @Test
    void commit_withoutActiveTransaction() {
        // Given
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(mock(DataSource.class));

        // Then
        assertThrows(IllegalStateException.class, transactionManager::commit);
    }

    @Test
    void commit_nested() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);

        transactionManager.begin();
        transactionManager.begin();

        // When
        transactionManager.commit();

        // Then
        assertTrue(transactionManager.isTransactionActive());
        verify(connection).setAutoCommit(false);
        verifyNoMoreInteractions(connection);
    }

    @Test
    void commit() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);

        transactionManager.begin();

        // When
        transactionManager.commit();

        // Then
        assertFalse(transactionManager.isTransactionActive());
        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void commit_throwsSQLException() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final SQLException sqlException = new SQLException("Test exception");
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        doThrow(sqlException).when(connection).commit();

        transactionManager.begin();

        // Then
        final TransactionException transactionException = assertThrows(TransactionException.class, transactionManager::commit);
        assertSame(sqlException, transactionException.getCause());
        assertFalse(transactionManager.isTransactionActive());
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void commit_rollbackOnlyTransaction() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);

        transactionManager.begin();
        transactionManager.begin();
        transactionManager.rollback();

        // Then
        final TransactionException transactionException = assertThrows(TransactionException.class, transactionManager::commit);
        assertSame("Transaction marked rollback-only", transactionException.getMessage());
        assertFalse(transactionManager.isTransactionActive());
        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void rollback_withoutActiveTransaction() {
        // Given
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(mock(DataSource.class));

        // Then
        assertThrows(IllegalStateException.class, transactionManager::rollback);
    }

    @Test
    void rollback_nested() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);

        transactionManager.begin();
        transactionManager.begin();

        // When
        transactionManager.rollback();

        // Then
        assertTrue(transactionManager.isTransactionActive());
        verify(connection).setAutoCommit(false);
        verifyNoMoreInteractions(connection);
    }

    @Test
    void rollback() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);

        transactionManager.begin();

        // When
        transactionManager.rollback();

        // Then
        assertFalse(transactionManager.isTransactionActive());
        verify(connection).setAutoCommit(false);
        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void rollback_throwsSQLException() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final SQLException sqlException = new SQLException("Test exception");
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        doThrow(sqlException).when(connection).rollback();

        transactionManager.begin();

        // Then
        final TransactionException transactionException = assertThrows(TransactionException.class, transactionManager::rollback);
        assertSame(sqlException, transactionException.getCause());
        assertFalse(transactionManager.isTransactionActive());
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void rollback_cleanupThrowsSQLException() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final SQLException sqlException = new SQLException("Test exception");
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        doThrow(sqlException).when(connection).close();

        transactionManager.begin();

        // Then
        final TransactionException transactionException = assertThrows(TransactionException.class, transactionManager::rollback);
        assertSame(sqlException, transactionException.getCause());
        assertFalse(transactionManager.isTransactionActive());
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void rollback_throwsSQLException_cleanupThrowsSQLException() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final SQLException sqlException = new SQLException("Test exception");
        final SQLException cleanupException = new SQLException("Cleanup test exception");
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        doThrow(sqlException).when(connection).rollback();
        doThrow(cleanupException).when(connection).setAutoCommit(true);
        doThrow(cleanupException).when(connection).close();

        transactionManager.begin();

        // Then
        final TransactionException transactionException = assertThrows(TransactionException.class, transactionManager::rollback);
        assertSame(sqlException, transactionException.getCause());
        assertFalse(transactionManager.isTransactionActive());
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void isTransactionActive() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);

        // Then
        assertFalse(transactionManager.isTransactionActive());

        // When
        transactionManager.begin();

        // Then
        assertTrue(transactionManager.isTransactionActive());

        // When
        transactionManager.commit();

        // Then
        assertFalse(transactionManager.isTransactionActive());
    }
}
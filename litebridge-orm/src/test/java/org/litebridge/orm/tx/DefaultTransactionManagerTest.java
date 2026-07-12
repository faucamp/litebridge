package org.litebridge.orm.tx;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.tx.Isolation;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.litebridge.db.spi.tx.TransactionException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    void begin_readOnlyAndCustomIsolation() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);

        // When
        transactionManager.begin(true, Isolation.SERIALIZABLE);

        // Then
        assertTrue(transactionManager.isTransactionActive());
        verify(connection).setAutoCommit(false);
        verify(connection).setReadOnly(true);
        verify(connection).setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    }

    @Test
    void begin_afterAutoCommitConnection_throwsTransactionException() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);

        transactionManager.connection();

        // Then
        final TransactionException transactionException = assertThrows(TransactionException.class, transactionManager::begin);
        assertEquals("Managed connection already acquired in auto-commit mode", transactionException.getMessage());
        assertFalse(transactionManager.isTransactionActive());
        assertTrue(transactionManager.requiresCleanup());
    }

    @Test
    void connection_withoutTransaction_reusesAutoCommitManagedConnection() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);

        // When
        final ManagedConnection firstManagedConnection = transactionManager.connection();
        final ManagedConnection secondManagedConnection = transactionManager.connection();

        // Then
        assertSame(firstManagedConnection, secondManagedConnection);
        assertFalse(transactionManager.isTransactionActive());
        assertTrue(transactionManager.requiresCleanup());
        verify(dataSource).getConnection();
    }

    @Test
    void cleanup_withoutState_doesNothing() {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        // When
        transactionManager.cleanup();

        // Then
        assertFalse(transactionManager.requiresCleanup());
        verifyNoMoreInteractions(dataSource);
    }

    @Test
    void cleanup_afterAutoCommitConnection_closesConnectionWithoutResettingAutoCommit() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);

        transactionManager.connection();

        // When
        transactionManager.cleanup();

        // Then
        assertFalse(transactionManager.requiresCleanup());
        assertFalse(transactionManager.isTransactionActive());
        verify(connection, never()).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void cleanup_afterAutoCommitConnection_closeThrowsSQLException() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final SQLException sqlException = new SQLException("Test exception");
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        doThrow(sqlException).when(connection).close();

        transactionManager.connection();

        // Then
        final TransactionException transactionException = assertThrows(TransactionException.class, transactionManager::cleanup);
        assertEquals("Cleanup failed", transactionException.getMessage());
        assertSame(sqlException, transactionException.getCause());
        assertFalse(transactionManager.requiresCleanup());
    }

    @Test
    void requiresCleanup() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);

        // Then
        assertFalse(transactionManager.requiresCleanup());

        // When
        transactionManager.begin();

        // Then
        assertTrue(transactionManager.requiresCleanup());

        // When
        transactionManager.commit();

        // Then
        assertFalse(transactionManager.requiresCleanup());
    }

    @Test
    void isRollbackOnly() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);

        // Then
        assertFalse(transactionManager.isRollbackOnly());

        // When
        transactionManager.begin();
        transactionManager.begin();
        transactionManager.rollback();

        // Then
        assertTrue(transactionManager.isRollbackOnly());

        // When
        assertThrows(TransactionException.class, transactionManager::commit);

        // Then
        assertFalse(transactionManager.isRollbackOnly());
    }

    @Test
    void addCommitCallback_withoutTransaction_executesImmediately() {
        // Given
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(mock(DataSource.class));
        final AtomicInteger callbackCalls = new AtomicInteger();

        // When
        transactionManager.addCommitCallback(callbackCalls::incrementAndGet);

        // Then
        assertEquals(1, callbackCalls.get());
    }

    @Test
    void addCommitCallback_withAutoCommitConnection_executesImmediately() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);
        final AtomicInteger callbackCalls = new AtomicInteger();

        when(dataSource.getConnection()).thenReturn(connection);

        transactionManager.connection();

        // When
        transactionManager.addCommitCallback(callbackCalls::incrementAndGet);

        // Then
        assertEquals(1, callbackCalls.get());
    }

    @Test
    void addCommitCallback_withTransaction_executesOnCommit() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);
        final AtomicInteger callbackCalls = new AtomicInteger();

        when(dataSource.getConnection()).thenReturn(connection);

        transactionManager.begin();

        // When
        transactionManager.addCommitCallback(callbackCalls::incrementAndGet);

        // Then
        assertEquals(0, callbackCalls.get());

        // When
        transactionManager.commit();

        // Then
        assertEquals(1, callbackCalls.get());
    }

    @Test
    void addCommitCallback_callbackThrowsException() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final RuntimeException callbackException = new RuntimeException("Callback exception");
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);

        transactionManager.begin();
        transactionManager.addCommitCallback(() -> {
            throw callbackException;
        });

        // Then
        final TransactionException transactionException = assertThrows(TransactionException.class, transactionManager::commit);
        assertEquals("Rollback callback failed", transactionException.getMessage());
        assertSame(callbackException, transactionException.getCause());
        assertFalse(transactionManager.requiresCleanup());
    }

    @Test
    void addRollbackCallback_withoutTransaction_doesNothing() {
        // Given
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(mock(DataSource.class));
        final AtomicInteger callbackCalls = new AtomicInteger();

        // When
        transactionManager.addRollbackCallback(callbackCalls::incrementAndGet);

        // Then
        assertEquals(0, callbackCalls.get());
    }

    @Test
    void addRollbackCallback_withAutoCommitConnection_doesNothing() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);
        final AtomicInteger callbackCalls = new AtomicInteger();

        when(dataSource.getConnection()).thenReturn(connection);

        transactionManager.connection();

        // When
        transactionManager.addRollbackCallback(callbackCalls::incrementAndGet);

        // Then
        assertEquals(0, callbackCalls.get());
    }

    @Test
    void addRollbackCallback_withTransaction_executesOnRollback() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);
        final AtomicInteger callbackCalls = new AtomicInteger();

        when(dataSource.getConnection()).thenReturn(connection);

        transactionManager.begin();

        // When
        transactionManager.addRollbackCallback(callbackCalls::incrementAndGet);

        // Then
        assertEquals(0, callbackCalls.get());

        // When
        transactionManager.rollback();

        // Then
        assertEquals(1, callbackCalls.get());
    }

    @Test
    void addRollbackCallback_callbackThrowsException() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final RuntimeException callbackException = new RuntimeException("Callback exception");
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);

        transactionManager.begin();
        transactionManager.addRollbackCallback(() -> {
            throw callbackException;
        });

        // Then
        final TransactionException transactionException = assertThrows(TransactionException.class, transactionManager::rollback);
        assertEquals("Rollback callback failed", transactionException.getMessage());
        assertSame(callbackException, transactionException.getCause());
        assertFalse(transactionManager.requiresCleanup());
    }

    @Test
    void addRollbackCallback_nestedRollbackOnlyTransaction_executesOnOuterCommitRollback() throws SQLException {
        // Given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DefaultTransactionManager transactionManager = new DefaultTransactionManager(dataSource);
        final AtomicInteger callbackCalls = new AtomicInteger();

        when(dataSource.getConnection()).thenReturn(connection);

        transactionManager.begin();
        transactionManager.begin();
        transactionManager.addRollbackCallback(callbackCalls::incrementAndGet);

        // When
        transactionManager.rollback();

        // Then
        assertEquals(0, callbackCalls.get());
        assertTrue(transactionManager.isRollbackOnly());

        // When
        assertThrows(TransactionException.class, transactionManager::commit);

        // Then
        assertEquals(1, callbackCalls.get());
    }
}
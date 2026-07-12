package org.litebridge.orm.tx;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.litebridge.db.spi.tx.TransactionManager;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionTest {

    @Test
    void commit() {
        // Given
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final Transaction transaction = new Transaction(transactionManager);

        // When
        transaction.commit();

        // Then
        verify(transactionManager).commit();
    }

    @Test
    void commit_completed_throwsIllegalStateException() {
        // Given
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final Transaction transaction = new Transaction(transactionManager);

        transaction.commit();

        // Then
        assertThrows(IllegalStateException.class, transaction::commit);
        verify(transactionManager).commit();
    }

    @Test
    void rollback() {
        // Given
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final Transaction transaction = new Transaction(transactionManager);

        // When
        transaction.rollback();

        // Then
        verify(transactionManager).rollback();
    }

    @Test
    void rollback_completed_doesNothing() {
        // Given
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final Transaction transaction = new Transaction(transactionManager);

        transaction.rollback();

        // When
        transaction.rollback();

        // Then
        verify(transactionManager).rollback();
    }

    @Test
    void isTransactionActive() {
        // Given
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final Transaction transaction = new Transaction(transactionManager);

        when(transactionManager.isTransactionActive()).thenReturn(true);

        // When
        final boolean result = transaction.isTransactionActive();

        // Then
        assertTrue(result);
    }

    @Test
    void isTransactionActive_false() {
        // Given
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final Transaction transaction = new Transaction(transactionManager);

        when(transactionManager.isTransactionActive()).thenReturn(false);

        // When
        final boolean result = transaction.isTransactionActive();

        // Then
        assertFalse(result);
    }

    @Test
    void isRollbackOnly() {
        // Given
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final Transaction transaction = new Transaction(transactionManager);

        when(transactionManager.isRollbackOnly()).thenReturn(true);

        // When
        final boolean result = transaction.isRollbackOnly();

        // Then
        assertTrue(result);
    }

    @Test
    void isRollbackOnly_false() {
        // Given
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final Transaction transaction = new Transaction(transactionManager);

        when(transactionManager.isRollbackOnly()).thenReturn(false);

        // When
        final boolean result = transaction.isRollbackOnly();

        // Then
        assertFalse(result);
    }

    @Test
    void connection() throws SQLException {
        // Given
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final ManagedConnection managedConnection = mock(ManagedConnection.class);
        final Transaction transaction = new Transaction(transactionManager);

        when(transactionManager.connection()).thenReturn(managedConnection);

        // When
        final ManagedConnection result = transaction.connection();

        // Then
        assertSame(managedConnection, result);
    }

    @Test
    void close_notCompleted_rollsBack() {
        // Given
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final Transaction transaction = new Transaction(transactionManager);

        when(transactionManager.isRollbackOnly()).thenReturn(false);

        // When
        transaction.close();

        // Then
        verify(transactionManager).isRollbackOnly();
        verify(transactionManager).rollback();
    }

    @Test
    void close_completed_doesNothing() {
        // Given
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final Transaction transaction = new Transaction(transactionManager);

        transaction.commit();

        // When
        transaction.close();

        // Then
        verify(transactionManager).commit();
        verify(transactionManager).isRollbackOnly();
        verify(transactionManager, never()).rollback();
    }

    @Test
    void close_completedRollbackOnly_rollsBackDoesNothing() {
        // Given
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final Transaction transaction = new Transaction(transactionManager);

        transaction.commit();
        when(transactionManager.isRollbackOnly()).thenReturn(true);

        // When
        transaction.close();

        // Then
        verify(transactionManager).commit();
        verify(transactionManager).isRollbackOnly();
        verify(transactionManager, never()).rollback();
    }

    @Test
    void close_notCompletedRollbackOnly_rollsBack() {
        // Given
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final Transaction transaction = new Transaction(transactionManager);

        when(transactionManager.isRollbackOnly()).thenReturn(true);

        // When
        transaction.close();

        // Then
        verify(transactionManager).isRollbackOnly();
        verify(transactionManager).rollback();
    }

    @Test
    void close_rollbackThrowsException_marksCompleted() {
        // Given
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final RuntimeException runtimeException = new RuntimeException("Test exception");
        final Transaction transaction = new Transaction(transactionManager);

        when(transactionManager.isRollbackOnly()).thenReturn(false);
        org.mockito.Mockito.doThrow(runtimeException).when(transactionManager).rollback();

        // When
        final RuntimeException result = assertThrows(RuntimeException.class, transaction::close);

        // Then
        assertSame(runtimeException, result);
        assertThrows(IllegalStateException.class, transaction::commit);
    }
}
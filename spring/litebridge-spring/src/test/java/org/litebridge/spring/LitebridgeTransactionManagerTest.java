package org.litebridge.spring;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridge.db.spi.tx.Isolation;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LitebridgeTransactionManagerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Test
    void testTransactionParticipation() throws SQLException {
        // Given
        LitebridgeTransactionManager tm = new LitebridgeTransactionManager(dataSource);
        TransactionTemplate tt = new TransactionTemplate(tm);
        
        when(dataSource.getConnection()).thenReturn(connection);

        // When
        tt.executeWithoutResult(status -> {
            // Then
            assertTrue(tm.isTransactionActive());
            try {
                ManagedConnection managedConnection = tm.connection();
                assertNotNull(managedConnection);
                // Verify it's a wrapper by calling a method and seeing it delegated
                managedConnection.getAutoCommit();
                verify(connection, atLeastOnce()).getAutoCommit();
            } catch (SQLException e) {
                fail(e);
            }
        });

        // Verify connection was handled by Spring
        verify(connection).commit();
        verify(connection).close();
    }

    @Test
    void testCallbacks() throws SQLException {
        // Given
        LitebridgeTransactionManager tm = new LitebridgeTransactionManager(dataSource);
        TransactionTemplate tt = new TransactionTemplate(tm);
        AtomicBoolean commitCalled = new AtomicBoolean(false);
        AtomicBoolean rollbackCalled = new AtomicBoolean(false);

        when(dataSource.getConnection()).thenReturn(connection);

        // When
        tt.executeWithoutResult(status -> {
            tm.addCommitCallback(() -> commitCalled.set(true));
            tm.addRollbackCallback(() -> rollbackCalled.set(true));
        });

        // Then
        assertTrue(commitCalled.get());
        assertFalse(rollbackCalled.get());
    }

    @Test
    void testRollbackCallbacks() throws SQLException {
        // Given
        LitebridgeTransactionManager tm = new LitebridgeTransactionManager(dataSource);
        TransactionTemplate tt = new TransactionTemplate(tm);
        AtomicBoolean commitCalled = new AtomicBoolean(false);
        AtomicBoolean rollbackCalled = new AtomicBoolean(false);

        when(dataSource.getConnection()).thenReturn(connection);

        // When
        try {
            tt.executeWithoutResult(status -> {
                tm.addCommitCallback(() -> commitCalled.set(true));
                tm.addRollbackCallback(() -> rollbackCalled.set(true));
                throw new RuntimeException("Rollback");
            });
        } catch (RuntimeException e) {
            // Expected
        }

        // Then
        assertFalse(commitCalled.get());
        assertTrue(rollbackCalled.get());
        verify(connection).rollback();
    }

    @Test
    void testDirectTransactionOperationsAreUnsupported() {
        // Given
        LitebridgeTransactionManager tm = new LitebridgeTransactionManager(dataSource);

        // When / Then
        UnsupportedOperationException beginException = assertThrows(
                UnsupportedOperationException.class,
                tm::begin
        );
        assertEquals(
                "LitebridgeTransactionManager does not support direct begin(). Use Spring's transaction management (e.g., @Transactional).",
                beginException.getMessage()
        );

        UnsupportedOperationException beginWithOptionsException = assertThrows(
                UnsupportedOperationException.class,
                () -> tm.begin(true, Isolation.READ_COMMITTED)
        );
        assertEquals(
                "LitebridgeTransactionManager does not support direct begin(). Use Spring's transaction management (e.g., @Transactional).",
                beginWithOptionsException.getMessage()
        );

        UnsupportedOperationException commitException = assertThrows(
                UnsupportedOperationException.class,
                tm::commit
        );
        assertEquals(
                "LitebridgeTransactionManager does not support direct commit(). Use Spring's transaction management (e.g., @Transactional).",
                commitException.getMessage()
        );

        UnsupportedOperationException rollbackException = assertThrows(
                UnsupportedOperationException.class,
                tm::rollback
        );
        assertEquals(
                "LitebridgeTransactionManager does not support direct rollback(). Use Spring's transaction management (e.g., @Transactional).",
                rollbackException.getMessage()
        );
    }

    @Test
    void testRequiresCleanup() {
        // Given
        LitebridgeTransactionManager tm = new LitebridgeTransactionManager(dataSource);

        // When / Then
        assertTrue(tm.requiresCleanup());
        assertDoesNotThrow(tm::cleanup);
    }

    @Test
    void testStateOutsideTransaction() {
        // Given
        LitebridgeTransactionManager tm = new LitebridgeTransactionManager(dataSource);

        // When / Then
        assertFalse(tm.isTransactionActive());
        assertFalse(tm.isRollbackOnly());
    }

    @Test
    void testReadOnlyTransactionReportsRollbackOnly() throws SQLException {
        // Given
        LitebridgeTransactionManager tm = new LitebridgeTransactionManager(dataSource);
        TransactionTemplate tt = new TransactionTemplate(tm);
        tt.setReadOnly(true);

        when(dataSource.getConnection()).thenReturn(connection);

        // When / Then
        tt.executeWithoutResult(status -> {
            assertTrue(tm.isTransactionActive());
            assertTrue(tm.isRollbackOnly());
        });

        verify(connection).commit();
        verify(connection).close();
    }

    @Test
    void testCommitCallbackRunsImmediatelyWhenSynchronizationIsInactive() {
        // Given
        LitebridgeTransactionManager tm = new LitebridgeTransactionManager(dataSource);
        AtomicBoolean commitCalled = new AtomicBoolean(false);

        // When
        tm.addCommitCallback(() -> commitCalled.set(true));

        // Then
        assertTrue(commitCalled.get());
    }

    @Test
    void testRollbackCallbackDoesNothingWhenSynchronizationIsInactive() {
        // Given
        LitebridgeTransactionManager tm = new LitebridgeTransactionManager(dataSource);
        AtomicBoolean rollbackCalled = new AtomicBoolean(false);

        // When
        tm.addRollbackCallback(() -> rollbackCalled.set(true));

        // Then
        assertFalse(rollbackCalled.get());
    }

    @Test
    void testRollbackCallbackIsNotCalledAfterCommitCompletion() throws SQLException {
        // Given
        LitebridgeTransactionManager tm = new LitebridgeTransactionManager(dataSource);
        TransactionTemplate tt = new TransactionTemplate(tm);
        AtomicBoolean rollbackCalled = new AtomicBoolean(false);

        when(dataSource.getConnection()).thenReturn(connection);

        // When
        tt.executeWithoutResult(status -> tm.addRollbackCallback(() -> rollbackCalled.set(true)));

        // Then
        assertFalse(rollbackCalled.get());
        verify(connection).commit();
        verify(connection).close();
    }
}

package org.litebridge.spring;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
}

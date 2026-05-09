package org.litebridge.orm.api.tx;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.tx.Isolation;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.orm.tx.Transaction;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TransactionContextTest {

    @Test
    void readOnly() {
        // Given
        TransactionManager transactionManager = mock(TransactionManager.class);
        TransactionContext context = new TransactionContext(transactionManager);

        // When
        ReadOnlyClause result = context.readOnly();

        // Then
        assertNotNull(result);
        Transaction tx = result.begin();
        verify(transactionManager).begin(true, Isolation.DEFAULT);
        assertNotNull(tx);
    }

    @Test
    void isolation() {
        // Given
        TransactionManager transactionManager = mock(TransactionManager.class);
        TransactionContext context = new TransactionContext(transactionManager);

        // When
        IsolationClause result = context.isolation(Isolation.SERIALIZABLE);

        // Then
        assertNotNull(result);
        Transaction tx = result.begin();
        verify(transactionManager).begin(false, Isolation.SERIALIZABLE);
        assertNotNull(tx);
    }

    @Test
    void isolation_readOnly() {
        // Given
        TransactionManager transactionManager = mock(TransactionManager.class);
        TransactionContext context = new TransactionContext(transactionManager);

        // When
        Transaction tx = context.isolation(Isolation.READ_COMMITTED).readOnly().begin();

        // Then
        verify(transactionManager).begin(true, Isolation.READ_COMMITTED);
        assertNotNull(tx);
    }

    @Test
    void commit() {
        // Given
        TransactionManager transactionManager = mock(TransactionManager.class);
        TransactionContext context = new TransactionContext(transactionManager);

        // When
        context.commit();

        // Then
        verify(transactionManager).commit();
    }

    @Test
    void rollback() {
        // Given
        TransactionManager transactionManager = mock(TransactionManager.class);
        TransactionContext context = new TransactionContext(transactionManager);

        // When
        context.rollback();

        // Then
        verify(transactionManager).rollback();
    }

    @Test
    void execute() {
        // Given
        TransactionManager transactionManager = mock(TransactionManager.class);
        TransactionContext context = new TransactionContext(transactionManager);
        Runnable runnable = mock(Runnable.class);

        // When
        context.execute(runnable);

        // Then
        verify(transactionManager).begin(false, Isolation.DEFAULT);
        verify(runnable).run();
        verify(transactionManager).commit();
    }
}

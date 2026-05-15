package org.litebridgedb.orm.tx;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.tx.TransactionManager;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class DefaultTransactionManagerFactoryTest {

    @Test
    void create() {
        // Given
        final DefaultTransactionManagerFactory defaultTransactionManagerFactory = new DefaultTransactionManagerFactory();
        final DataSource dataSource = mock(DataSource.class);

        // When
        final TransactionManager transactionManager = defaultTransactionManagerFactory.create(dataSource);

        // Then
        assertNotNull(transactionManager);
        assertInstanceOf(DefaultTransactionManager.class, transactionManager);
    }
}
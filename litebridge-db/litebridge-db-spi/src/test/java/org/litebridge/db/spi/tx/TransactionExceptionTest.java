package org.litebridge.db.spi.tx;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class TransactionExceptionTest {

    @Test
    void testMessageConstructor() {
        TransactionException exception = new TransactionException("error");
        assertEquals("error", exception.getMessage());
    }

    @Test
    void testMessageAndCauseConstructor() {
        Throwable cause = new RuntimeException("cause");
        TransactionException exception = new TransactionException("error", cause);
        assertEquals("error", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}

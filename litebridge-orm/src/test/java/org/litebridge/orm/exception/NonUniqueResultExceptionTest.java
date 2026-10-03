package org.litebridge.orm.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NonUniqueResultExceptionTest {

    @Test
    void test() {
        // Given
        final NonUniqueResultException nonUniqueResultException = new NonUniqueResultException("Test message");

        // Then
        assertEquals("Test message", nonUniqueResultException.getMessage());
        assertNull(nonUniqueResultException.getCause());
    }
}
package org.litebridge.orm.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NonUniqueResultExceptionTest {

    @Test
    void test() {
        // Given
        final NonUniqueResultException nonUniqueResultException = new NonUniqueResultException("Test message", new RuntimeException());

        // Then
        assertEquals("Test message", nonUniqueResultException.getMessage());
        assertNotNull(nonUniqueResultException.getCause());
    }
}
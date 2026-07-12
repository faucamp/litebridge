package org.litebridge.orm.persistence.manytomany;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoOpFieldAccessorTest {

    private final NoOpFieldAccessor noOpFieldAccessor = new NoOpFieldAccessor();

    @Test
    void name() {
        // When
        final String result = noOpFieldAccessor.name();

        // Then
        assertEquals("no-op", result);
    }

    @Test
    void type() {
        // When
        final Class<?> result = noOpFieldAccessor.type();

        // Then
        assertEquals(Object.class, result);
    }

    @Test
    void dtoClass() {
        // When
        final Class<?> result = noOpFieldAccessor.dtoClass();

        // Then
        assertEquals(NoOpFieldAccessor.class, result);
    }

    @Test
    void get() {
        // Given
        final Object dto = new Object();

        // When/Then
        assertThrows(UnsupportedOperationException.class, () -> noOpFieldAccessor.get(dto));
    }

    @Test
    void set() {
        // Given
        final Object dto = new Object();

        // When/Then
        assertThrows(UnsupportedOperationException.class, () -> noOpFieldAccessor.set(dto, 123L));
    }

    @Test
    void genericTypes() {
        // When/Then
        assertThrows(UnsupportedOperationException.class, noOpFieldAccessor::genericTypes);
    }

    @Test
    void genericType() {
        // When/Then
        assertThrows(UnsupportedOperationException.class, noOpFieldAccessor::genericType);
    }
}
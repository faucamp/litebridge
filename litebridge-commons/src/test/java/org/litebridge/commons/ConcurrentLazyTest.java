package org.litebridge.commons;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrentLazyTest {

    @Test
    void optional() {
        // Given
        final ConcurrentLazy<String> concurrentLazy = new ConcurrentLazy<>(() -> "hello");

        // When
        final Optional<String> result = concurrentLazy.optional();

        // Then
        assertTrue(result.isPresent());
        assertEquals("hello", result.get());
    }

    @Test
    void orNull() {
        // Given
        final ConcurrentLazy<String> concurrentLazy = new ConcurrentLazy<>(() -> "hello");

        // When
        final String result = concurrentLazy.orNull();

        // Then
        assertEquals("hello", result);
    }

    @Test
    void peek_notInitialised() {
        // Given
        final ConcurrentLazy<String> concurrentLazy = new ConcurrentLazy<>(() -> "hello");

        // When
        final String result = concurrentLazy.peek();

        // Then
        assertNull(result);
    }

    @Test
    void peek_initialised() {
        // Given
        final ConcurrentLazy<String> concurrentLazy = new ConcurrentLazy<>(() -> "hello");
        concurrentLazy.optional();

        // When
        final String result = concurrentLazy.peek();

        // Then
        assertEquals("hello", result);
    }

    @Test
    void reset() {
        // Given
        final ConcurrentLazy<String> concurrentLazy = new ConcurrentLazy<>(() -> "hello");
        concurrentLazy.optional();
        // Sanity check
        assertEquals("hello", concurrentLazy.peek());

        // When
        concurrentLazy.reset();

        // Then
        assertNull(concurrentLazy.peek());
    }

    @Test
    void isInitialised_true() {
        // Given
        final ConcurrentLazy<String> concurrentLazy = new ConcurrentLazy<>(() -> "hello");
        concurrentLazy.optional();

        // When
        final boolean result = concurrentLazy.isInitialised();

        // Then
        assertTrue(result);
    }

    @Test
    void isInitialised_false() {
        // Given
        final ConcurrentLazy<String> concurrentLazy = new ConcurrentLazy<>(() -> "hello");

        // When
        final boolean result = concurrentLazy.isInitialised();

        // Then
        assertFalse(result);
    }
}
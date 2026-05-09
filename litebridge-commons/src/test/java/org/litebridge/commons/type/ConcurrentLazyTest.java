package org.litebridge.commons.type;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
    void orThrow() {
        // Given
        final ConcurrentLazy<String> concurrentLazy = new ConcurrentLazy<>(() -> "hello");

        // When
        final String result = concurrentLazy.orThrow();

        // Then
        assertEquals("hello", result);
    }

    @Test
    void orThrow_null() {
        // Given
        final ConcurrentLazy<String> concurrentLazy = new ConcurrentLazy<>(() -> null);

        // When / Then
        assertThrows(java.util.NoSuchElementException.class, concurrentLazy::orThrow);
    }

    @Test
    void orThrow_exceptionSupplier() throws Exception {
        // Given
        final ConcurrentLazy<String> concurrentLazy = new ConcurrentLazy<>(() -> "hello");

        // When
        final String result = concurrentLazy.orThrow(() -> new Exception("test"));

        // Then
        assertEquals("hello", result);
    }

    @Test
    void orThrow_exceptionSupplier_null() {
        // Given
        final ConcurrentLazy<String> concurrentLazy = new ConcurrentLazy<>(() -> null);

        // When / Then
        assertThrows(Exception.class, () -> concurrentLazy.orThrow(() -> new Exception("test")));
    }

    @Test
    void orNull_concurrent() {
        // Given
        final int threadCount = 5;
        final ConcurrentLazy<String> concurrentLazy = new ConcurrentLazy<>(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                fail(ex.getMessage(), ex);
            }

            return "hello";
        });

        final Thread[] threads = new Thread[threadCount];
        final @Nullable String[] threadResults = new String[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> threadResults[index] = concurrentLazy.orNull());
        }

        for (int i = 0; i < threadCount; i++) {
            try {
                threads[i].join(100);
            } catch (InterruptedException ex) {
                fail(ex.getMessage(), ex);
            }
        }

        // When
        for (Thread thread : threads) {
            thread.start();
        }

        // Then
        for (int i = 0; i < threadCount; i++) {
            try {
                threads[i].join(100);
            } catch (InterruptedException ex) {
                fail(ex.getMessage(), ex);
            }

            assertEquals("hello", threadResults[i]);
        }

        assertEquals("hello", concurrentLazy.orNull());
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
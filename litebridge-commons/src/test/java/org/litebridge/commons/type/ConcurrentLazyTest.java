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
    void get() {
        // Given
        final ConcurrentLazy<String> concurrentLazy = new ConcurrentLazy<>(() -> "hello");

        // When
        final Optional<String> result = concurrentLazy.get();

        // Then
        assertTrue(result.isPresent());
        assertEquals("hello", result.get());
    }

    @Test
    void getOrNull() {
        // Given
        final ConcurrentLazy<String> concurrentLazy = new ConcurrentLazy<>(() -> "hello");

        // When
        final String result = concurrentLazy.getOrNull();

        // Then
        assertEquals("hello", result);
    }

    @Test
    void getOrThrow() {
        // Given
        final ConcurrentLazy<String> concurrentLazy = new ConcurrentLazy<>(() -> "hello");

        // When
        final String result = concurrentLazy.getOrThrow();

        // Then
        assertEquals("hello", result);
    }

    @Test
    void getOrThrow_null() {
        // Given
        final ConcurrentLazy<String> concurrentLazy = new ConcurrentLazy<>(() -> null);

        // When / Then
        assertThrows(java.util.NoSuchElementException.class, concurrentLazy::getOrThrow);
    }

    @Test
    void getOrThrow_exceptionSupplier() throws Exception {
        // Given
        final ConcurrentLazy<String> concurrentLazy = new ConcurrentLazy<>(() -> "hello");

        // When
        final String result = concurrentLazy.getOrThrow(() -> new Exception("test"));

        // Then
        assertEquals("hello", result);
    }

    @Test
    void getOrThrow_exceptionSupplier_null() {
        // Given
        final ConcurrentLazy<String> concurrentLazy = new ConcurrentLazy<>(() -> null);

        // When / Then
        assertThrows(Exception.class, () -> concurrentLazy.getOrThrow(() -> new Exception("test")));
    }

    @Test
    void getOrNull_concurrent() {
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
            threads[i] = new Thread(() -> threadResults[index] = concurrentLazy.getOrNull());
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

        assertEquals("hello", concurrentLazy.getOrNull());
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
        concurrentLazy.get();

        // When
        final String result = concurrentLazy.peek();

        // Then
        assertEquals("hello", result);
    }

    @Test
    void reset() {
        // Given
        final ConcurrentLazy<String> concurrentLazy = new ConcurrentLazy<>(() -> "hello");
        concurrentLazy.get();
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
        concurrentLazy.get();

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
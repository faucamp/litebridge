package org.litebridge.commons.type;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ConcurrentLazyFunctionTest {

    @Test
    void get() {
        // Given
        final ConcurrentLazyFunction<Integer, String> concurrentLazy = new ConcurrentLazyFunction<>(value -> "hello " + value);
        final Integer value = 123;

        // When
        final Optional<String> result = concurrentLazy.get(value);

        // Then
        assertTrue(result.isPresent());
        assertEquals("hello 123", result.get());
    }

    @Test
    void getOrNull() {
        // Given
        final ConcurrentLazyFunction<Integer, String> concurrentLazy = new ConcurrentLazyFunction<>(value -> "hello " + value);
        final Integer value = 123;

        // When
        final String result = concurrentLazy.getOrNull(value);

        // Then
        assertEquals("hello 123", result);
    }

    @Test
    void getOrThrow() {
        // Given
        final ConcurrentLazyFunction<Integer, String> concurrentLazy = new ConcurrentLazyFunction<>(value -> "hello " + value);
        final Integer value = 123;

        // When
        final String result = concurrentLazy.getOrThrow(value);

        // Then
        assertEquals("hello 123", result);
    }

    @Test
    void getOrThrow_null() {
        // Given
        final ConcurrentLazyFunction<Integer, String> concurrentLazy = new ConcurrentLazyFunction<>(value -> null);
        final Integer value = 123;

        // When / Then
        assertThrows(NoSuchElementException.class, () -> concurrentLazy.getOrThrow(value));
    }

    @Test
    void getOrThrow_exceptionSupplier() throws Exception {
        // Given
        final ConcurrentLazyFunction<Integer, String> concurrentLazy = new ConcurrentLazyFunction<>(value -> "hello " + value);
        final Integer value = 123;

        // When
        final String result = concurrentLazy.getOrThrow(value, () -> new Exception("test"));

        // Then
        assertEquals("hello 123", result);
    }

    @Test
    void getOrThrow_exceptionSupplier_null() {
        // Given
        final ConcurrentLazyFunction<Integer, String> concurrentLazy = new ConcurrentLazyFunction<>(value -> null);
        final Integer value = 123;

        // When / Then
        assertThrows(Exception.class, () -> concurrentLazy.getOrThrow(value, () -> new Exception("test")));
    }

    @Test
    void getOrNull_concurrent() {
        // Given
        final int threadCount = 5;
        final ConcurrentLazyFunction<Integer, String> concurrentLazy = new ConcurrentLazyFunction<>(value -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                fail(ex.getMessage(), ex);
            }

            return "hello " + value;
        });

        final Thread[] threads = new Thread[threadCount];
        final @Nullable String[] threadResults = new String[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> threadResults[index] = concurrentLazy.getOrNull(index));
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

            assertEquals("hello " + i, threadResults[i]);
        }

        assertEquals("hello 0", concurrentLazy.getOrNull(0));
    }

    @Test
    void peek_notInitialised() {
        // Given
        final ConcurrentLazyFunction<Integer, String> concurrentLazy = new ConcurrentLazyFunction<>(value -> "hello " + value);

        // When
        final String result = concurrentLazy.peek();

        // Then
        assertNull(result);
    }

    @Test
    void peek_initialised() {
        // Given
        final ConcurrentLazyFunction<Integer, String> concurrentLazy = new ConcurrentLazyFunction<>(value -> "hello " + value);
        concurrentLazy.get(123);

        // When
        final String result = concurrentLazy.peek();

        // Then
        assertEquals("hello", result);
    }

    @Test
    void reset() {
        // Given
        final ConcurrentLazyFunction<Integer, String> concurrentLazy = new ConcurrentLazyFunction<>(value -> "hello " + value);
        concurrentLazy.get(123);
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
        final ConcurrentLazyFunction<Integer, String> concurrentLazy = new ConcurrentLazyFunction<>(value -> "hello " + value);
        concurrentLazy.get(123);

        // When
        final boolean result = concurrentLazy.isInitialised();

        // Then
        assertTrue(result);
    }

    @Test
    void isInitialised_false() {
        // Given
        final ConcurrentLazyFunction<Integer, String> concurrentLazy = new ConcurrentLazyFunction<>(value -> "hello " + value);

        // When
        final boolean result = concurrentLazy.isInitialised();

        // Then
        assertFalse(result);
    }
}
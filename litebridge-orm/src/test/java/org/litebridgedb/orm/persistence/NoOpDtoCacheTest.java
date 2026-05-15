package org.litebridgedb.orm.persistence;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;

class NoOpDtoCacheTest {

    @Test
    void get() {
        // Given
        final NoOpDtoCache dtoCache = NoOpDtoCache.INSTANCE;

        // When
        final TestDto result = dtoCache.get(TestDto.class, List.of("test"));

        // Then
        assertNull(result);
    }

    @Test
    void put() {
        // Given
        final NoOpDtoCache dtoCache = NoOpDtoCache.INSTANCE;

        // When
        dtoCache.put(List.of("test"), new TestDto());

        // Then
        assertNull(dtoCache.get(TestDto.class, List.of("test")));
    }

    @Test
    void getAll() {
        // Given
        final NoOpDtoCache dtoCache = NoOpDtoCache.INSTANCE;

        // When
        final List<TestDto> result = dtoCache.getAll(TestDto.class);

        // Then
        assertNull(result);
    }

    private static class TestDto {
        private String myVar;
    }
}
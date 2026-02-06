package org.litebridge.orm.persistence;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DtoCacheImplTest {

    @Test
    void get() {
        // Given
        final DtoCacheImpl dtoCache = new DtoCacheImpl();
        final TestDto testDto = new TestDto();
        dtoCache.put(List.of("test"), testDto);

        // When
        final TestDto result = dtoCache.get(TestDto.class, List.of("test"));

        // Then
        assertSame(testDto, result);
    }

    @Test
    void get_notFound() {
        // Given
        final DtoCacheImpl dtoCache = new DtoCacheImpl();

        // When
        final TestDto result = dtoCache.get(TestDto.class, List.of("test"));

        // Then
        assertNull(result);
    }

    @Test
    void put_null() {
        // Given
        final DtoCacheImpl dtoCache = new DtoCacheImpl();

        // When
        dtoCache.put(List.of("test"), null);

        // Then
        assertNull(dtoCache.get(TestDto.class, List.of("test")));
    }

    private static class TestDto {
        private String myVar;
    }
}
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

    @Test
    void getAll_withEmptyCache() {
        // Given
        final DtoCacheImpl dtoCache = new DtoCacheImpl();

        // When
        final List<TestDto> result = dtoCache.getAll(TestDto.class);

        // Then
        assertNull(result);
    }

    @Test
    void getAll_withData() {
        // Given
        final DtoCacheImpl dtoCache = new DtoCacheImpl();
        final TestDto testDto1 = new TestDto();
        final TestDto testDto2 = new TestDto();
        dtoCache.put(List.of("id1"), testDto1);
        dtoCache.put(List.of("id2"), testDto2);

        // When
        final List<TestDto> result = dtoCache.getAll(TestDto.class);

        // Then
        assertSame(2, result.size());
        assertSame(testDto1, result.get(0));
        assertSame(testDto2, result.get(1));
    }

    private static class TestDto {
        private String myVar;
    }
}
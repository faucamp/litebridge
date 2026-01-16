package org.litebridge.orm.persistence;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.litebridge.orm.api.spec.FieldSpecBuilder.f;

class EntityDtoMapperTest {

    @Test
    void dtoClass() {
        // Given
        final EntityDtoMapper<TestDto> entityDtoMapper = new EntityDtoMapper<>(TestDto.class,
                List.of(new DtoEntityMapping(TestEntity1.class,
                                java.util.Map.of(
                                        f("dtoValue1"), f("entityValue1")
                                )),
                        new DtoEntityMapping(TestEntity2.class,
                                java.util.Map.of(
                                        f("dtoValue2"), f("entityValue2")
                                ))));

        // When
        final Class<TestDto> result = entityDtoMapper.dtoClass();

        // Then
        assertEquals(TestDto.class, result);
    }

    @Test
    void entityClasses() {
        // Given
        final EntityDtoMapper<TestDto> entityDtoMapper = new EntityDtoMapper<>(TestDto.class,
                List.of(new DtoEntityMapping(TestEntity1.class,
                                java.util.Map.of(
                                        f("dtoValue1"), f("entityValue1")
                                )),
                        new DtoEntityMapping(TestEntity2.class,
                                java.util.Map.of(
                                        f("dtoValue2"), f("entityValue2")
                                ))));

        // When
        final List<Class<?>> result = entityDtoMapper.entityClasses();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(TestEntity1.class));
        assertTrue(result.contains(TestEntity2.class));
    }

    @Test
    void entities() {
        // Given
        final EntityDtoMapper<TestDto> entityDtoMapper = new EntityDtoMapper<>(TestDto.class,
                List.of(new DtoEntityMapping(TestEntity1.class,
                                java.util.Map.of(
                                        f("dtoValue1"), f("entityValue1")
                                )),
                        new DtoEntityMapping(TestEntity2.class,
                                java.util.Map.of(
                                        f("dtoValue2"), f("entityValue2")
                                ))));

        final TestDto testDto = new TestDto();
        testDto.dtoValue1 = "testValue1";
        testDto.dtoValue2 = 123L;

        // When
        final List<Object> result = entityDtoMapper.entities(testDto);

        // Then
        assertEquals(2, result.size());
        final TestEntity1 testEntity1 = result.stream()
                .filter(e -> e instanceof TestEntity1)
                .map(e -> (TestEntity1) e)
                .findFirst().orElseThrow();
        assertEquals("testValue1", testEntity1.entityValue1);
        final TestEntity2 testEntity2 = result.stream()
                .filter(e -> e instanceof TestEntity2)
                .map(e -> (TestEntity2) e)
                .findFirst().orElseThrow();
        assertEquals(123L, testEntity2.entityValue2);
    }

    @Test
    void dto() {
        // Given
        final EntityDtoMapper<TestDto> entityDtoMapper = new EntityDtoMapper<>(TestDto.class,
                List.of(new DtoEntityMapping(TestEntity1.class,
                                java.util.Map.of(
                                        f("dtoValue1"), f("entityValue1")
                                )),
                        new DtoEntityMapping(TestEntity2.class,
                                java.util.Map.of(
                                        f("dtoValue2"), f("entityValue2")
                                ))));

        final TestEntity1 testEntity1 = new TestEntity1();
        testEntity1.entityValue1 = "testValue1";

        final TestEntity2 testEntity2 = new TestEntity2();
        testEntity2.entityValue2 = 123L;

        // When
        final TestDto result = entityDtoMapper.dto(testEntity1, testEntity2);

        // Then
        assertEquals("testValue1", result.dtoValue1);
        assertEquals(123L, result.dtoValue2);
    }

    public static class TestDto {
        private String dtoValue1;
        private Long dtoValue2;
    }

    public static class TestEntity1 {
        private String entityValue1;
    }

    public static class TestEntity2 {
        private Long entityValue2;
    }
}
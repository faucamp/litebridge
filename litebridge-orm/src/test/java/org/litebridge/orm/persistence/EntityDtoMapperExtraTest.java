package org.litebridge.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridge.tracking.ClassFieldAccessorCache;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.litebridge.orm.api.spec.FieldMapping.f;

class EntityDtoMapperExtraTest {

    @Test
    void dto_withMissingEntity() {
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final EntityDtoMapper<TestDto> mapper = new EntityDtoMapper<>(TestDto.class,
                List.of(new DtoEntityMapping(TestEntity1.class, Map.of(f("val1"), f("val1"))),
                        new DtoEntityMapping(TestEntity2.class, Map.of(f("val2"), f("val2")))),
                cache);

        final TestEntity1 entity1 = new TestEntity1();
        entity1.val1 = "v1";

        // Only entity1 is provided, entity2 is missing
        final TestDto dto = mapper.dto(List.of(entity1));

        assertNotNull(dto);
        assertEquals("v1", dto.val1);
        assertNull(dto.val2);
    }

    @Test
    void entities_withNullDto() {
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final EntityDtoMapper<TestDto> mapper = new EntityDtoMapper<>(TestDto.class,
                List.of(new DtoEntityMapping(TestEntity1.class, Map.of(f("val1"), f("val1")))),
                cache);

        assertThrows(NullPointerException.class, () -> mapper.entities(null));
    }

    @Test
    void dto_withPrimitives() {
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final EntityDtoMapper<PrimitiveDto> mapper = new EntityDtoMapper<>(PrimitiveDto.class,
                List.of(new DtoEntityMapping(PrimitiveEntity.class, Map.of(
                        f("boolVal"), f("boolVal"),
                        f("intVal"), f("intVal"),
                        f("longVal"), f("longVal"),
                        f("charVal"), f("charVal"),
                        f("floatVal"), f("floatVal"),
                        f("doubleVal"), f("doubleVal")
                ))),
                cache);

        final PrimitiveEntity entity = new PrimitiveEntity();
        entity.boolVal = true;
        entity.intVal = 10;
        entity.longVal = 20L;
        entity.charVal = 'A';
        entity.floatVal = 1.5f;
        entity.doubleVal = 2.5;

        final PrimitiveDto dto = mapper.dto(entity);
        assertTrue(dto.boolVal);
        assertEquals(10, dto.intVal);
        assertEquals(20L, dto.longVal);
        assertEquals('A', dto.charVal);
        assertEquals(1.5f, dto.floatVal);
        assertEquals(2.5, dto.doubleVal);

        // Test that values are NOT overridden if already set (and not default)
        final PrimitiveEntity entity2 = new PrimitiveEntity();
        entity2.boolVal = false;
        entity2.intVal = 0;
        entity2.longVal = 0L;
        entity2.charVal = 0;
        entity2.floatVal = 0.0f;
        entity2.doubleVal = 0.0;

        final PrimitiveDto dto2 = mapper.dto(entity, entity2);
        assertTrue(dto2.boolVal);
        assertEquals(10, dto2.intVal);
        assertEquals(20L, dto2.longVal);
        assertEquals('A', dto2.charVal);
        assertEquals(1.5f, dto2.floatVal);
        assertEquals(2.5, dto2.doubleVal);
    }

    @Test
    void dto_withNullEntities() {
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final EntityDtoMapper<TestDto> mapper = new EntityDtoMapper<>(TestDto.class,
                List.of(new DtoEntityMapping(TestEntity1.class, Map.of(f("val1"), f("val1")))),
                cache);

        assertThrows(NullPointerException.class, () -> mapper.dto((List<Object>) null));
    }

    @Test
    void entities_withNestedDto() {
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final EntityDtoMapper<NestedDto> mapper = new EntityDtoMapper<>(NestedDto.class,
                List.of(new DtoEntityMapping(NestedEntity.class, Map.of(
                        f("id"), f("id"),
                        f("child.name"), f("child.name")
                ))),
                cache);

        final NestedDto dto = new NestedDto();
        dto.id = 1L;
        dto.child = new ChildDto();
        dto.child.name = "childName";

        final List<Object> entities = mapper.entities(dto);
        assertEquals(2, entities.size());
        final NestedEntity entity = (NestedEntity) entities.stream()
                .filter(e -> e instanceof NestedEntity)
                .findFirst().orElseThrow();
        assertEquals(1L, entity.id);
        assertNotNull(entity.child);
        assertEquals("childName", entity.child.name);

        final ChildEntity childEntity = (ChildEntity) entities.stream()
                .filter(e -> e instanceof ChildEntity)
                .findFirst().orElseThrow();
        assertEquals("childName", childEntity.name);
    }

    @Test
    void dto_withNestedEntity() {
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final EntityDtoMapper<NestedDto> mapper = new EntityDtoMapper<>(NestedDto.class,
                List.of(new DtoEntityMapping(NestedEntity.class, Map.of(
                        f("id"), f("id"),
                        f("child"), f("child")
                ))),
                cache);

        final NestedEntity entity = new NestedEntity();
        entity.id = 1L;
        entity.child = new ChildEntity();
        entity.child.name = "childName";

        final NestedDto dto = mapper.dto(entity);
        assertEquals(1L, dto.id);
        // Currently EntityDtoMapper has a 'return;' for nested entities in dto()
        assertNull(dto.child);
    }

    public static class TestDto {
        private String val1;
        private String val2;
    }

    public static class TestEntity1 {
        private String val1;
    }

    public static class TestEntity2 {
        private String val2;
    }

    public static class PrimitiveDto {
        private boolean boolVal;
        private int intVal;
        private long longVal;
        private char charVal;
        private float floatVal;
        private double doubleVal;
    }

    public static class PrimitiveEntity {
        private boolean boolVal;
        private int intVal;
        private long longVal;
        private char charVal;
        private float floatVal;
        private double doubleVal;
    }

    public static class NestedDto {
        private Long id;
        private ChildDto child;
    }

    public static class ChildDto {
        private String name;
    }

    public static class NestedEntity {
        private Long id;
        private ChildEntity child;
    }

    public static class ChildEntity {
        private String name;
    }
}

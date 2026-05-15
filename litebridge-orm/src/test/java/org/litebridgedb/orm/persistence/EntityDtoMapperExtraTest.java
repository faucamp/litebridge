package org.litebridgedb.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridgedb.tracking.ClassFieldAccessorCache;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.litebridgedb.orm.api.spec.FieldMapping.f;

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
}

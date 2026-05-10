package org.litebridge.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;

import java.lang.invoke.MethodHandles;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoConstructorExtraTest {

    @Test
    void newInstance_record() {
        ClassFieldAccessorCache cache = new ClassFieldAccessorCache(MethodHandles.lookup());
        FieldAccessor idAccessor = cache.fieldAccessor(TestRecord.class, "id");
        FieldAccessor nameAccessor = cache.fieldAccessor(TestRecord.class, "name");

        List<DtoConstructor.FieldAccessorValue> values = List.of(
                new DtoConstructor.FieldAccessorValue(idAccessor, 1L),
                new DtoConstructor.FieldAccessorValue(nameAccessor, "test")
        );

        DtoConstructor.ConstructionResult<TestRecord> result = DtoConstructor.newInstance(TestRecord.class, values);
        assertNotNull(result.dto());
        assertEquals(1L, result.dto().id());
        assertEquals("test", result.dto().name());
    }

    @Test
    void newInstance_pojo_multipleConstructors_matchByType() {
        ClassFieldAccessorCache cache = new ClassFieldAccessorCache(MethodHandles.lookup());
        FieldAccessor idAccessor = cache.fieldAccessor(MultiConstructorDto.class, "id");
        FieldAccessor nameAccessor = cache.fieldAccessor(MultiConstructorDto.class, "name");

        List<DtoConstructor.FieldAccessorValue> values = List.of(
                new DtoConstructor.FieldAccessorValue(idAccessor, 1L),
                new DtoConstructor.FieldAccessorValue(nameAccessor, "test")
        );

        DtoConstructor.ConstructionResult<MultiConstructorDto> result = DtoConstructor.newInstance(MultiConstructorDto.class, values);
        assertNotNull(result.dto());
        assertEquals(1L, result.dto().id);
        assertEquals("test", result.dto().name);
    }

    public record TestRecord(Long id, String name) {}

    public static class MultiConstructorDto {
        private Long id;
        private String name;

        public MultiConstructorDto(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}

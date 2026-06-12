package org.litebridgedb.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.tracking.ClassFieldAccessorCache;
import org.litebridgedb.tracking.FieldAccessor;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DtoConstructorExtraTest {

    @Test
    void newInstance_record() {
        ClassFieldAccessorCache cache = new ClassFieldAccessorCache(MethodHandles.lookup());
        FieldAccessor idAccessor = cache.fieldAccessor(TestRecord.class, "id");
        FieldAccessor nameAccessor = cache.fieldAccessor(TestRecord.class, "name");
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final OrmTable ormTable = mock(OrmTable.class);
        when(tableRegistry.getTableOrThrow(MultiConstructorDto.class)).thenReturn(ormTable);
        when(ormTable.fieldAcessorStream()).thenReturn(Stream.of(idAccessor, nameAccessor));
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);

        List<DtoConstructor.FieldAccessorValue> values = List.of(
                new DtoConstructor.FieldAccessorValue(idAccessor, 1L),
                new DtoConstructor.FieldAccessorValue(nameAccessor, "test")
        );

        DtoConstructor.ConstructionResult<TestRecord> result = dtoConstructor.newInstance(TestRecord.class, values);
        assertNotNull(result.dto());
        assertEquals(1L, result.dto().id());
        assertEquals("test", result.dto().name());
    }

    @Test
    void newInstance_pojo_multipleConstructors_matchByType() {
        ClassFieldAccessorCache cache = new ClassFieldAccessorCache(MethodHandles.lookup());
        FieldAccessor idAccessor = cache.fieldAccessor(MultiConstructorDto.class, "id");
        FieldAccessor nameAccessor = cache.fieldAccessor(MultiConstructorDto.class, "name");
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final OrmTable ormTable = mock(OrmTable.class);
        when(tableRegistry.getTableOrThrow(MultiConstructorDto.class)).thenReturn(ormTable);
        when(ormTable.fieldAcessorStream()).thenReturn(Stream.of(idAccessor, nameAccessor));
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);

        List<DtoConstructor.FieldAccessorValue> values = List.of(
                new DtoConstructor.FieldAccessorValue(idAccessor, 1L),
                new DtoConstructor.FieldAccessorValue(nameAccessor, "test")
        );

        DtoConstructor.ConstructionResult<MultiConstructorDto> result = dtoConstructor.newInstance(MultiConstructorDto.class, values);
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

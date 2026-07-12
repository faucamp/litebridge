package org.litebridge.tracking;

import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class TrackedDtoTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackedDtoTest.class.getName());

    @Test
    void dto() {
        // Given
        final TestDto testDto = new TestDto();
        final TrackedDto<TestDto> trackedDto = new TrackedDto<>(testDto, new ClassFieldAccessorCache(MethodHandles.lookup()), dto -> fail());

        // When
        final TestDto result = trackedDto.dto();

        // Then
        assertEquals(testDto, result);
    }

    @Test
    void dto_garbageCollected_throwsIllegalStateException() throws Exception {
        // Given
        final TestDto testDto = new TestDto();
        final TrackedDto<TestDto> trackedDto = new TrackedDto<>(testDto, new ClassFieldAccessorCache(MethodHandles.lookup()), dto -> fail());
        final Field dtoRefField = TrackedDto.class.getDeclaredField("dtoRef");
        dtoRefField.setAccessible(true);
        dtoRefField.set(trackedDto, new WeakReference<>(null));

        // When
        final IllegalStateException ex = assertThrows(IllegalStateException.class, trackedDto::dto);

        // Then
        assertTrue(ex.getMessage().contains("DTO object has been garbage collected"));
    }

    @Test
    void constructor_nullArguments() {
        // Given
        final TestDto testDto = new TestDto();
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final List<FieldAccessor> fieldAccessors = fieldAccessors();

        // When/Then
        assertThrows(NullPointerException.class, () -> new TrackedDto<>(null, classFieldAccessorCache, dto -> fail()));
        assertThrows(NullPointerException.class, () -> new TrackedDto<>(testDto, null, classFieldAccessorCache, dto -> fail()));
        assertThrows(NullPointerException.class, () -> new TrackedDto<>(testDto, fieldAccessors, null, dto -> fail()));
        assertThrows(NullPointerException.class, () -> new TrackedDto<>(testDto, fieldAccessors, classFieldAccessorCache, null));
    }

    @Test
    @SuppressWarnings("unchecked")
    void snapshot() {
        // Given
        final TestDto testDto = new TestDto();
        testDto.string = "value1";
        testDto.nestedDto = new NestedDto();
        testDto.map = new HashMap<>();
        testDto.map.put("key1", 1L);
        testDto.list = new ArrayList<>();
        testDto.list.add("item1");
        testDto.nestedDtoList = new ArrayList<>();
        testDto.nestedDtoList.add(new NestedDto());
        testDto.nestedDtoMap = new HashMap<>();
        testDto.nestedDtoMap.put("key1", new NestedDto());
        testDto.nestedDtoKeyMap = new HashMap<>();
        final NestedDto nestedDtoKeyMapKey = new NestedDto();
        testDto.nestedDtoKeyMap.put(nestedDtoKeyMapKey, 1L);

        final boolean[] nestedDtosRegistered = {false, false, false, false};

        final Consumer<Object> consumer = dto -> {
            if (dto == testDto.nestedDto) {
                nestedDtosRegistered[0] = true;
            } else if (dto == testDto.nestedDtoList.get(0)) {
                nestedDtosRegistered[1] = true;
            } else if (dto == testDto.nestedDtoMap.get("key1")) {
                nestedDtosRegistered[2] = true;
            } else if (dto == nestedDtoKeyMapKey) {
                nestedDtosRegistered[3] = true;
            }
        };

        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final List<FieldAccessor> fieldAccessors = ClassUtils.getAllFields(TestDto.class, MethodHandles.lookup()).stream()
                .map(field -> (FieldAccessor) new DirectFieldAccessor(field, MethodHandles.lookup()))
                .toList();
        final TrackedDto<TestDto> trackedDto = new TrackedDto<>(testDto, fieldAccessors, classFieldAccessorCache, consumer);

        // When 1
        trackedDto.snapshot(true);
        // Then 1
        assertTrue(trackedDto.changedFields().isEmpty());

        // When 2
        trackedDto.snapshot(true);
        testDto.string = "value2";
        // Then 2
        assertEquals(1, trackedDto.changedFields().size());
        assertTrue(trackedDto.changedFields().contains("string"));

        for (int i = 0; i < nestedDtosRegistered.length; i++) {
            assertTrue(nestedDtosRegistered[i], "Nested DTO not registered at index " + i);
        }
    }

    @Test
    void snapshot_overwriteTrue_clearsCachedChangedFields() {
        // Given
        final TestDto testDto = new TestDto();
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final TrackedDto<TestDto> trackedDto = new TrackedDto<>(testDto, fieldAccessors(), classFieldAccessorCache, dto -> fail());

        trackedDto.snapshot(false);
        testDto.string = "value1";
        final ChangedFields firstChangedFields = trackedDto.changedFields();
        assertTrue(firstChangedFields.contains("string"));

        // When
        trackedDto.snapshot(true);

        // Then
        final ChangedFields changedFieldsAfterOverwrite = trackedDto.changedFields();
        assertTrue(changedFieldsAfterOverwrite.isEmpty());
        assertNotSame(firstChangedFields, changedFieldsAfterOverwrite);
    }

    @Test
    void snapshotEmpty_twice_overwriteFalse() {
        // Given
        final TestDto testDto = new TestDto();
        testDto.string = "value1";

        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "string"), MethodHandles.lookup());
        final TrackedDto<TestDto> trackedDto = new TrackedDto<>(testDto, List.of(fieldAccessor), classFieldAccessorCache, dto -> fail());


        // When/Then
        trackedDto.snapshot(false);
        assertThrows(IllegalStateException.class, () -> trackedDto.snapshot(false));
    }

    @Test
    void snapshotEmpty() {
        // Given
        final TestDto testDto = new TestDto();
        testDto.string = "value1";

        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "string"), MethodHandles.lookup());
        final TrackedDto<TestDto> trackedDto = new TrackedDto<>(testDto, List.of(fieldAccessor), classFieldAccessorCache, dto -> fail());

        // When
        trackedDto.snapshotEmpty();

        // Then
        assertEquals(1, trackedDto.changedFields().size());
        assertTrue(trackedDto.changedFields().contains("string"));
    }

    @Test
    void snapshotEmpty_twice() {
        // Given
        final TestDto testDto = new TestDto();
        testDto.string = "updatedValue";

        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final TrackedDto<TestDto> trackedDto = new TrackedDto(testDto, classFieldAccessorCache, dto -> fail());
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "string"), MethodHandles.lookup());

        // When/Then
        trackedDto.snapshotEmpty();
        assertThrows(IllegalStateException.class, trackedDto::snapshotEmpty);
    }

    @Test
    void snapshot_circularReference() {
        // Given
        final OneToManyParentDto parent = new OneToManyParentDto();
        parent.id = 1L;
        parent.children = new ArrayList<>();

        final ManyToOneChildDto child1 = new ManyToOneChildDto();
        child1.id = 2L;
        child1.name = "child1";
        child1.parent = parent;
        parent.children.add(child1);

        final ManyToOneChildDto child2 = new ManyToOneChildDto();
        child2.id = 3L;
        child2.name = "child2";
        child2.parent = parent;
        parent.children.add(child2);

        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final TrackedDto<OneToManyParentDto> trackedDto = new TrackedDto<>(parent, classFieldAccessorCache, dto -> {
            LOGGER.debug("Snapshotting DTO: {}", dto);
        });

        // When
        trackedDto.snapshot(true);

        // Then
        assertTrue(trackedDto.changedFields().isEmpty());

        // When
        LOGGER.debug("Updating child1");
        child1.name = "updatedChild1";

        // Then
        final ChangedFields updatedChangedFields = trackedDto.changedFields(true);
        assertEquals(1, updatedChangedFields.size());
        assertTrue(trackedDto.changedFields().contains("children"));
    }

    @Test
    void changedFields() {
        // Given
        final TestDto testDto = new TestDto();
        final boolean[] nestedDtosRegistered = {false, false, false, false};

        final Consumer<Object> consumer = dto -> {
            if (dto == testDto.nestedDto) {
                nestedDtosRegistered[0] = true;
            } else if (testDto.nestedDtoList != null && dto == testDto.nestedDtoList.get(0)) {
                nestedDtosRegistered[1] = true;
            } else if (testDto.nestedDtoMap != null && dto == testDto.nestedDtoMap.get("key1")) {
                nestedDtosRegistered[2] = true;
            } else {
                nestedDtosRegistered[3] = true;
            }
        };

        final List<FieldAccessor> fieldAccessors = ClassUtils.getAllFields(TestDto.class, MethodHandles.lookup()).stream()
                .map(field -> (FieldAccessor) new DirectFieldAccessor(field, MethodHandles.lookup()))
                .toList();
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final TrackedDto<TestDto> trackedDto = new TrackedDto<>(testDto, fieldAccessors, classFieldAccessorCache, consumer);
        trackedDto.snapshot(false);

        // When 1
        testDto.string = "value2";
        final NestedDto nestedDtoKeyMapKey = new NestedDto();
        testDto.nestedDtoKeyMap = new HashMap<>();
        testDto.nestedDtoKeyMap.put(nestedDtoKeyMapKey, 1L);
        final ChangedFields changedFields = trackedDto.changedFields();
        assertTrue(nestedDtosRegistered[3]);

        // Then 1
        assertEquals(2, changedFields.size());
        assertTrue(changedFields.contains("string"));
        assertTrue(changedFields.contains("nestedDtoKeyMap"));

        // When 2
        trackedDto.snapshot(true);
        testDto.nestedDto = new NestedDto();
        final ChangedFields changedFields2 = trackedDto.changedFields();

        // Then 2
        assertEquals(1, changedFields2.size());
        assertTrue(changedFields2.contains("nestedDto"));
        assertTrue(nestedDtosRegistered[0]);
    }

    @Test
    void changedFields_nestedDtoBranch_skipsCallbackWhenFieldValueBecomesNull() {
        // Given
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final FieldAccessor fieldAccessor = classFieldAccessorCache.fieldAccessor(TestDto.class, "nestedDto");
        final boolean[] callbackInvoked = {false};

        final TestDto testDto = new TestDto();
        testDto.nestedDto = new NestedDto();

        final TrackedDto<TestDto> trackedDto = new TrackedDto<>(testDto, List.of(fieldAccessor), classFieldAccessorCache, dto -> {
            callbackInvoked[0] = true;
        });

        trackedDto.snapshot(true);
        assertTrue(callbackInvoked[0]);
        callbackInvoked[0] = false;
        testDto.nestedDto = null;

        // When
        final ChangedFields changedFields = trackedDto.changedFields();

        // Then
        assertEquals(1, changedFields.size());
        assertTrue(changedFields.contains("nestedDto"));
        assertNull(changedFields.get("nestedDto").orElseThrow().value());
        assertFalse(callbackInvoked[0]);
    }

    @Test
    void changedFields_isCachedUntilRefresh() {
        // Given
        final TestDto testDto = new TestDto();
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final TrackedDto<TestDto> trackedDto = new TrackedDto<>(testDto, fieldAccessors(), classFieldAccessorCache, dto -> fail());
        trackedDto.snapshot(false);
        testDto.string = "value1";

        // When
        final ChangedFields first = trackedDto.changedFields();
        testDto.string = "value2";
        final ChangedFields second = trackedDto.changedFields();

        // Then
        assertSame(first, second);
        assertEquals(1, second.size());
        assertTrue(second.contains("string"));
    }

    @Test
    void changedFields_refresh_recomputesResult() {
        // Given
        final TestDto testDto = new TestDto();
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final TrackedDto<TestDto> trackedDto = new TrackedDto<>(testDto, fieldAccessors(), classFieldAccessorCache, dto -> fail());
        trackedDto.snapshot(false);

        testDto.string = "value1";
        final ChangedFields cached = trackedDto.changedFields();

        // When
        testDto.map = new HashMap<>();
        testDto.map.put("key1", 1L);
        final ChangedFields refreshed = trackedDto.changedFields(true);

        // Then
        assertNotSame(cached, refreshed);
        assertEquals(2, refreshed.size());
        assertTrue(refreshed.contains("string"));
        assertTrue(refreshed.contains("map"));
    }

    @Test
    void changedFields_noChanges() {
        // Given
        final TestDto testDto = new TestDto();

        final Consumer<Object> consumer = dto -> {
            // do nothing
        };

        final List<FieldAccessor> fieldAccessors = ClassUtils.getAllFields(TestDto.class, MethodHandles.lookup()).stream()
                .map(field -> (FieldAccessor) new DirectFieldAccessor(field, MethodHandles.lookup()))
                .toList();
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final TrackedDto<TestDto> trackedDto = new TrackedDto<>(testDto, fieldAccessors, classFieldAccessorCache, consumer);
        trackedDto.snapshot(false);

        // When
        final ChangedFields changedFields = trackedDto.changedFields();

        // Then
        assertTrue(changedFields.isEmpty());
    }

    @Test
    void changedFields_nestedDtoToNull() {
        // Given
        final TestDto testDto = new TestDto();
        testDto.nestedDto = new NestedDto();

        final Consumer<Object> consumer = dto -> {
            // do nothing
        };

        final List<FieldAccessor> fieldAccessors = ClassUtils.getAllFields(TestDto.class, MethodHandles.lookup()).stream()
                .map(field -> (FieldAccessor) new DirectFieldAccessor(field, MethodHandles.lookup()))
                .toList();
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final TrackedDto<TestDto> trackedDto = new TrackedDto<>(testDto, fieldAccessors, classFieldAccessorCache, consumer);
        trackedDto.snapshot(false);

        // When
        testDto.nestedDto = null;
        final ChangedFields changedFields = trackedDto.changedFields();

        // Then
        assertFalse(changedFields.isEmpty());
        assertEquals(1, changedFields.size());
        assertTrue(changedFields.contains("nestedDto"));
        assertNull(changedFields.get("nestedDto").orElseThrow().value());
    }

    @Test
    void changedFields_noSnapshots() {
        // Given
        final TestDto testDto = new TestDto();
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final TrackedDto<TestDto> trackedDto = new TrackedDto<>(testDto, classFieldAccessorCache, dto -> fail());
        final List<FieldAccessor> fieldAccessors = ClassUtils.getAllFields(TestDto.class, MethodHandles.lookup()).stream()
                .map(field -> (FieldAccessor) new DirectFieldAccessor(field, MethodHandles.lookup()))
                .toList();

        // When/Then
        assertThrows(IllegalStateException.class, () -> trackedDto.changedFields());
    }

    @Test
    void changedFields_refresh_noSnapshots() {
        // Given
        final TestDto testDto = new TestDto();
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final TrackedDto<TestDto> trackedDto = new TrackedDto<>(testDto, classFieldAccessorCache, dto -> fail());
        final List<FieldAccessor> fieldAccessors = ClassUtils.getAllFields(TestDto.class, MethodHandles.lookup()).stream()
                .map(field -> (FieldAccessor) new DirectFieldAccessor(field, MethodHandles.lookup()))
                .toList();

        // When/Then
        assertThrows(IllegalStateException.class, () -> trackedDto.changedFields(true));
    }

    private static List<FieldAccessor> fieldAccessors() {
        return ClassUtils.getAllFields(TestDto.class, MethodHandles.lookup()).stream()
                .map(field -> (FieldAccessor) new DirectFieldAccessor(field, MethodHandles.lookup()))
                .toList();
    }

    @NullUnmarked
    private static class TestDto {
        private String string;
        private NestedDto nestedDto;
        private NestedDto nullNestedDto = null;
        private Map<String, Long> map;
        private Map<?, ?> emptyMap = Collections.emptyMap();
        private Map<String, NestedDto> nestedDtoMap;
        private Map<NestedDto, Long> nestedDtoKeyMap;
        private List<String> list;
        private List<?> emptyList = Collections.emptyList();
        private List<NestedDto> nestedDtoList;
    }

    @NullUnmarked
    private static class NestedDto {
        private String string;
    }


    private class OneToManyParentDto {
        private Long id;
        private List<ManyToOneChildDto> children;
    }

    private class ManyToOneChildDto {
        private Long id;
        private String name;
        private OneToManyParentDto parent;
    }
}
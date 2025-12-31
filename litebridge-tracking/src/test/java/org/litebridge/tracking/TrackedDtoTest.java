package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class TrackedDtoTest {

    @Test
    void getDto() {
        // Given
        final TestDto testDto = new TestDto();
        final TrackedDto<TestDto> trackedDto = new TrackedDto(testDto, dto -> fail());

        // When
        final TestDto result = trackedDto.getDto();

        // Then
        assertEquals(testDto, result);
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

        final List<FieldAccessor> fieldAccessors = ClassUtils.getAllFields(TestDto.class).stream()
                .map(field -> (FieldAccessor) new FieldAccessorImpl(field))
                .toList();
        final TrackedDto<TestDto> trackedDto = new TrackedDto(testDto, fieldAccessors, consumer);

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
    void snapshotEmpty_twice_overwriteFalse() {
        // Given
        final TestDto testDto = new TestDto();
        testDto.string = "value1";

        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "string"));
        final TrackedDto<TestDto> trackedDto = new TrackedDto(testDto, List.of(fieldAccessor), dto -> fail());


        // When/Then
        trackedDto.snapshot(false);
        assertThrows(IllegalStateException.class, () -> trackedDto.snapshot(false));
    }

    @Test
    void snapshotEmpty() {
        // Given
        final TestDto testDto = new TestDto();
        testDto.string = "value1";

        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "string"));
        final TrackedDto<TestDto> trackedDto = new TrackedDto(testDto, List.of(fieldAccessor), dto -> fail());

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

        final TrackedDto<TestDto> trackedDto = new TrackedDto(testDto, dto -> fail());
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "string"));

        // When/Then
        trackedDto.snapshotEmpty();
        assertThrows(IllegalStateException.class, trackedDto::snapshotEmpty);
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

        final List<FieldAccessor> fieldAccessors = ClassUtils.getAllFields(TestDto.class).stream()
                .map(field -> (FieldAccessor) new FieldAccessorImpl(field))
                .toList();
        final TrackedDto<TestDto> trackedDto = new TrackedDto(testDto, fieldAccessors, consumer);
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
        testDto.nestedDto = new NestedDto();
        final ChangedFields changedFields2 = trackedDto.changedFields(true);

        // Then 2
        assertEquals(1, changedFields2.size());
        assertTrue(changedFields2.contains("nestedDto"));
        assertTrue(nestedDtosRegistered[0]);
    }

    @Test
    void changedFields_noSnapshots() {
        // Given
        final TestDto testDto = new TestDto();
        final TrackedDto<TestDto> trackedDto = new TrackedDto(testDto, dto -> fail());
        final List<FieldAccessor> fieldAccessors = ClassUtils.getAllFields(TestDto.class).stream()
                .map(field -> (FieldAccessor) new FieldAccessorImpl(field))
                .toList();

        // When
        assertThrows(IllegalStateException.class, () -> trackedDto.changedFields());
    }

    @Nullable
    private class TestDto {
        private String string;
        private NestedDto nestedDto;
        private NestedDto nullNestedDto = null;
        private Map<String, Long> map;
        private Map<?, ?> emptyMap = Collections.emptyMap();
        @Nullable
        private Map<String, NestedDto> nestedDtoMap;
        private Map<NestedDto, Long> nestedDtoKeyMap;
        private List<String> list;
        private List<?> emptyList = Collections.emptyList();
        @Nullable
        private List<NestedDto> nestedDtoList;
    }

    @Nullable
    private class NestedDto {
        private String string;
    }
}
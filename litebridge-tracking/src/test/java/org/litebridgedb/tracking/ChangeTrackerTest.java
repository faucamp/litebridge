package org.litebridgedb.tracking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.shadow.de.siegmar.fastcsv.util.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChangeTrackerTest {

    private ChangeTracker changeTracker;

    @BeforeEach
    public void beforeEach() {
        changeTracker = new ChangeTracker(MethodHandles.lookup());
    }

    @Test
    public void trackDto() {
        // Given
        final TestDto dto = new TestDto();
        final Set<String> trackedFields = Set.of("field1", "field2");

        // When
        final TestDto result = changeTracker.trackDto(dto, trackedFields);
        dto.setField1("TestValue");
        dto.setField2(42);

        // Then
        assertEquals(dto, result);

        final TrackedDto<TestDto> trackedDto = changeTracker.getTrackedDto(dto);
        assertNotNull(trackedDto);

        final ChangedFields changedFields = trackedDto.changedFields();
        assertNotNull(changedFields);
        assertEquals(2, changedFields.size());
        changedFields.get("field1")
                .ifPresentOrElse(
                        changedField -> assertEquals("TestValue", changedField.value()),
                        Assertions::fail);
        changedFields.get("field2")
                .ifPresentOrElse(
                        changedField -> assertEquals(42, changedField.value()),
                        Assertions::fail);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void trackDto_trackNestedDto_null() throws Exception {
        // Given
        final Method trackNestedDto = ChangeTracker.class.getDeclaredMethod("trackNestedDto", Object.class);
        trackNestedDto.setAccessible(true);
        final TestDto dto = null;

        // When
        final InvocationTargetException result = assertThrows(InvocationTargetException.class, () -> trackNestedDto.invoke(changeTracker, dto));

        // Then
        assertEquals(NullPointerException.class, result.getCause().getClass());
        assertEquals("Nested DTO is null", result.getCause().getMessage());
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void trackDto_null() {
        // Given
        final Object dto = null;
        final Set<String> trackedFields = Set.of("field1", "field2");

        // When/Then
        assertThrows(NullPointerException.class, () -> changeTracker.trackDto(dto, trackedFields));
    }

    @Test
    public void trackDto_invalidField() {
        // Given
        final TestDto dto = new TestDto();
        final Set<String> trackedFields = Set.of("nonExistentField");

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> changeTracker.trackDto(dto, trackedFields));
    }

    @Test
    public void trackDto_fieldType_invalidField() throws Exception {
        // Given
        final TestDto dto = new TestDto();
        final Set<FieldAccessor> invalidFields = Set.of(new DirectFieldAccessor(ContainerDto.class.getDeclaredField("parentField1"), MethodHandles.lookup()));

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> changeTracker.trackDtoFields(dto, invalidFields));
    }

    @Test
    public void trackDto_emptyTrackedFields() {
        // Given
        final TestDto dto = new TestDto();
        final Set<String> trackedFields = Collections.emptySet();

        // When
        final TestDto result = changeTracker.trackDto(dto, trackedFields);

        // Then
        assertEquals(dto, result);

        final TrackedDto<TestDto> trackedDto = changeTracker.getTrackedDto(dto);
        assertNotNull(trackedDto);
        assertTrue(trackedDto.changedFields().isEmpty());
    }

    @Test
    public void trackDto_alreadyTracked() {
        // Given
        final Set<String> trackedFields = Set.of("field1", "field2");
        final TestDto dto = changeTracker.trackDto(new TestDto(), trackedFields);
        dto.setField1("InitialValue");
        dto.setField2(100);

        // When
        final TestDto result = changeTracker.trackDto(dto, trackedFields);

        // Then
        assertEquals(dto, result);
    }

    @Test
    public void trackDtoFields() {
        // Given
        final TestDto dto = new TestDto();
        final Field field1 = TestDto.getDeclaredField("field1");
        final Field field2 = TestDto.getDeclaredField("field2");
        final Set<FieldAccessor> trackedFields = Set.of(new DirectFieldAccessor(field1, MethodHandles.lookup()), new DirectFieldAccessor(field2, MethodHandles.lookup()));

        // When
        final TestDto result = changeTracker.trackDtoFields(dto, trackedFields);
        dto.setField1("TestValue");
        dto.setField2(42);

        // Then
        assertEquals(dto, result);

        final TrackedDto<TestDto> trackedDto = changeTracker.getTrackedDto(dto);
        assertNotNull(trackedDto);

        final ChangedFields changedFields = trackedDto.changedFields();
        assertNotNull(changedFields);
        assertEquals(2, changedFields.size());
        changedFields.get("field1")
                .ifPresentOrElse(
                        changedField -> assertEquals("TestValue", changedField.value()),
                        Assertions::fail);
        changedFields.get("field2")
                .ifPresentOrElse(
                        changedField -> assertEquals(42, changedField.value()),
                        Assertions::fail);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void trackDtoFields_null() {
        // Given
        final Object dto = null;
        final Field field1 = TestDto.getDeclaredField("field1");
        final Field field2 = TestDto.getDeclaredField("field2");
        final Set<FieldAccessor> trackedFields = Set.of(new DirectFieldAccessor(field1, MethodHandles.lookup()), new DirectFieldAccessor(field2, MethodHandles.lookup()));

        // When/Then
        assertThrows(NullPointerException.class, () -> changeTracker.trackDtoFields(dto, trackedFields));
    }

    /**
     * Tests tracking changes to a complex DTO with nested child DTOs.
     * In this case, the child DTO is already present when the parent DTO is registered for tracking.
     */
    @Test
    public void trackDto_nested_nestedDtoAlreadyPresent() {
        // Given
        final ContainerDto dto = new ContainerDto();
        dto.setParentField1(1L);
        final TestDto nestedDto = new TestDto();
        nestedDto.setField1("NestedDtoField1");
        dto.setNestedDto(nestedDto);

        // When
        final ContainerDto result = changeTracker.trackDto(dto);

        // Then
        assertEquals(dto, result);
        nestedDto.setField1("NestedDtoField1_Changed");

        final TrackedDto<ContainerDto> trackedDto = changeTracker.getTrackedDto(dto);
        assertNotNull(trackedDto);

        final ChangedFields changedFields = trackedDto.changedFields();
        assertNotNull(changedFields);
        assertEquals(1, changedFields.size());
        assertTrue(changedFields.contains("nestedDto"));
        assertEquals(nestedDto, changedFields.get("nestedDto").orElseThrow().value());

        final TrackedDto<TestDto> nestedTrackedDto = changeTracker.getTrackedDto(nestedDto);
        final ChangedFields nestedChangedFields = nestedTrackedDto.changedFields();
        assertNotNull(nestedChangedFields);
        assertEquals(1, nestedChangedFields.size());
    }

    /**
     * Tests tracking changes to a complex DTO with nested child DTOs.
     * In this case, the child DTO is not present when the parent DTO is registered for tracking.
     */
    @Test
    public void trackDto_nested_nestedDtoNotPresent() {
        // Given
        final ContainerDto dto = new ContainerDto();
        dto.setParentField1(1L);

        // When
        final ContainerDto result = changeTracker.trackDto(dto);
        final TestDto nestedDto = new TestDto();
        nestedDto.setField1("NestedDtoField1");
        dto.setNestedDto(nestedDto);

        // Then
        assertEquals(dto, result);
        nestedDto.setField1("NestedDtoField1_Changed");

        final TrackedDto<ContainerDto> trackedDto = changeTracker.getTrackedDto(dto);
        assertNotNull(trackedDto);

        final ChangedFields changedFields = trackedDto.changedFields();
        assertNotNull(changedFields);
        assertEquals(1, changedFields.size());
        assertTrue(changedFields.contains("nestedDto"));
        assertEquals(nestedDto, changedFields.get("nestedDto").orElseThrow().value());

        final TrackedDto<TestDto> nestedTrackedDto = changeTracker.getTrackedDto(nestedDto);
        final ChangedFields nestedChangedFields = nestedTrackedDto.changedFields();
        assertNotNull(nestedChangedFields);
        assertEquals(1, nestedChangedFields.size());
    }

    @Test
    public void trackDto_basicList() {
        // Given
        final ContainerDto dto = new ContainerDto();
        dto.setBasicList(new ArrayList<>());
        dto.getBasicList().add("TestValue1");

        // When
        final ContainerDto result = changeTracker.trackDto(dto);
        dto.getBasicList().add("TestValue2");

        // Then
        assertEquals(dto, result);

        final TrackedDto<ContainerDto> trackedDto = changeTracker.getTrackedDto(dto);
        assertNotNull(trackedDto);

        final ChangedFields changedFields = trackedDto.changedFields();
        assertNotNull(changedFields);
        assertEquals(1, changedFields.size());
        assertTrue(changedFields.contains("basicList"));
        assertEquals(dto.getBasicList(), changedFields.get("basicList").orElseThrow().value());
    }

    @Test
    public void trackDto_nestedDtoList() {
        // Given
        final ContainerDto dto = new ContainerDto();
        dto.setNestedDtoList(new ArrayList<>());

        final TestDto nestedDto1 = new TestDto();
        nestedDto1.setField1("String1");
        nestedDto1.setField2(1);
        dto.getNestedDtoList().add(nestedDto1);

        final TestDto nestedDto2 = new TestDto();
        nestedDto2.setField1("String2");
        nestedDto2.setField2(2);

        // When
        final ContainerDto result = changeTracker.trackDto(dto);
        dto.getNestedDtoList().add(nestedDto2);

        // Then
        assertEquals(dto, result);

        final TrackedDto<ContainerDto> trackedDto = changeTracker.getTrackedDto(dto);
        assertNotNull(trackedDto);

        final ChangedFields changedFields = trackedDto.changedFields();
        assertNotNull(changedFields);
        assertEquals(1, changedFields.size());
        assertTrue(changedFields.contains("nestedDtoList"));
        assertEquals(dto.getNestedDtoList(), changedFields.get("nestedDtoList").orElseThrow().value());
    }

    @Test
    public void trackDto_nestedDtoMap() {
        // Given
        final ContainerDto dto = new ContainerDto();
        dto.setStringMap(new HashMap<>());
        dto.getStringMap().put("Key1", "Value1");

        // When
        final ContainerDto result = changeTracker.trackDto(dto);
        dto.getStringMap().put("Key2", "Value2");

        // Then
        assertEquals(dto, result);

        final TrackedDto<ContainerDto> trackedDto = changeTracker.getTrackedDto(dto);
        assertNotNull(trackedDto);

        final ChangedFields changedFields = trackedDto.changedFields();
        assertNotNull(changedFields);
        assertEquals(1, changedFields.size());
        final ChangedMapField changedMapField = changedFields.get("stringMap").orElseThrow()
                .cast(ChangedMapField.class).orElseThrow();
        assertEquals(dto.getStringMap(), changedMapField.value());

        assertNotNull(changedMapField.mapSnapshot());
        assertEquals(1, changedMapField.mapSnapshot().size());
        assertTrue(changedMapField.mapSnapshot().containsKey("Key1"));
    }

    // Helper DTO class for testing
    static class TestDto {
        @Nullable
        private String field1;
        @Nullable
        private Integer field2;

        public String getField1() {
            return field1;
        }

        public void setField1(String field1) {
            this.field1 = field1;
        }

        public Integer getField2() {
            return field2;
        }

        public void setField2(Integer field2) {
            this.field2 = field2;
        }

        // Helper method to simplify field lookup in tests
        public static Field getDeclaredField(String fieldName) {
            try {
                return TestDto.class.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class ContainerDto {

        private long parentField1;
        @Nullable
        private TestDto nestedDto;
        @Nullable
        private List<String> basicList;
        @Nullable
        private List<TestDto> nestedDtoList;
        @Nullable
        private Map<String, String> stringMap;

        public long getParentField1() {
            return parentField1;
        }

        public void setParentField1(final long parentField1) {
            this.parentField1 = parentField1;
        }

        public TestDto getNestedDto() {
            return nestedDto;
        }

        public void setNestedDto(final TestDto nestedDto) {
            this.nestedDto = nestedDto;
        }

        public List<String> getBasicList() {
            return basicList;
        }

        public void setBasicList(final List<String> basicList) {
            this.basicList = basicList;
        }

        public List<TestDto> getNestedDtoList() {
            return nestedDtoList;
        }

        public void setNestedDtoList(final List<TestDto> nestedDtoList) {
            this.nestedDtoList = nestedDtoList;
        }

        public Map<String, String> getStringMap() {
            return stringMap;
        }

        public void setStringMap(final Map<String, String> stringMap) {
            this.stringMap = stringMap;
        }
    }
}
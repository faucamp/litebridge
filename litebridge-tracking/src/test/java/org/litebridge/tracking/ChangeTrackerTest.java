package org.litebridge.tracking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ChangeTrackerTest {

    private ChangeTracker changeTracker;

    @BeforeEach
    public void beforeEach() {
        changeTracker = new ChangeTracker();
    }

    @Test
    public void trackDto() {
        // Given
        final TestDto dto = new TestDto();
        final Set<String> trackedFields = Set.of("field1", "field2");

        // When
        final TestDto result = changeTracker.trackDto(dto, trackedFields);
        dto.setField1("TestName");
        dto.setField2(42);

        // Then
        assertEquals(dto, result);

        final TrackedDto<TestDto> trackedDto = changeTracker.getTrackedDto(dto);
        assertNotNull(trackedDto);

        final Map<String, ChangedField> changedFields = trackedDto.getChangedFields();
        assertNotNull(changedFields);
        assertEquals(2, changedFields.size());

        changedFields.forEach((fieldName, changedField) -> {
            if ("field1".equals(fieldName)) {
                assertEquals("TestName", changedField.value());
            } else if ("field2".equals(fieldName)) {
                assertEquals(42, changedField.value());
            } else {
                fail("Unexpected field name: " + fieldName);
            }
        });
    }

    @Test
    public void trackDto_null() {
        // Given
        final Object dto = null;
        final Set<String> trackedFields = Set.of("field1", "field2");

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> changeTracker.trackDto(dto, trackedFields));
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
    public void trackDto_emptyTrackedFields() {
        // Given
        final TestDto dto = new TestDto();
        final Set<String> trackedFields = Collections.emptySet();

        // When
        final TestDto result = changeTracker.trackDto(dto, trackedFields);

        // Then
        assertEquals(dto, result);

        final TrackedDto trackedDto = changeTracker.getTrackedDto(dto);
        assertNotNull(trackedDto);
        assertTrue(trackedDto.getChangedFields().isEmpty());
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
        final Set<Field> trackedFields = Set.of(field1, field2);

        // When
        final TestDto result = changeTracker.trackDtoFields(dto, trackedFields);
        dto.setField1("TestName");
        dto.setField2(42);

        // Then
        assertEquals(dto, result);

        final TrackedDto<TestDto> trackedDto = changeTracker.getTrackedDto(dto);
        assertNotNull(trackedDto);

        final Map<String, ChangedField> changedFields = trackedDto.getChangedFields();
        assertNotNull(changedFields);
        assertEquals(2, changedFields.size());

        changedFields.forEach((fieldName, changedField) -> {
            if ("field1".equals(fieldName)) {
                assertEquals("TestName", changedField.value());
            } else if ("field2".equals(fieldName)) {
                assertEquals(42, changedField.value());
            } else {
                fail("Unexpected field name: " + fieldName);
            }
        });
    }

    @Test
    public void trackDtoFields_null() {
        // Given
        final Object dto = null;
        final Field field1 = TestDto.getDeclaredField("field1");
        final Field field2 = TestDto.getDeclaredField("field2");
        final Set<Field> trackedFields = Set.of(field1, field2);

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> changeTracker.trackDtoFields(dto, trackedFields));
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

        final Map<String, ChangedField> changedFields = trackedDto.getChangedFields();
        assertNotNull(changedFields);
        assertEquals(1, changedFields.size());
        assertTrue(changedFields.containsKey("nestedDto"));
        assertEquals(nestedDto, changedFields.get("nestedDto").value());

        final TrackedDto<TestDto> nestedTrackedDto = changeTracker.getTrackedDto(nestedDto);
        final Map<String, ChangedField> nestedChangedFields = nestedTrackedDto.getChangedFields();
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

        final Map<String, ChangedField> changedFields = trackedDto.getChangedFields();
        assertNotNull(changedFields);
        assertEquals(1, changedFields.size());
        assertTrue(changedFields.containsKey("nestedDto"));
        assertEquals(nestedDto, changedFields.get("nestedDto").value());

        final TrackedDto<TestDto> nestedTrackedDto = changeTracker.getTrackedDto(nestedDto);
        final Map<String, ChangedField> nestedChangedFields = nestedTrackedDto.getChangedFields();
        assertNotNull(nestedChangedFields);
        assertEquals(1, nestedChangedFields.size());
    }

    // Helper DTO class for testing
    static class TestDto {
        private String field1;
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
        private TestDto nestedDto;

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
    }
}
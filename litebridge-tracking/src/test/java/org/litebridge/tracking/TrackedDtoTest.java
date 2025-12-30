package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;

import java.util.List;

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
        testDto.setString("value1");

        final TrackedDto<TestDto> trackedDto = new TrackedDto(testDto, dto -> fail());
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "string"));

        // When 1
        trackedDto.snapshot(List.of(fieldAccessor), true);
        // Then 1
        assertTrue(trackedDto.getChangedFields().isEmpty());

        // When 2
        trackedDto.snapshot(List.of(fieldAccessor), true);
        testDto.setString("value2");
        // Then 2
        assertEquals(1, trackedDto.getChangedFields().size());
        assertTrue(trackedDto.getChangedFields().contains("string"));
    }

    @Test
    void snapshotEmpty_twice_overwriteFalse() {
        // Given
        final TestDto testDto = new TestDto();
        testDto.setString("value1");

        final TrackedDto<TestDto> trackedDto = new TrackedDto(testDto, dto -> fail());
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "string"));

        // When/Then
        trackedDto.snapshot(List.of(fieldAccessor), false);
        assertThrows(IllegalStateException.class, () -> trackedDto.snapshot(List.of(fieldAccessor), false));
    }

    @Test
    void snapshotEmpty() {
        // Given
        final TestDto testDto = new TestDto();
        testDto.setString("value1");

        final TrackedDto<TestDto> trackedDto = new TrackedDto(testDto, dto -> fail());
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "string"));

        // When
        trackedDto.snapshotEmpty(List.of(fieldAccessor));

        // Then
        assertEquals(1, trackedDto.getChangedFields().size());
        assertTrue(trackedDto.getChangedFields().contains("string"));
    }

    @Test
    void snapshotEmpty_twice() {
        // Given
        final TestDto testDto = new TestDto();
        testDto.setString("updatedValue");

        final TrackedDto<TestDto> trackedDto = new TrackedDto(testDto, dto -> fail());
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "string"));

        // When/Then
        trackedDto.snapshotEmpty(List.of(fieldAccessor));
        assertThrows(IllegalStateException.class, () -> trackedDto.snapshotEmpty(List.of(fieldAccessor)));
    }

    @Nullable
    private class TestDto {
        private String string;
        private NestedDto nestedDto;

        public String getString() {
            return string;
        }

        public void setString(final String string) {
            this.string = string;
        }
    }

    @Nullable
    private class NestedDto {
        private String string;
    }
}
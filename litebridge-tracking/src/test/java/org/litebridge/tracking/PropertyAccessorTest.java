package org.litebridge.tracking;

import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropertyAccessorTest {

    @Test
    void name() {
        // When
        final PropertyAccessor propertyAccessor = new PropertyAccessor(ClassUtils.getProperty(TestDto.class, "myVar"));
        final String result = propertyAccessor.name();

        // Then
        assertEquals("myVar", result);
    }

    @Test
    void get() {
        // Given
        final PropertyAccessor propertyAccessor = new PropertyAccessor(ClassUtils.getProperty(TestDto.class, "myVar"));
        final TestDto testDto = new TestDto();
        testDto.setMyVar("testValue");

        // When
        final String result = (String) propertyAccessor.get(testDto);

        // Then
        assertEquals("testValue", result);
    }

    @Test
    void set() {
        // Given
        final PropertyAccessor propertyAccessor = new PropertyAccessor(ClassUtils.getProperty(TestDto.class, "myVar"));
        final TestDto testDto = new TestDto();

        // When
        propertyAccessor.set(testDto, "testValue2");

        // Then
        assertEquals("testValue2", testDto.getMyVar());
    }

    @Test
    void type() {
        // When
        final PropertyAccessor propertyAccessor = new PropertyAccessor(ClassUtils.getProperty(TestDto.class, "myVar"));
        final Class<?> result = propertyAccessor.type();

        // Then
        assertEquals(String.class, result);
    }

    @Test
    void genericTypes() throws Exception {
        // Given
        final PropertyAccessor propertyAccessor = new PropertyAccessor(ClassUtils.getProperty(TestDto2.class, "list"));

        // When
        final Class<?>[] result = propertyAccessor.genericTypes();

        // Then
        assertEquals(1, result.length);
        assertEquals(Long.class, result[0]);
    }

    private class TestDto {
        private String myVar;

        public String getMyVar() {
            return myVar;
        }

        public void setMyVar(final String myVar) {
            this.myVar = myVar;
        }
    }

    private class TestDto2 {
        private List<Long> list;

        public List<Long> getList() {
            return list;
        }

        public void setList(final List<Long> list) {
            this.list = list;
        }
    }
}
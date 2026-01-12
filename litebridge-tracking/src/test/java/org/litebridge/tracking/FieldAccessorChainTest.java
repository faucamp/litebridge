package org.litebridge.tracking;

import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class FieldAccessorChainTest {

    @Test
    void fieldPath() {
        // Given
        final FieldAccessor parentAccessor = mock(FieldAccessor.class);
        final String fieldPath = "parent.child";

        final FieldAccessorChain fieldAccessorChain = new FieldAccessorChain(parentAccessor, fieldPath);

        // When
        final String result = fieldAccessorChain.fieldPath();

        // Assert
        assertEquals(fieldPath, result);
    }

    @Test
    void fieldPath_singleLevelPath() {
        // Given
        final FieldAccessor parentAccessor = mock(FieldAccessor.class);
        final String fieldPath = "field";

        final FieldAccessorChain fieldAccessorChain = new FieldAccessorChain(parentAccessor, fieldPath);

        // When
        final String result = fieldAccessorChain.fieldPath();

        // Then
        assertEquals(fieldPath, result);
    }

    @Test
    void subChain() {
        // Given
        FieldAccessor parentAccessor = mock(FieldAccessor.class);
        FieldAccessor additionalAccessor = mock(FieldAccessor.class);
        when(parentAccessor.name()).thenReturn("parent");
        when(additionalAccessor.name()).thenReturn("child");

        String fieldPath = "parent.child";
        FieldAccessorChain fieldAccessorChain = new FieldAccessorChain(parentAccessor, fieldPath);
        fieldAccessorChain.add(additionalAccessor);

        // When
        FieldAccessorChain subChain = fieldAccessorChain.subChain();
        String result = subChain.fieldPath();

        // Then
        assertEquals("child", result, "Field path of the sub-chain should only include the last segment.");
    }

    @Test
    void fieldAccessors_containsParentAndAddedAccessors() {
        // Given
        FieldAccessor parent = mock(FieldAccessor.class);
        FieldAccessor child = mock(FieldAccessor.class);

        FieldAccessorChain chain = new FieldAccessorChain(parent, "parent.child");
        chain.add(child);

        // When
        List<FieldAccessor> accessors = chain.fieldAccessors();

        // Then
        assertEquals(2, accessors.size());
        assertSame(parent, accessors.get(0));
        assertSame(child, accessors.get(1));
    }

    @Test
    void add_whenAddingChain_flattensAccessors() {
        // Given
        FieldAccessor a = mock(FieldAccessor.class);
        FieldAccessor b = mock(FieldAccessor.class);
        FieldAccessor c = mock(FieldAccessor.class);

        FieldAccessorChain chain1 = new FieldAccessorChain(a, "a.b");
        chain1.add(b);

        FieldAccessorChain chain2 = new FieldAccessorChain(c, "c");

        // When
        chain2.add(chain1);

        // Then
        List<FieldAccessor> accessors = chain2.fieldAccessors();
        assertEquals(3, accessors.size());
        assertSame(c, accessors.get(0));
        assertSame(a, accessors.get(1));
        assertSame(b, accessors.get(2));
    }

    @Test
    void name_delegatesToLastAccessor() {
        // Given
        FieldAccessor parent = mock(FieldAccessor.class);
        FieldAccessor last = mock(FieldAccessor.class);
        when(last.name()).thenReturn("child");

        FieldAccessorChain chain = new FieldAccessorChain(parent, "parent.child");
        chain.add(last);

        // When
        String name = chain.name();

        // Then
        assertEquals("child", name);
        verify(last).name();
        verifyNoMoreInteractions(last);
    }

    @Test
    void get_traversesAccessorsInOrder() {
        // Given
        Object dto = new Object();
        Object intermediate = new Object();
        Object finalValue = new Object();

        FieldAccessor parent = mock(FieldAccessor.class);
        FieldAccessor child = mock(FieldAccessor.class);

        when(parent.get(dto)).thenReturn(intermediate);
        when(child.get(intermediate)).thenReturn(finalValue);

        FieldAccessorChain chain = new FieldAccessorChain(parent, "parent.child");
        chain.add(child);

        // When
        Object result = chain.get(dto);

        // Then
        assertSame(finalValue, result);
        verify(parent).get(dto);
        verify(child).get(intermediate);
    }

    @Test
    void get_returnsNullAndStopsTraversalWhenIntermediateIsNull() {
        // Given
        Object dto = new Object();

        FieldAccessor parent = mock(FieldAccessor.class);
        FieldAccessor child = mock(FieldAccessor.class);

        when(parent.get(dto)).thenReturn(null);

        FieldAccessorChain chain = new FieldAccessorChain(parent, "parent.child");
        chain.add(child);

        // When
        Object result = chain.get(dto);

        // Then
        assertNull(result);
        verify(parent).get(dto);
        verifyNoInteractions(child);
    }

    @Test
    void set_delegatesToLastAccessor() {
        // Given
        Object dto = new Object();
        Object value = new Object();

        FieldAccessor parent = mock(FieldAccessor.class);
        FieldAccessor last = mock(FieldAccessor.class);

        FieldAccessorChain chain = new FieldAccessorChain(parent, "parent.child");
        chain.add(last);

        // When
        chain.set(dto, value);

        // Then
        verify(last).set(dto, value);
        verifyNoMoreInteractions(last);
        verifyNoInteractions(parent);
    }

    @Test
    void type_delegatesToLastAccessor() {
        // Given
        FieldAccessor parent = mock(FieldAccessor.class);
        FieldAccessor last = mock(FieldAccessor.class);
        doReturn(String.class).when(last).type();

        FieldAccessorChain chain = new FieldAccessorChain(parent, "parent.child");
        chain.add(last);

        // When / Then
        assertSame(String.class, chain.type());
        verify(last).type();
        verifyNoMoreInteractions(last);
    }

    @Test
    void genericTypes_delegatesToLastAccessor() {
        // Given
        FieldAccessor parent = mock(FieldAccessor.class);
        FieldAccessor last = mock(FieldAccessor.class);
        Class<?>[] generics = new Class<?>[]{String.class, Integer.class};
        when(last.genericTypes()).thenReturn(generics);

        FieldAccessorChain chain = new FieldAccessorChain(parent, "parent.child");
        chain.add(last);

        // When
        Class<?>[] result = chain.genericTypes();

        // Then
        assertSame(generics, result);
        verify(last).genericTypes();
        verifyNoMoreInteractions(last);
    }

    @Test
    void dtoClass_delegatesToLastAccessor() {
        // Given
        final FieldAccessor parent = mock(FieldAccessor.class);
        final Field field = ClassUtils.getField(TestDto.class, "myVar");
        final FieldAccessor last = new FieldAccessorImpl(field);

        final FieldAccessorChain chain = new FieldAccessorChain(parent, "parent.child");
        chain.add(last);

        // When
        final Class<?> result = chain.dtoClass();

        // Then
        assertEquals(TestDto.class, result);
    }

    @Test
    void equals_delegatesToLastAccessorEquals() {
        // Given
        final FieldAccessor parent = mock(FieldAccessor.class);
        final Field field = ClassUtils.getField(TestDto.class, "myVar");
        final FieldAccessor last = new FieldAccessorImpl(field);

        final FieldAccessorChain chain = new FieldAccessorChain(parent, "parent.child");
        chain.add(last);

        // When
        final boolean result = chain.equals(last);

        // Then
        assertTrue(result);
    }

    @Test
    void hashCode_delegatesToLastAccessorHashCode() {
        // Given
        final FieldAccessor parent = mock(FieldAccessor.class);
        final Field field = ClassUtils.getField(TestDto.class, "myVar");
        final FieldAccessor last = new FieldAccessorImpl(field);
        final int expectedHashCode = last.hashCode();

        final FieldAccessorChain chain = new FieldAccessorChain(parent, "parent.child");
        chain.add(last);

        // When
        final int result = chain.hashCode();

        // Then
        assertEquals(expectedHashCode, result);
    }

    private class TestDto {
        private String myVar;
    }
}
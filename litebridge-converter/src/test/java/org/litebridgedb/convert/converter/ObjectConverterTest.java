package org.litebridgedb.convert.converter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObjectConverterTest {

    private final ObjectConverter converter = new ObjectConverter();

    @Test
    void type() {
        assertEquals(Object.class, converter.type());
    }

    @Test
    void convert() {
        // Given
        final String input = "test";

        // When
        final Object result = converter.convert(input);

        // Then
        assertSame(input, result);
    }
}
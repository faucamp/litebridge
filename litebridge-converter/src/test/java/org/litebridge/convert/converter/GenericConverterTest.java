package org.litebridge.convert.converter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GenericConverterTest {

    @Test
    void type() {
        final GenericConverter<String> converter = new GenericConverter<>(String.class, value -> (String) value);
        assertEquals(String.class, converter.type());
    }

    @Test
    void convert() {
        final GenericConverter<String> converter = new GenericConverter<>(String.class, value -> value == null ? null : value.toString());
        assertEquals("123", converter.convert(123));
        assertNull(converter.convert(null));
    }
}

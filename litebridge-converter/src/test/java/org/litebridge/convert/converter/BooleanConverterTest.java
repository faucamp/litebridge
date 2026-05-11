package org.litebridge.convert.converter;

import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BooleanConverterTest {

    private final BooleanConverter converter = new BooleanConverter();

    @Test
    void convert_null() {
        assertNull(converter.convert(null));
    }

    @Test
    void convert_Boolean() {
        assertTrue(converter.convert(Boolean.TRUE));
        assertFalse(converter.convert(Boolean.FALSE));
    }

    @Test
    void convert_String() {
        assertTrue(converter.convert("true"));
        assertTrue(converter.convert("TRUE"));
        assertFalse(converter.convert("false"));
        assertFalse(converter.convert("abc"));
    }

    @Test
    void convert_BlankString() {
        assertNull(converter.convert(""));
        assertNull(converter.convert("  "));
    }

    @Test
    void type() {
        assertEquals(Boolean.class, converter.type());
    }

    @Test
    void sqlTypes() {
        assertArrayEquals(new int[]{Types.BIT, Types.BOOLEAN}, converter.sqlTypes());
    }
}

package org.litebridgedb.convert.converter;

import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GenericSqlConverterTest {

    @Test
    void sqlTypes_array() {
        final int[] sqlTypes = {Types.VARCHAR, Types.CHAR};
        final GenericSqlConverter<String> converter = new GenericSqlConverter<>(String.class, sqlTypes, value -> (String) value);
        assertArrayEquals(sqlTypes, converter.sqlTypes());
    }

    @Test
    void sqlTypes_single() {
        final GenericSqlConverter<String> converter = new GenericSqlConverter<>(String.class, Types.VARCHAR, value -> (String) value);
        assertArrayEquals(new int[]{Types.VARCHAR}, converter.sqlTypes());
    }

    @Test
    void inheritance() {
        final GenericSqlConverter<String> converter = new GenericSqlConverter<>(String.class, Types.VARCHAR, value -> value == null ? null : value.toString());
        assertEquals(String.class, converter.type());
        assertEquals("123", converter.convert(123));
    }
}

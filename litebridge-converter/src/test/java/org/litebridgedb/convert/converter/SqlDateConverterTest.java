package org.litebridgedb.convert.converter;

import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class SqlDateConverterTest {

    private final SqlDateConverter converter = new SqlDateConverter();

    @Test
    void convert_null() {
        assertNull(converter.convert(null));
    }

    @Test
    void convert_Date() {
        // Given
        final Date input = Date.valueOf("2023-01-01");

        // When
        final Date result = converter.convert(input);

        // Then
        assertSame(input, result);
    }

    @Test
    void convert_String() {
        // Given
        final String input = "2023-01-01";

        // When
        final Date result = converter.convert(input);

        // Then
        assertEquals(Date.valueOf("2023-01-01"), result);
    }

    @Test
    void convert_BlankString() {
        assertNull(converter.convert(""));
        assertNull(converter.convert("  "));
    }

    @Test
    void type() {
        assertEquals(Date.class, converter.type());
    }

    @Test
    void sqlTypes() {
        assertArrayEquals(new int[]{Types.DATE}, converter.sqlTypes());
    }
}

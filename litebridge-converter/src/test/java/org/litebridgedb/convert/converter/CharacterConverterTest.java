package org.litebridgedb.convert.converter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class CharacterConverterTest {

    private final CharacterConverter converter = new CharacterConverter();

    @Test
    void convert_null() {
        assertNull(converter.convert(null));
    }

    @Test
    void convert_Character() {
        // Given
        final Character input = 'A';

        // When
        final Character result = converter.convert(input);

        // Then
        assertSame(input, result);
    }

    @Test
    void convert_String() {
        assertEquals('A', converter.convert("A"));
        assertEquals('A', converter.convert("ABC"));
    }

    @Test
    void convert_BlankString() {
        assertNull(converter.convert(""));
        assertNull(converter.convert("  "));
    }

    @Test
    void type() {
        assertEquals(Character.class, converter.type());
    }

    @Test
    void primitiveType() {
        assertEquals(char.class, converter.primitiveType());
    }
}

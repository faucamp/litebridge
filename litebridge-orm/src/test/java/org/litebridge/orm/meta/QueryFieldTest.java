package org.litebridge.orm.meta;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.expression.intent.ConvertSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class QueryFieldTest {

    @Test
    void testConstructorAndAccessors() {
        // Given
        final QueryField field = new QueryField(TestDto.class, "name");

        // Then
        assertEquals(TestDto.class, field.dtoClass());
        assertEquals("name", field.field());
    }

    @Test
    void testConvert() {
        // Given
        final QueryField field = new QueryField(TestDto.class, "name");

        // When
        final ConvertSpec<Integer> convert = field.convert(Integer.class);

        // Then
        assertNotNull(convert);
        assertEquals(Integer.class, convert.returnType());
    }

    @Test
    void testToString() {
        // Given
        final QueryField field = new QueryField(TestDto.class, "name");

        // Then
        assertEquals("name", field.toString());
    }
}

package org.litebridge.orm.meta;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.expression.ProtoNestableTOExpr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StringQueryFieldTest {

    @Test
    void testConstructor() {
        // Given
        final StringQueryField field = new StringQueryField(TestDto.class, "name");

        // Then
        assertEquals(TestDto.class, field.dtoClass());
        assertEquals("name", field.field());
    }

    @Test
    void testUpper() {
        // Given
        final StringQueryField field = new StringQueryField(TestDto.class, "name");

        // When
        final ProtoNestableTOExpr<String> upper = field.upper();

        // Then
        assertNotNull(upper);
        assertEquals(String.class, upper.returnType());
    }

    @Test
    void testLower() {
        // Given
        final StringQueryField field = new StringQueryField(TestDto.class, "name");

        // When
        final ProtoNestableTOExpr<String> lower = field.lower();

        // Then
        assertNotNull(lower);
        assertEquals(String.class, lower.returnType());
    }
}

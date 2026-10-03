package org.litebridge.orm.meta;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class StringQueryFieldTest {

    @Test
    void constructor() {
        // Given
        final StringQueryField field = new StringQueryField(TestDto.class, "name");

        // Then
        assertEquals(TestDto.class, field.dtoClass());
        assertEquals("name", field.field());
        assertNull(field.pendingExpressionSpec());
    }

    @Test
    void upper() {
        // Given
        final StringQueryField field = new StringQueryField(TestDto.class, "name");

        // When
        final StringQueryField result = field.upper();

        // Then
        assertNotNull(result);
        assertNotNull(result.pendingExpressionSpec());
    }

    @Test
    void lower() {
        // Given
        final StringQueryField field = new StringQueryField(TestDto.class, "name");

        // When
        final StringQueryField result = field.lower();

        // Then
        assertNotNull(result);
        assertNotNull(result.pendingExpressionSpec());
    }
}

package org.litebridge.db.spi.query;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.expression.LiteralExpression;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ValuesTest {

    @Test
    void getters_and_toString() {
        // Given
        final LiteralExpression lit = mock(LiteralExpression.class);
        final List<LiteralExpression> list = List.of(lit);
        final Values values = new Values(list, "v_alias");

        // When & Then
        assertEquals(list, values.values());
        assertEquals("v_alias", values.name());
        assertTrue(values.isVirtual());
        assertEquals("Values[alias='v_alias', values=" + list + "]", values.toString());
    }

    @Test
    void equals_and_hashCode() {
        // Given
        final LiteralExpression lit1 = mock(LiteralExpression.class);
        final LiteralExpression lit2 = mock(LiteralExpression.class);
        final Values v1 = new Values(List.of(lit1), "a");
        final Values v2 = new Values(List.of(lit1), "a");
        final Values vDiffAlias = new Values(List.of(lit1), "b");
        final Values vDiffValues = new Values(List.of(lit2), "a");

        // When & Then
        assertEquals(v1, v1);
        assertEquals(v1, v2);
        assertEquals(v1.hashCode(), v2.hashCode());

        assertNotEquals(v1, vDiffAlias);
        assertNotEquals(v1, vDiffValues);
        assertNotEquals(v1, null);
        assertNotEquals(v1, "not values");
    }
}

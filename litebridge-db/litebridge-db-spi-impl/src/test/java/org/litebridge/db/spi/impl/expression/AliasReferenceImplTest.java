package org.litebridge.db.spi.impl.expression;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.query.Select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AliasReferenceImplTest {

    private final LabelGenerator labelGenerator = new LabelGenerator();

    @Test
    void getters_returnConfiguredValues() {
        // Given
        final AliasReferenceImpl aliasRef = new AliasReferenceImpl("myCol", "myTable", labelGenerator);

        // When / Then
        assertEquals("myCol", aliasRef.alias());
        assertEquals("myTable", aliasRef.tableAlias());
    }

    @Test
    void toSql_withTableAlias() {
        // Given
        final AliasReferenceImpl aliasRef = new AliasReferenceImpl("myCol", "myTable", labelGenerator);
        final Select select = mock(Select.class);

        // When
        final String sql = aliasRef.toSql(select, ClauseType.SELECT, null);

        // Then
        assertEquals("\"myTable\".\"myCol\"", sql);
    }

    @Test
    void toSql_withoutTableAlias() {
        // Given
        final AliasReferenceImpl aliasRef = new AliasReferenceImpl("myCol", null, labelGenerator);
        final Select select = mock(Select.class);

        // When
        final String sql = aliasRef.toSql(select, ClauseType.SELECT, null);

        // Then
        assertEquals("\"myCol\"", sql);
    }

    @Test
    void toSql_withNullAlias_throwsIllegalStateException() {
        // Given
        final AliasReferenceImpl aliasRef = new AliasReferenceImpl(null, "myTable", labelGenerator);
        final Select select = mock(Select.class);

        // When / Then
        final IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> aliasRef.toSql(select, ClauseType.SELECT, null));
        assertEquals("Alias is null; cannot render SQL fragment", ex.getMessage());
    }

    @Test
    void equalsAndHashCode() {
        // Given
        final AliasReferenceImpl ref1 = new AliasReferenceImpl("a", "t1", labelGenerator);
        final AliasReferenceImpl ref2 = new AliasReferenceImpl("a", "t1", labelGenerator);
        final AliasReferenceImpl refDifferentAlias = new AliasReferenceImpl("b", "t1", labelGenerator);
        final AliasReferenceImpl refDifferentTable = new AliasReferenceImpl("a", "t2", labelGenerator);

        // Then
        assertEquals(ref1, ref1);
        assertEquals(ref1, ref2);
        assertNotEquals(ref1, refDifferentAlias);
        assertNotEquals(ref1, refDifferentTable);
        assertNotEquals(ref1, null);
        assertNotEquals(ref1, "someString");

        assertEquals(ref1.hashCode(), ref2.hashCode());
    }

    @Test
    void testToString() {
        // Given
        final AliasReferenceImpl ref = new AliasReferenceImpl("colAlias", "tableAlias", labelGenerator);

        // When
        final String s = ref.toString();

        // Then
        assertTrue(s.contains("AliasReferenceImpl"));
        assertTrue(s.contains("alias='colAlias'"));
        assertTrue(s.contains("tableAlias='tableAlias'"));
    }
}

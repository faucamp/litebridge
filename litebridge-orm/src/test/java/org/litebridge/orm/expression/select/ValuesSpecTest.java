package org.litebridge.orm.expression.select;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ValuesSpecTest {

    @Test
    void testValuesSpec() {
        // Given
        final ValuesSpec valuesSpec = new ValuesSpec("tableAlias", new String[]{"label"}, new Object[]{1});

        // Then
        assertEquals("tableAlias", valuesSpec.tableAlias());
        assertEquals("label", valuesSpec.labels()[0]);
        assertEquals(1, valuesSpec.values()[0]);
        assertNotEquals(0, valuesSpec.hashCode());
    }

    @Test
    void equals() {
        // Given
        final ValuesSpec valuesSpec = new ValuesSpec("tableAlias", new String[]{"label"}, new Object[]{1});
        final ValuesSpec valuesSpecAlt = new ValuesSpec("tableAlias", new String[]{"label"}, new Object[]{1});
        final ValuesSpec valuesSpecDiffTableAlias = new ValuesSpec("alias2", new String[]{"label"}, new Object[]{1});
        final ValuesSpec valuesSpecDiffLabels = new ValuesSpec("tableAlias", new String[]{"other"}, new Object[]{1});
        final ValuesSpec valuesSpecDiffValues = new ValuesSpec("tableAlias", new String[]{"label"}, new Object[]{"test"});
        // Then
        assertEquals(valuesSpec, valuesSpecAlt);
        assertNotEquals(valuesSpec, valuesSpecDiffTableAlias);
        assertNotEquals(valuesSpec, valuesSpecDiffLabels);
        assertNotEquals(valuesSpec, valuesSpecDiffValues);
    }
}
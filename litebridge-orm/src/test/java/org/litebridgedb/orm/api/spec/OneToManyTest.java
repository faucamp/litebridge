package org.litebridgedb.orm.api.spec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OneToManyTest {

    @Test
    void constructor() {
        // Given
        final FieldSpec mappedByField = new FieldSpec("mappedByField", false);

        // When
        final OneToMany oneToMany = new OneToMany(mappedByField);

        // Then
        assertEquals(mappedByField, oneToMany.mappedByField());
    }
}
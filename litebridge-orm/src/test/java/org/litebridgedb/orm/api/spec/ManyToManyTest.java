package org.litebridgedb.orm.api.spec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManyToManyTest {

    @Test
    void constructor() {
        // Given
        final String joinTable = "TEST_TABLE";
        final String joinColumn = "TEST_COLUMN";
        final String inverseJoinColumn = "REMOTE_COLUMN";

        // When
        final ManyToMany manyToMany = new ManyToMany(joinTable, joinColumn, inverseJoinColumn);

        // Then
        assertEquals(joinTable, manyToMany.joinTable());
        assertEquals(joinColumn, manyToMany.joinColumn());
        assertEquals(inverseJoinColumn, manyToMany.inverseJoinColumn());
    }
}
package org.litebridgedb.orm.api.spec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class NoFieldMappingTest {

    @Test
    void constructor() {
        // When
        final NoFieldMapping noFieldMapping = new NoFieldMapping();

        // Then
        assertNotNull(noFieldMapping);
    }
}
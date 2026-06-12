package org.litebridgedb.orm.support;

import org.junit.jupiter.api.Test;
import org.litebridgedb.orm.Litebridge;
import org.litebridgedb.orm.support.entities.TestEntity1;
import org.litebridgedb.orm.support.entities.TestEntity2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class EntityScannerTest {

    @Test
    void scanBasePackage() {
        // Given
        final EntityScanner entityScanner = new EntityScanner();

        // When
        final Class<?>[] result = entityScanner.scanBasePackage("org.litebridgedb.orm.support.entities");

        // Then
        assertEquals(2, result.length);
        assertEquals(TestEntity1.class, result[0]);
        assertEquals(TestEntity2.class, result[1]);
    }
}
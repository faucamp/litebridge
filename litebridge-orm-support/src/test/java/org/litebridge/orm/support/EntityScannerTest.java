package org.litebridge.orm.support;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.support.entities.TestEntity1;
import org.litebridge.orm.support.entities.TestEntity2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class EntityScannerTest {

    @Test
    void scanBasePackage() {
        // Given
        final EntityScanner entityScanner = new EntityScanner();

        // When
        final Class<?>[] result = entityScanner.scanBasePackage("org.litebridge.orm.support.entities");

        // Then
        assertEquals(2, result.length);
        assertEquals(TestEntity1.class, result[0]);
        assertEquals(TestEntity2.class, result[1]);
    }
}
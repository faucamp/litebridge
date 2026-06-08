package org.litebridgedb.orm.support;

import org.junit.jupiter.api.Test;
import org.litebridgedb.orm.api.register.TypeSafeDtoTableMapping;
import org.litebridgedb.orm.support.mapping.TestTypeSafeMapping1;
import org.litebridgedb.orm.support.mapping.TestTypeSafeMapping2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class TypeSafeDtoMappingScannerTest {

    @Test
    void scanBasePackage() {
        // Given
        final TypeSafeDtoMappingScanner typeSafeDtoMappingScanner = new TypeSafeDtoMappingScanner();

        // When
        final TypeSafeDtoTableMapping[] result = typeSafeDtoMappingScanner.scanBasePackage("org.litebridgedb.orm.support.mapping");

        // Then
        assertEquals(2, result.length);
        assertInstanceOf(TestTypeSafeMapping1.class, result[0]);
        assertInstanceOf(TestTypeSafeMapping2.class, result[1]);
    }
}
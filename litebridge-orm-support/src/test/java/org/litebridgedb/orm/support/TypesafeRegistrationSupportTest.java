package org.litebridgedb.orm.support;

import org.junit.jupiter.api.Test;
import org.litebridgedb.orm.Litebridge;
import org.litebridgedb.orm.support.mapping.TestTypeSafeMapping1;
import org.litebridgedb.orm.support.mapping.TestTypeSafeMapping2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class TypesafeRegistrationSupportTest {

    @Test
    void scanBasePackage() {
        // Given
        final Litebridge litebridge = mock(Litebridge.class);
        final TypesafeRegistrationSupport typesafeRegistrationSupport = new TypesafeRegistrationSupport(litebridge);

        // When
        typesafeRegistrationSupport.scanBasePackage("org.litebridgedb.orm.support.mapping");

        // Then
        verify(litebridge).register(any(TestTypeSafeMapping1.class), any(TestTypeSafeMapping2.class));
        verifyNoMoreInteractions(litebridge);
    }
}
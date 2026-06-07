package org.litebridgedb.orm.support;

import org.junit.jupiter.api.Test;
import org.litebridgedb.orm.Litebridge;
import org.litebridgedb.orm.support.entities.TestEntity1;
import org.litebridgedb.orm.support.entities.TestEntity2;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class EntityPackageRegistrationSupportTest {

    @Test
    void scanBasePackage() {
        // Given
        final Litebridge litebridge = mock(Litebridge.class);
        final EntityPackageRegistrationSupport entityPackageRegistrationSupport = new EntityPackageRegistrationSupport(litebridge);

        // When
        entityPackageRegistrationSupport.scanBasePackage("org.litebridgedb.orm.support.entities");

        // Then
        verify(litebridge).register(TestEntity1.class, TestEntity2.class);
        verifyNoMoreInteractions(litebridge);
    }
}
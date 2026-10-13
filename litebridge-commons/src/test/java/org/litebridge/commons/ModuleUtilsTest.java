package org.litebridge.commons;

import java.lang.invoke.MethodHandles;
import org.junit.jupiter.api.Test;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ModuleUtilsTest {

    @Test
    void requireAccessible_withSameModuleClass() {
        // Given
        final Class<ModuleUtils> clazz = ModuleUtils.class;

        // When
        final Class<ModuleUtils> result = ModuleUtils.requireAccessible(clazz);

        // Then
        assertEquals(clazz, result);
    }

    @Test
    void requireAccessible_withUnopenedModuleClass() {
        // java.lang.String is in java.base, and java.lang is usually not open to other modules
        // Given
        final Class<String> clazz = String.class;

        // When & Then
        if (clazz.getModule().isNamed()) {
            assertThrows(IllegalArgumentException.class, () -> ModuleUtils.requireAccessible(clazz));
        } else {
            // If running on classpath, it should pass
            assertEquals(clazz, ModuleUtils.requireAccessible(clazz));
        }
    }

    @Test
    void requireAccessible_withUnnamedModuleClass() {
        // Given
        final Class<?> unnamedClass = new ByteBuddy()
                .subclass(Object.class)
                .make()
                .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();

        // When
        final Class<?> result = ModuleUtils.requireAccessible(unnamedClass);

        // Then
        assertFalse(unnamedClass.getModule().isNamed());
        assertEquals(unnamedClass, result);
    }

    @Test
    void requireAccessible_withLookup_whenLookupHasAccess() throws IllegalAccessException {
        // Given
        final Class<ModuleUtilsTest> clazz = ModuleUtilsTest.class;
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        // When
        final Class<ModuleUtilsTest> result = ModuleUtils.requireAccessible(clazz, lookup);

        // Then
        assertEquals(clazz, result);
    }

    @Test
    void requireAccessible_withLookup_whenLookupDoesNotHaveAccess() {
        // java.lang.String is in java.base, and lookup from here cannot access it privately without open module
        // Given
        final Class<String> clazz = String.class;
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        // When & Then
        if (clazz.getModule().isNamed()) {
            assertThrows(IllegalArgumentException.class, () -> ModuleUtils.requireAccessible(clazz, lookup));
        } else {
            assertEquals(clazz, ModuleUtils.requireAccessible(clazz, lookup));
        }
    }

    @Test
    void requireAccessible_withLookup_whenNullLookup() {
        // Given
        final Class<String> clazz = String.class;

        // When & Then
        if (clazz.getModule().isNamed()) {
            assertThrows(IllegalArgumentException.class, () -> ModuleUtils.requireAccessible(clazz, (MethodHandles.Lookup) null));
        } else {
            assertEquals(clazz, ModuleUtils.requireAccessible(clazz, (MethodHandles.Lookup) null));
        }
    }
}

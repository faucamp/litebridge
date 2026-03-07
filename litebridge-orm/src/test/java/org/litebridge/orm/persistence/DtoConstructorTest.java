package org.litebridge.orm.persistence;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;
import org.litebridge.tracking.DirectFieldAccessor;
import org.litebridge.tracking.FieldAccessor;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class DtoConstructorTest {

    private static Map<Class<?>, Object> defaultConstructorCache;
    private static Map<Class<?>, Object> canonicalConstructorCache;

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void beforeAll() {
        try {
            final Field defaultConstructorCacheField = ClassUtils.getField(DtoConstructor.class, "defaultConstructorCache");
            defaultConstructorCacheField.setAccessible(true);
            defaultConstructorCache = (Map<Class<?>, Object>) defaultConstructorCacheField.get(null);

            final Field canonicalConstructorCacheField = ClassUtils.getField(DtoConstructor.class, "canonicalConstructorCache");
            canonicalConstructorCacheField.setAccessible(true);
            canonicalConstructorCache = (Map<Class<?>, Object>) canonicalConstructorCacheField.get(null);
        } catch (Exception ex) {
            fail("Failed to reflect constructor cache fields", ex);
        }
    }

    @BeforeEach
    void beforeEach() {
        defaultConstructorCache.clear();
        canonicalConstructorCache.clear();
    }

    /**
     * Test case for creating an instance using the default constructor.
     */
    @Test
    @SuppressWarnings("unchecked")
    void newInstance_defaultConstructor() throws Exception {
        // Given
        final Constructor<DefaultConstructorDto> defaultConstructor = DefaultConstructorDto.class.getDeclaredConstructor();
        defaultConstructor.setAccessible(true);

        // Ensure default constructor cache uses the mocked value
        defaultConstructorCache.put(DefaultConstructorDto.class, defaultConstructor);

        // When
        final DtoConstructor.ConstructionResult<DefaultConstructorDto> result = DtoConstructor.newInstance(DefaultConstructorDto.class, Collections.emptyList());

        // Then
        assertNotNull(result);
        assertNotNull(result.dto());
        assertTrue(result.defaultConstructorUsed());
    }

    /**
     * Test case for creating an instance using a canonical constructor (with arguments).
     */
    @Test
    @SuppressWarnings("unchecked")
    void newInstance_canonicalConstructor() throws Exception {
        // Given
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(CanonicalConstructorDto.class.getDeclaredField("id"), MethodHandles.lookup());
        final DtoConstructor.FieldAccessorValue fieldAccessorValue = new DtoConstructor.FieldAccessorValue(fieldAccessor, 42);

        // When
        final DtoConstructor.ConstructionResult<CanonicalConstructorDto> result = DtoConstructor.newInstance(CanonicalConstructorDto.class, List.of(fieldAccessorValue));

        // Then
        assertNotNull(result);
        assertNotNull(result.dto());
        assertFalse(result.defaultConstructorUsed());
        assertEquals(42, result.dto().id);
    }

    /**
     * Test case for creating an instance using a canonical constructor for a record type.
     */
    @Test
    @SuppressWarnings("unchecked")
    void newInstance_recordCanonicalConstructor() throws Exception {
        // Given
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(RecordDto.class.getDeclaredField("value"), MethodHandles.lookup());
        final DtoConstructor.FieldAccessorValue fieldAccessorValue = new DtoConstructor.FieldAccessorValue(fieldAccessor, "Hello world!");

        // When
        final DtoConstructor.ConstructionResult<RecordDto> result = DtoConstructor.newInstance(RecordDto.class, List.of(fieldAccessorValue));

        // Then
        assertNotNull(result);
        assertNotNull(result.dto());
        assertFalse(result.defaultConstructorUsed());
        assertEquals("Hello world!", result.dto().value());
    }

    /**
     * Test case for falling back to default constructor when arguments don't match any canonical constructor.
     */
    @Test
    @SuppressWarnings("unchecked")
    void newInstance_fallbackToDefaultConstructor() throws Exception {
        // Given
        final Constructor<NoArgAndOtherConstructorDto> defaultConstructor = NoArgAndOtherConstructorDto.class.getDeclaredConstructor();
        defaultConstructor.setAccessible(true);

        final FieldAccessor fieldAccessor = new DirectFieldAccessor(NoArgAndOtherConstructorDto.class.getDeclaredField("intField"), MethodHandles.lookup());
        final DtoConstructor.FieldAccessorValue fieldAccessorValue = new DtoConstructor.FieldAccessorValue(fieldAccessor, 42);

        // When
        final DtoConstructor.ConstructionResult<NoArgAndOtherConstructorDto> result = DtoConstructor.newInstance(NoArgAndOtherConstructorDto.class, List.of(fieldAccessorValue));

        // Then
        assertNotNull(result);
        assertNotNull(result.dto());
        assertTrue(result.defaultConstructorUsed());
    }

    /**
     * Test case for missing default constructor leading to exception.
     */
    @Test
    void newInstance_noDefaultConstructor() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> DtoConstructor.newInstance(NoDefaultConstructorDto.class, Collections.emptyList()));
    }

    /**
     * Test case for no matching canonical constructor leading to default constructor usage.
     */
    @Test
    @SuppressWarnings("unchecked")
    void newInstance_noMatchingCanonicalConstructorUsesDefault() throws Exception {
        // Given
        final Constructor<NoArgAndOtherConstructorDto> defaultConstructor = NoArgAndOtherConstructorDto.class.getDeclaredConstructor();
        defaultConstructor.setAccessible(true);

        // Populate cache
        defaultConstructorCache.put(NoArgAndOtherConstructorDto.class, defaultConstructor);
        canonicalConstructorCache.put(NoArgAndOtherConstructorDto.class, DtoConstructor.NO_CONSTRUCTOR);

        // When
        final DtoConstructor.ConstructionResult<NoArgAndOtherConstructorDto> result = DtoConstructor.newInstance(NoArgAndOtherConstructorDto.class, Collections.emptyList());

        // Then
        assertNotNull(result);
        assertNotNull(result.dto());
        assertTrue(result.defaultConstructorUsed());
    }

    /**
     * Test case for caching constructors when no suitable ones are present.
     */
    @Test
    void newInstance_noSuitableConstructors() throws Exception {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> DtoConstructor.newInstance(NoSuitableConstructorDto.class, Collections.emptyList()));
    }

    /**
     * Test case for successful caching of constructors.
     */
    @Test
    void newInstance_successfulCacheConstructors() throws Exception {
        // Given
        final Constructor<NoArgConstructorDto> defaultConstructor = NoArgConstructorDto.class.getDeclaredConstructor();

        // When
        DtoConstructor.newInstance(NoArgConstructorDto.class, Collections.emptyList());

        // Assert
        assertEquals(defaultConstructor, defaultConstructorCache.get(NoArgConstructorDto.class));
        assertEquals(DtoConstructor.NO_CONSTRUCTOR, canonicalConstructorCache.get(NoArgConstructorDto.class));
    }

    private static final class DefaultConstructorDto {
    }

    private static final class NoArgConstructorDto {
        public NoArgConstructorDto() {
        }
    }

    private static final class CanonicalConstructorDto {
        private final int id;

        public CanonicalConstructorDto(int id) {
            this.id = id;
        }
    }

    private static final class NoArgAndOtherConstructorDto {
        private int intField;

        public NoArgAndOtherConstructorDto() {
        }

        public NoArgAndOtherConstructorDto(String ignored) {
        }
    }

    private static final class NoDefaultConstructorDto {
        public NoDefaultConstructorDto(String ignored) {
        }
    }

    private static final class NoSuitableConstructorDto {
        public NoSuitableConstructorDto(String ignored) {
        }
    }

    private record RecordDto(String value) {
    }
}
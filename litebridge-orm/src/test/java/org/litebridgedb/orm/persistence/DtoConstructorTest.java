package org.litebridgedb.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridgedb.tracking.FieldAccessor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DtoConstructorTest {

    @Test
    void newInstanceUsesDefaultConstructorWhenAvailable() {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final OrmTable ormTable = ormTable();
        when(tableRegistry.getTableOrThrow(DefaultConstructorDto.class)).thenReturn(ormTable);

        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);

        // When
        final DtoConstructor.ConstructionResult<DefaultConstructorDto> result =
                dtoConstructor.newInstance(DefaultConstructorDto.class, List.of());

        // Then
        assertTrue(result.defaultConstructorUsed());
        assertInstanceOf(DefaultConstructorDto.class, result.dto());
        assertEquals("default", result.dto().value);
    }

    @Test
    void newInstanceUsesCachedConstructorsAfterFirstLookup() {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final OrmTable ormTable = ormTable();
        when(tableRegistry.getTableOrThrow(DefaultConstructorDto.class)).thenReturn(ormTable);

        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);

        // When
        dtoConstructor.newInstance(DefaultConstructorDto.class, List.of());
        dtoConstructor.newInstance(DefaultConstructorDto.class, List.of());

        // Then
        verify(tableRegistry).getTableOrThrow(DefaultConstructorDto.class);
    }

    @Test
    void newInstanceUsesPojoCanonicalConstructorMatchedByTypes() {
        // Given
        final FieldAccessor id = field("id", Long.class);
        final FieldAccessor name = field("name", String.class);
        final OrmTable ormTable = ormTable(id, name);

        final TableRegistry tableRegistry = mock(TableRegistry.class);
        when(tableRegistry.getTableOrThrow(PojoCanonicalDto.class))
                .thenReturn(ormTable);

        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);

        // When
        final DtoConstructor.ConstructionResult<PojoCanonicalDto> result =
                dtoConstructor.newInstance(
                        PojoCanonicalDto.class,
                        List.of(
                                new DtoConstructor.FieldAccessorValue(id, 123L),
                                new DtoConstructor.FieldAccessorValue(name, "Alice")
                        )
                );

        // Then
        assertFalse(result.defaultConstructorUsed());
        assertEquals(123L, result.dto().id);
        assertEquals("Alice", result.dto().name);
    }

    @Test
    void newInstanceConvertsDtoDependencyConstructorArgumentToNull() {
        // Given
        final FieldAccessor id = field("id", Long.class);
        final FieldAccessor dependency = field("dependency", DependencyDto.class);
        final OrmTable ormTable = ormTable(id, dependency);
        final DtoConstructor.DtoDependency dtoDependency =
                new DtoConstructor.DtoDependency(dependency, DependencyDto.class, List.of(new DtoConstructor.FieldAccessorValue(id, 456L)));

        final TableRegistry tableRegistry = mock(TableRegistry.class);
        when(tableRegistry.getTableOrThrow(PojoWithDependencyDto.class))
                .thenReturn(ormTable);

        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);

        // When
        final DtoConstructor.ConstructionResult<PojoWithDependencyDto> result =
                dtoConstructor.newInstance(
                        PojoWithDependencyDto.class,
                        List.of(
                                new DtoConstructor.FieldAccessorValue(id, 123L),
                                new DtoConstructor.FieldAccessorValue(dependency, dtoDependency)
                        )
                );

        // Then
        assertFalse(result.defaultConstructorUsed());
        assertEquals(123L, result.dto().id);
        assertNull(result.dto().dependency);
        assertEquals(List.of(456L), dtoDependency.targetPrimaryKeyValue());
    }

    @Test
    void newInstanceUsesRecordCanonicalConstructor() {
        // Given
        final FieldAccessor id = field("id", Long.class);
        final FieldAccessor name = field("name", String.class);
        final OrmTable ormTable = ormTable(id, name);

        final TableRegistry tableRegistry = mock(TableRegistry.class);
        when(tableRegistry.getTableOrThrow(RecordDto.class))
                .thenReturn(ormTable);

        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);

        // When
        final DtoConstructor.ConstructionResult<RecordDto> result =
                dtoConstructor.newInstance(
                        RecordDto.class,
                        List.of(
                                new DtoConstructor.FieldAccessorValue(id, 321L),
                                new DtoConstructor.FieldAccessorValue(name, "Bob")
                        )
                );

        // Then
        assertFalse(result.defaultConstructorUsed());
        assertEquals(new RecordDto(321L, "Bob"), result.dto());
    }

    @Test
    void cacheConstructorsCachesRelatedDtoFromParentContext() {
        // Given
        final FieldAccessor dependency = field("dependency", DependencyDto.class);

        final OrmTable parentTable = ormTableWithRelatedDtos(Set.of(DependencyDto.class), dependency);
        final OrmTable relatedContextTable = ormTable();

        final TableRegistry tableRegistry = mock(TableRegistry.class);
        when(tableRegistry.getTableOrThrow(ParentWithDefaultConstructorDto.class)).thenReturn(parentTable);
        when(tableRegistry.getTableInContext(DependencyDto.class, ParentWithDefaultConstructorDto.class))
                .thenReturn(Optional.of(relatedContextTable));

        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);

        // When
        final DtoConstructor.ConstructionResult<ParentWithDefaultConstructorDto> result =
                dtoConstructor.newInstance(ParentWithDefaultConstructorDto.class, List.of());

        // Then
        assertTrue(result.defaultConstructorUsed());
        verify(tableRegistry).getTableInContext(DependencyDto.class, ParentWithDefaultConstructorDto.class);
    }

    @Test
    void cacheConstructorsFallsBackToGlobalRegistryForRelatedDtoWhenContextTableIsMissing() {
        // Given
        final FieldAccessor dependency = field("dependency", DependencyDto.class);

        final OrmTable parentTable = ormTableWithRelatedDtos(Set.of(DependencyDto.class), dependency);
        final OrmTable relatedGlobalTable = ormTable();

        final TableRegistry tableRegistry = mock(TableRegistry.class);
        when(tableRegistry.getTableOrThrow(ParentWithDefaultConstructorDto.class)).thenReturn(parentTable);
        when(tableRegistry.getTableInContext(DependencyDto.class, ParentWithDefaultConstructorDto.class))
                .thenReturn(Optional.empty());
        when(tableRegistry.getTableOrThrow(DependencyDto.class)).thenReturn(relatedGlobalTable);

        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);

        // When
        dtoConstructor.newInstance(ParentWithDefaultConstructorDto.class, List.of());

        // Then
        verify(tableRegistry).getTableOrThrow(DependencyDto.class);
    }

    @Test
    void newInstanceThrowsWhenNoDefaultOrCanonicalConstructorMatches() {
        // Given
        final FieldAccessor id = field("id", Long.class);
        final OrmTable ormTable = ormTable(id);

        final TableRegistry tableRegistry = mock(TableRegistry.class);
        when(tableRegistry.getTableOrThrow(NoSuitableConstructorDto.class))
                .thenReturn(ormTable);

        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);

        // When
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dtoConstructor.newInstance(NoSuitableConstructorDto.class, List.of(new DtoConstructor.FieldAccessorValue(id, 1L)))
        );

        // Then
        assertTrue(exception.getMessage().contains("No suitable constructor found for DTO class"));
    }

    @Test
    void pojoConstructorThrowsWhenDuplicateFieldTypesRequireUnavailableParameterNames() {
        // Given
        final FieldAccessor firstName = field("firstName", String.class);
        final FieldAccessor lastName = field("lastName", String.class);
        final OrmTable ormTable = ormTable(firstName, lastName);

        final TableRegistry tableRegistry = mock(TableRegistry.class);
        when(tableRegistry.getTableOrThrow(DuplicateTypePojoDto.class))
                .thenReturn(ormTable);

        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);

        // When
        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> dtoConstructor.newInstance(
                        DuplicateTypePojoDto.class,
                        List.of(
                                new DtoConstructor.FieldAccessorValue(firstName, "Alice"),
                                new DtoConstructor.FieldAccessorValue(lastName, "Smith")
                        )
                )
        );

        // Then
        assertTrue(exception.getMessage().contains("Unable to determine parameter names"));
    }

    @Test
    void recordConstructorIsRejectedWhenFieldNameIsMissing() {
        // Given
        final FieldAccessor different = field("different", Long.class);
        final FieldAccessor name = field("name", String.class);
        final OrmTable ormTable = ormTable(different, name);

        final TableRegistry tableRegistry = mock(TableRegistry.class);
        when(tableRegistry.getTableOrThrow(RecordDto.class))
                .thenReturn(ormTable);

        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);

        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> dtoConstructor.newInstance(
                        RecordDto.class,
                        List.of(
                                new DtoConstructor.FieldAccessorValue(different, 1L),
                                new DtoConstructor.FieldAccessorValue(name, "Alice")
                        )
                )
        );
    }

    @Test
    void recordConstructorIsRejectedWhenFieldTypeIsNotAssignableToRecordComponentType() {
        // Given
        final FieldAccessor id = field("id", Integer.class);
        final FieldAccessor name = field("name", String.class);
        final OrmTable ormTable = ormTable(id, name);

        final TableRegistry tableRegistry = mock(TableRegistry.class);
        when(tableRegistry.getTableOrThrow(RecordDto.class))
                .thenReturn(ormTable);

        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);

        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> dtoConstructor.newInstance(
                        RecordDto.class,
                        List.of(
                                new DtoConstructor.FieldAccessorValue(id, 1),
                                new DtoConstructor.FieldAccessorValue(name, "Alice")
                        )
                )
        );
    }

    @Test
    void recordConstructorIsRejectedWhenParameterNameDoesNotMatchMappedFieldName() {
        // Given
        final FieldAccessor id = mock(FieldAccessor.class);
        when(id.name()).thenReturn("id", "renamed");
        when(id.type()).thenAnswer(i -> Long.class);

        final FieldAccessor name = field("name", String.class);
        final OrmTable ormTable = ormTable(id, name);

        final TableRegistry tableRegistry = mock(TableRegistry.class);
        when(tableRegistry.getTableOrThrow(RecordDto.class))
                .thenReturn(ormTable);

        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);

        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> dtoConstructor.newInstance(
                        RecordDto.class,
                        List.of(
                                new DtoConstructor.FieldAccessorValue(id, 1L),
                                new DtoConstructor.FieldAccessorValue(name, "Alice")
                        )
                )
        );

        verify(id, atLeastOnce()).name();
    }

    @Test
    void pojoConstructorIsRejectedWhenTypeOnlyMatchingCannotFindFieldForParameterType() {
        // Given
        final FieldAccessor id = field("id", Long.class);
        final FieldAccessor active = field("active", Boolean.class);
        final OrmTable ormTable = ormTable(id, active);

        final TableRegistry tableRegistry = mock(TableRegistry.class);
        when(tableRegistry.getTableOrThrow(PojoCanonicalDto.class))
                .thenReturn(ormTable);

        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);

        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> dtoConstructor.newInstance(
                        PojoCanonicalDto.class,
                        List.of(
                                new DtoConstructor.FieldAccessorValue(id, 1L),
                                new DtoConstructor.FieldAccessorValue(active, true)
                        )
                )
        );
    }

    @Test
    void pojoConstructorIsRejectedWhenConstructorParameterCountDoesNotMatchFieldCount() {
        // Given
        final FieldAccessor id = field("id", Long.class);
        final FieldAccessor name = field("name", String.class);
        final FieldAccessor active = field("active", Boolean.class);
        final OrmTable ormTable = ormTable(id, name, active);

        final TableRegistry tableRegistry = mock(TableRegistry.class);
        when(tableRegistry.getTableOrThrow(PojoCanonicalDto.class))
                .thenReturn(ormTable);

        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);

        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> dtoConstructor.newInstance(
                        PojoCanonicalDto.class,
                        List.of(
                                new DtoConstructor.FieldAccessorValue(id, 1L),
                                new DtoConstructor.FieldAccessorValue(name, "Alice"),
                                new DtoConstructor.FieldAccessorValue(active, true)
                        )
                )
        );
    }

    @Test
    void fieldAccessorValueStoresFieldAndValue() {
        // Given
        final FieldAccessor field = field("id", Long.class);

        // When
        final DtoConstructor.FieldAccessorValue value = new DtoConstructor.FieldAccessorValue(field, 123L);

        // Then
        assertSame(field, value.field());
        assertEquals(123L, value.value());
    }

    private static OrmTable ormTable(final FieldAccessor... fieldAccessors) {
        return ormTableWithRelatedDtos(Set.of(), fieldAccessors);
    }

    private static OrmTable ormTableWithRelatedDtos(final Set<Class<?>> relatedDtoClasses, final FieldAccessor... fieldAccessors) {
        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.fieldAcessorStream()).thenReturn(Stream.of(fieldAccessors));
        when(ormTable.getRelatedDtoClasses()).thenReturn(relatedDtoClasses);
        return ormTable;
    }

    private static FieldAccessor field(final String name, final Class<?> type) {
        final FieldAccessor fieldAccessor = mock(FieldAccessor.class);
        when(fieldAccessor.name()).thenReturn(name);
        when(fieldAccessor.type()).thenAnswer(i -> type);
        return fieldAccessor;
    }

    private static final class DefaultConstructorDto {

        private final String value;

        private DefaultConstructorDto() {
            this.value = "default";
        }
    }

    private static final class ParentWithDefaultConstructorDto {

        private ParentWithDefaultConstructorDto() {
        }
    }

    private static final class DependencyDto {

        private DependencyDto() {
        }
    }

    private static final class PojoCanonicalDto {

        private final Long id;
        private final String name;

        private PojoCanonicalDto(final Long id, final String name) {
            this.id = id;
            this.name = name;
        }
    }

    private static final class PojoWithDependencyDto {

        private final Long id;
        private final DependencyDto dependency;

        private PojoWithDependencyDto(final Long id, final DependencyDto dependency) {
            this.id = id;
            this.dependency = dependency;
        }
    }

    private static final class DuplicateTypePojoDto {

        @SuppressWarnings("unused")
        private DuplicateTypePojoDto(final String firstName, final String lastName) {
        }
    }

    private static final class NoSuitableConstructorDto {

        @SuppressWarnings("unused")
        private NoSuitableConstructorDto(final Long id, final String name) {
        }
    }

    private record RecordDto(Long id, String name) {
    }
}
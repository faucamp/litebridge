package org.litebridge.orm.engine;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.annotation.Column;
import org.litebridge.orm.annotation.Table;
import org.litebridge.orm.api.spec.ColumnSpec;
import org.litebridge.orm.api.spec.DtoTableSpec;
import org.litebridge.orm.api.spec.FieldSpec;
import org.litebridge.orm.api.spec.TableSpec;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMapper;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrationEngineTest {

    @Test
    void registerWithProgrammaticContextFunction() {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMapper tableMapper = mock(TableMapper.class);
        final ChangeTracker changeTracker = mock(ChangeTracker.class);
        final ClassFieldAccessorCache accessorCache = mock(ClassFieldAccessorCache.class);
        when(changeTracker.classFieldAccessorCache()).thenReturn(accessorCache);
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getNestedDtoClasses()).thenReturn(Collections.emptyList());
        final TableMapper.MappedTable mappedTable = new TableMapper.MappedTable(ormTable, Collections.emptyList());
        when(tableMapper.mapToTable(any(), eq(TestDto.class), any(), anySet())).thenReturn(mappedTable);

        final RegistrationEngine engine = new RegistrationEngine(databaseProvider, tableRegistry, tableMapper, changeTracker, lookup);

        // When
        engine.register(TestDto.class, rc -> rc.mapToTable("test_table").with(f -> f.mapField("id").toColumn("id")));

        // Then
        verify(tableMapper).mapToTable(eq(lookup), eq(TestDto.class), any(), anySet());
        verify(tableRegistry).addTable(TestDto.class, ormTable);
    }

    @Test
    void registerWithAnnotatedEntityClasses() {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMapper tableMapper = mock(TableMapper.class);
        final ChangeTracker changeTracker = mock(ChangeTracker.class);
        final ClassFieldAccessorCache accessorCache = mock(ClassFieldAccessorCache.class);
        when(changeTracker.classFieldAccessorCache()).thenReturn(accessorCache);
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getNestedDtoClasses()).thenReturn(Collections.emptyList());
        final TableMapper.MappedTable mappedTable = new TableMapper.MappedTable(ormTable, Collections.emptyList());
        when(tableMapper.mapToTable(any(), eq(ValidEntity.class), any(), anySet())).thenReturn(mappedTable);

        final RegistrationEngine engine = new RegistrationEngine(databaseProvider, tableRegistry, tableMapper, changeTracker, lookup);

        // When
        engine.register(ValidEntity.class);

        // Then
        verify(tableMapper).mapToTable(eq(lookup), eq(ValidEntity.class), any(), anySet());
        verify(tableRegistry).addTable(ValidEntity.class, ormTable);
    }

    @Test
    void registerElevatedLookupIllegalAccessExceptionCaughtGracefully() {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMapper tableMapper = mock(TableMapper.class);
        final ChangeTracker changeTracker = mock(ChangeTracker.class);
        final ClassFieldAccessorCache accessorCache = mock(ClassFieldAccessorCache.class);
        when(changeTracker.classFieldAccessorCache()).thenReturn(accessorCache);
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getNestedDtoClasses()).thenReturn(Collections.emptyList());
        final TableMapper.MappedTable mappedTable = new TableMapper.MappedTable(ormTable, Collections.emptyList());
        when(tableMapper.mapToTable(any(), eq(String.class), any(), anySet())).thenReturn(mappedTable);

        final RegistrationEngine engine = new RegistrationEngine(databaseProvider, tableRegistry, tableMapper, changeTracker, lookup);
        final DtoTableSpec spec = new DtoTableSpec(String.class, createTableSpec("strings"), Collections.emptyList());

        // When / Then
        assertDoesNotThrow(() -> engine.register(spec));
        verify(tableMapper).mapToTable(eq(lookup), eq(String.class), any(), anySet());
        verify(tableRegistry).addTable(String.class, ormTable);
        verify(accessorCache, never()).registerElevatedLookup(eq(String.class), any());
    }

    @Test
    void registerWithInterfacesAndNestedDtoClasses() {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMapper tableMapper = mock(TableMapper.class);
        final ChangeTracker changeTracker = mock(ChangeTracker.class);
        final ClassFieldAccessorCache accessorCache = mock(ClassFieldAccessorCache.class);
        when(changeTracker.classFieldAccessorCache()).thenReturn(accessorCache);
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getNestedDtoClasses()).thenReturn(List.of(NestedDto.class));
        final TableMapper.MappedTable mappedTable = new TableMapper.MappedTable(ormTable, Collections.emptyList());
        when(tableMapper.mapToTable(any(), eq(TestDto.class), any(), anySet())).thenReturn(mappedTable);

        final RegistrationEngine engine = new RegistrationEngine(databaseProvider, tableRegistry, tableMapper, changeTracker, lookup);
        final DtoTableSpec spec = new DtoTableSpec(TestDto.class, createTableSpec("test_table"), List.of(TestInterface.class));

        // When
        engine.register(spec);

        // Then
        verify(ormTable).setDtoClassInterfaces(Set.of(TestInterface.class));
        verify(tableRegistry).addTable(TestInterface.class, ormTable);
        verify(tableRegistry).addTable(NestedDto.class, ormTable);
        verify(tableRegistry).addTable(TestDto.class, ormTable);
    }

    @Test
    void registerManyToOneDependenciesWhenTargetAlreadyRegistered() {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMapper tableMapper = mock(TableMapper.class);
        final ChangeTracker changeTracker = mock(ChangeTracker.class);
        final ClassFieldAccessorCache accessorCache = mock(ClassFieldAccessorCache.class);
        when(changeTracker.classFieldAccessorCache()).thenReturn(accessorCache);
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        final OrmTable dependentOrmTable = mock(OrmTable.class);
        when(dependentOrmTable.getNestedDtoClasses()).thenReturn(Collections.emptyList());

        final OrmTable targetOrmTable = mock(OrmTable.class);
        when(tableRegistry.getOrmTable(TargetDto.class)).thenReturn(targetOrmTable);

        final FieldAccessor fieldAccessor = mock(FieldAccessor.class);
        when(fieldAccessor.genericType()).thenReturn((Class) TargetDto.class);

        final TableMapper.MappedTable mappedTable = new TableMapper.MappedTable(dependentOrmTable, List.of(fieldAccessor));
        when(tableMapper.mapToTable(any(), eq(TestDto.class), any(), anySet())).thenReturn(mappedTable);

        final RegistrationEngine engine = new RegistrationEngine(databaseProvider, tableRegistry, tableMapper, changeTracker, lookup);
        final DtoTableSpec spec = new DtoTableSpec(TestDto.class, createTableSpec("test_table"), Collections.emptyList());

        // When
        engine.register(spec);

        // Then
        verify(targetOrmTable).addOneToManyReverseMapping(fieldAccessor);
    }

    @Test
    void registerManyToOneDependenciesDeferredResolution() {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMapper tableMapper = mock(TableMapper.class);
        final ChangeTracker changeTracker = mock(ChangeTracker.class);
        final ClassFieldAccessorCache accessorCache = mock(ClassFieldAccessorCache.class);
        when(changeTracker.classFieldAccessorCache()).thenReturn(accessorCache);
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        // Dependent table has two many-to-one dependencies: TargetDto and OtherDto
        final FieldAccessor targetAccessor = mock(FieldAccessor.class);
        when(targetAccessor.genericType()).thenReturn((Class) TargetDto.class);

        final FieldAccessor otherAccessor = mock(FieldAccessor.class);
        when(otherAccessor.genericType()).thenReturn((Class) OtherDto.class);

        final OrmTable dependentOrmTable = mock(OrmTable.class);
        when(dependentOrmTable.getNestedDtoClasses()).thenReturn(Collections.emptyList());
        final TableMapper.MappedTable dependentMapped = new TableMapper.MappedTable(dependentOrmTable, List.of(targetAccessor, otherAccessor));
        when(tableMapper.mapToTable(any(), eq(TestDto.class), any(), anySet())).thenReturn(dependentMapped);

        // Target tables are not yet registered
        when(tableRegistry.getOrmTable(TargetDto.class)).thenReturn(null);
        when(tableRegistry.getOrmTable(OtherDto.class)).thenReturn(null);

        final OrmTable targetOrmTable = mock(OrmTable.class);
        when(targetOrmTable.getNestedDtoClasses()).thenReturn(Collections.emptyList());
        final TableMapper.MappedTable targetMapped = new TableMapper.MappedTable(targetOrmTable, Collections.emptyList());
        when(tableMapper.mapToTable(any(), eq(TargetDto.class), any(), anySet())).thenReturn(targetMapped);

        final RegistrationEngine engine = new RegistrationEngine(databaseProvider, tableRegistry, tableMapper, changeTracker, lookup);

        final DtoTableSpec dependentSpec = new DtoTableSpec(TestDto.class, createTableSpec("test_table"), Collections.emptyList());
        final DtoTableSpec targetSpec = new DtoTableSpec(TargetDto.class, createTableSpec("target_table"), Collections.emptyList());

        // When: register dependent table first, then target table
        engine.register(dependentSpec);
        verify(targetOrmTable, never()).addOneToManyReverseMapping(any());

        engine.register(targetSpec);

        // Then: target table received reverse mapping for targetAccessor, but not for otherAccessor
        verify(targetOrmTable).addOneToManyReverseMapping(targetAccessor);
        verify(targetOrmTable, never()).addOneToManyReverseMapping(otherAccessor);
    }

    private static TableSpec createTableSpec(final String tableName) {
        return new TableSpec(tableName, Map.of(new FieldSpec("id", false), new ColumnSpec("id")));
    }

    interface TestInterface {
    }

    static class TestDto implements TestInterface {
        private Long id;
    }

    static class NestedDto {
        private String detail;
    }

    static class TargetDto {
        private Long id;
    }

    static class OtherDto {
        private Long id;
    }

    @Table("valid_entity")
    static class ValidEntity {
        @Column("id")
        private Long id;
    }
}

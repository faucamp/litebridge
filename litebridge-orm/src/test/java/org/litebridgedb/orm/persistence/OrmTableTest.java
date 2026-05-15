package org.litebridgedb.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.MappedFieldTarget;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.tracking.ChangeTracker;
import org.litebridgedb.tracking.ClassFieldAccessorCache;
import org.litebridgedb.tracking.FieldAccessor;
import org.litebridgedb.tracking.TrackedDto;

import java.lang.invoke.MethodHandles;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrmTableTest {

    @Test
    void constructor_andGetters() {
        // Given
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final FieldAccessor idField = fieldAccessor(changeTracker, TestDto.class, "id");
        final FieldAccessor nameField = fieldAccessor(changeTracker, TestDto.class, "name");

        final ColumnMetaData idColumn = column("test_table", "id", Types.BIGINT);
        final ColumnMetaData nameColumn = column("test_table", "name", Types.VARCHAR);

        final OrmTable ormTable = new OrmTable(TestDto.class,
                tableMetaData("test_table", idColumn, nameColumn),
                Map.of(
                        idField, idColumn,
                        nameField, nameColumn
                ),
                changeTracker,
                new ClassFieldAccessorCache(MethodHandles.lookup()));

        // Then
        assertEquals(TestDto.class, ormTable.dtoClass());
        assertEquals(tableMetaData("test_table", idColumn, nameColumn), ormTable.getMetaData());
        assertSame(idColumn, ormTable.getColumnForFieldName("id"));
        assertSame(nameColumn, ormTable.getColumnForFieldName("name"));
        assertSame(idColumn, ormTable.getColumn("id"));
        assertSame(nameField, ormTable.getFieldForColumnName("name"));
        assertSame(idField, ormTable.fieldForColumnNameOrNull("id"));
        assertNull(ormTable.fieldForColumnNameOrNull("unknown"));
    }

    @Test
    void getColumnForFieldName_notFound() {
        // Given
        final OrmTable ormTable = simpleOrmTable();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ormTable.getColumnForFieldName("unknown"));
    }

    @Test
    void getColumn_notFound() {
        // Given
        final OrmTable ormTable = simpleOrmTable();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ormTable.getColumn("unknown"));
    }

    @Test
    void getFieldForColumnName_notFound() {
        // Given
        final OrmTable ormTable = simpleOrmTable();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ormTable.getFieldForColumnName("unknown"));
    }

    @Test
    void fieldAcessorStream() {
        // Given
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final FieldAccessor idField = fieldAccessor(changeTracker, TestDto.class, "id");
        final FieldAccessor nameField = fieldAccessor(changeTracker, TestDto.class, "name");

        final ColumnMetaData idColumn = column("test_table", "id", Types.BIGINT);
        final ColumnMetaData nameColumn = column("test_table", "name", Types.VARCHAR);

        final OrmTable ormTable = new OrmTable(TestDto.class,
                tableMetaData("test_table", idColumn, nameColumn),
                Map.of(
                        idField, idColumn,
                        nameField, nameColumn
                ),
                changeTracker,
                new ClassFieldAccessorCache(MethodHandles.lookup()));

        // When
        final List<FieldAccessor> result = ormTable.fieldAcessorStream().toList();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(idField));
        assertTrue(result.contains(nameField));
    }

    @Test
    void trackDto_andGetTrackedDto() {
        // Given
        final OrmTable ormTable = simpleOrmTable();
        final TestDto dto = new TestDto();

        // When
        ormTable.trackDto(dto);
        final TrackedDto<TestDto> result = ormTable.getTrackedDto(dto);

        // Then
        assertNotNull(result);
    }

    @Test
    void ensureTrackedDto_new() {
        // Given
        final OrmTable ormTable = simpleOrmTable();
        final TestDto dto = new TestDto();

        // When
        final TrackedDto<TestDto> result = ormTable.ensureTrackedDto(dto);

        // Then
        assertNotNull(result);
        assertSame(result, ormTable.getTrackedDto(dto));
    }

    @Test
    void ensureTrackedDto_existing() {
        // Given
        final OrmTable ormTable = simpleOrmTable();
        final TestDto dto = new TestDto();
        ormTable.trackDto(dto);

        // When
        final TrackedDto<TestDto> result = ormTable.ensureTrackedDto(dto);

        // Then
        assertSame(result, ormTable.getTrackedDto(dto));
    }

    @Test
    void syncPersistedDto() {
        // Given
        final OrmTable ormTable = simpleOrmTable();
        final TestDto dto = new TestDto();
        ormTable.trackDto(dto);

        // When
        ormTable.syncPersistedDto(dto);

        // Then
        assertTrue(ormTable.isPersistedDto(dto));
    }

    @Test
    void isPersistedDto_notPersisted() {
        // Given
        final OrmTable ormTable = simpleOrmTable();

        // When
        final boolean result = ormTable.isPersistedDto(new TestDto());

        // Then
        assertFalse(result);
    }

    @Test
    void getOneToManyMappings_andGetOneToManyMappingForField() {
        // Given
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final FieldAccessor childrenField = fieldAccessor(changeTracker, ParentDto.class, "children");
        final FieldAccessor parentField = fieldAccessor(changeTracker, ChildDto.class, "parent");

        final ColumnMetaData idColumn = column("parent_table", "id", Types.BIGINT);

        final MappedOneToMany mappedOneToMany = new MappedOneToMany(parentField, childrenField);

        final OrmTable ormTable = new OrmTable(ParentDto.class,
                tableMetaData("parent_table", idColumn),
                Map.of(childrenField, mappedOneToMany),
                changeTracker,
                new ClassFieldAccessorCache(MethodHandles.lookup()));

        // Then
        assertEquals(1, ormTable.getOneToManyMappings().size());
        assertSame(mappedOneToMany, ormTable.getOneToManyMappings().getFirst());
        assertTrue(ormTable.getOneToManyMappingForField(childrenField).isPresent());
        assertSame(mappedOneToMany, ormTable.getOneToManyMappingForField(childrenField).orElseThrow());
    }

    @Test
    void getOneToManyMappingForField_notFound() {
        // Given
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final FieldAccessor idField = fieldAccessor(changeTracker, TestDto.class, "id");
        final ColumnMetaData idColumn = column("test_table", "id", Types.BIGINT);

        final OrmTable ormTable = new OrmTable(TestDto.class,
                tableMetaData("test_table", idColumn),
                Map.of(idField, idColumn),
                changeTracker,
                new ClassFieldAccessorCache(MethodHandles.lookup()));

        // Then
        assertTrue(ormTable.getOneToManyMappingForField(idField).isEmpty());
        assertTrue(ormTable.getOneToManyMappings().isEmpty());
        assertTrue(ormTable.getManyToManyMappings().isEmpty());
        assertTrue(ormTable.getManyToManyMappingForField(idField).isEmpty());
    }

    @Test
    void mappedFieldTargets_ordersByTableMetadataColumns() {
        // Given
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final FieldAccessor idField = fieldAccessor(changeTracker, TestDto.class, "id");
        final FieldAccessor nameField = fieldAccessor(changeTracker, TestDto.class, "name");

        final ColumnMetaData idColumn = column("test_table", "id", Types.BIGINT);
        final ColumnMetaData nameColumn = column("test_table", "name", Types.VARCHAR);

        final Map<FieldAccessor, MappedFieldTarget> fieldTargetMap = new LinkedHashMap<>();
        fieldTargetMap.put(nameField, nameColumn);
        fieldTargetMap.put(idField, idColumn);

        final OrmTable ormTable = new OrmTable(TestDto.class,
                tableMetaData("test_table", idColumn, nameColumn),
                fieldTargetMap,
                changeTracker,
                new ClassFieldAccessorCache(MethodHandles.lookup()));

        // When
        final List<Map.Entry<FieldAccessor, MappedFieldTarget>> result = ormTable.mappedFieldTargets();

        // Then
        assertEquals(2, result.size());
        assertSame(idField, result.get(0).getKey());
        assertSame(idColumn, result.get(0).getValue());
        assertSame(nameField, result.get(1).getKey());
        assertSame(nameColumn, result.get(1).getValue());
    }

    private static OrmTable simpleOrmTable() {
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final FieldAccessor idField = fieldAccessor(changeTracker, TestDto.class, "id");
        final ColumnMetaData idColumn = column("test_table", "id", Types.BIGINT);

        return new OrmTable(TestDto.class,
                tableMetaData("test_table", idColumn),
                Map.of(idField, idColumn),
                changeTracker,
                new ClassFieldAccessorCache(MethodHandles.lookup()));
    }

    private static FieldAccessor fieldAccessor(final ChangeTracker changeTracker, final Class<?> dtoClass, final String fieldName) {
        return changeTracker.classFieldAccessorCache().fieldAccessors(dtoClass).stream()
                .filter(fieldAccessor -> fieldAccessor.name().equals(fieldName))
                .findFirst()
                .orElseThrow();
    }

    private static ColumnMetaData column(final String tableName, final String columnName, final int type) {
        return new ColumnMetaData(new Table("", "public", tableName), columnName, false, type);
    }

    private static TableMetaData tableMetaData(final String tableName, final ColumnMetaData... columns) {
        return new TableMetaData(new Table("", "public", tableName),
                List.of(columns[0].name()),
                List.of(columns));
    }

    private static class TestDto {
        private Long id;
        private String name;
    }

    private static class ParentDto {
        private Long id;
        private List<ChildDto> children;
    }

    private static class ChildDto {
        private Long id;
        private ParentDto parent;
    }
}
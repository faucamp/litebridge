package org.litebridge.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.RowColumn;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;

import java.lang.invoke.MethodHandles;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DtoMapperTest {

    @Test
    void toDtos_emptyRows() {
        // Given
        final DtoMapper dtoMapper = createDtoMapper(new TableRegistry());

        // When
        final List<TestPersonDto> result = dtoMapper.toDtos(TestPersonDto.class, null, Collections.emptyList());

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void toDtos_unqualifiedTableInRowColumn() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final Table canonicalTable = new Table("", "public", "test_person");
        final ColumnMetaData idCol = new ColumnMetaData(canonicalTable, "id", false, Types.BIGINT);
        final ColumnMetaData nameCol = new ColumnMetaData(canonicalTable, "name", false, Types.VARCHAR);
        final TableMetaData metaData = new TableMetaData(canonicalTable, List.of("id"), List.of(idCol, nameCol));
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(lookup);
        final FieldAccessor idField = cache.fieldAccessorOrThrow(TestPersonDto.class, "id");
        final FieldAccessor nameField = cache.fieldAccessorOrThrow(TestPersonDto.class, "name");
        final OrmTable ormTable = new OrmTable(
                TestPersonDto.class,
                metaData,
                Map.of(idField, idCol, nameField, nameCol),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(TestPersonDto.class, ormTable);

        final DtoMapper dtoMapper = createDtoMapper(tableRegistry);

        // RowColumn has an unqualified Table (catalog=null, schema=null) as returned by Postgres JDBC metadata
        final Table unqualifiedTable = new Table(null, null, "test_person");
        final Row row = new Row(List.of(
                new RowColumn("id", 1L, new Column(unqualifiedTable, "id")),
                new RowColumn("name", "Alice", new Column(unqualifiedTable, "name"))
        ));

        // When
        final List<TestPersonDto> result = dtoMapper.toDtos(TestPersonDto.class, null, List.of(row));

        // Then
        assertEquals(1, result.size());
        final TestPersonDto person = result.getFirst();
        assertEquals(1L, person.id);
        assertEquals("Alice", person.name);
    }

    @Test
    void toDtos_plainColumnNameWithoutTable() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final Table canonicalTable = new Table("", "public", "test_person");
        final ColumnMetaData idCol = new ColumnMetaData(canonicalTable, "id", false, Types.BIGINT);
        final ColumnMetaData nameCol = new ColumnMetaData(canonicalTable, "name", false, Types.VARCHAR);
        final TableMetaData metaData = new TableMetaData(canonicalTable, List.of("id"), List.of(idCol, nameCol));
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(lookup);
        final FieldAccessor idField = cache.fieldAccessorOrThrow(TestPersonDto.class, "id");
        final FieldAccessor nameField = cache.fieldAccessorOrThrow(TestPersonDto.class, "name");
        final OrmTable ormTable = new OrmTable(
                TestPersonDto.class,
                metaData,
                Map.of(idField, idCol, nameField, nameCol),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(TestPersonDto.class, ormTable);

        final DtoMapper dtoMapper = createDtoMapper(tableRegistry);

        // RowColumn has null column or Column with no table as returned by Oracle JDBC metadata
        final Row row = new Row(List.of(
                new RowColumn("id", 2L, null),
                new RowColumn("name", "Bob", new Column(null, "name"))
        ));

        // When
        final List<TestPersonDto> result = dtoMapper.toDtos(TestPersonDto.class, null, List.of(row));

        // Then
        assertEquals(1, result.size());
        final TestPersonDto person = result.getFirst();
        assertEquals(2L, person.id);
        assertEquals("Bob", person.name);
    }

    @Test
    void toDtos_unregisteredDtoThrows() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final DtoMapper dtoMapper = createDtoMapper(tableRegistry);
        final Row row = new Row(List.of(
                new RowColumn("id", 1L, null)
        ));

        // When / Then
        assertThrows(NullPointerException.class, () -> dtoMapper.toDtos(TestPersonDto.class, null, List.of(row)));
    }

    @Test
    void toDtos_unknownColumnLabelThrows() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final Table canonicalTable = new Table("", "public", "test_person");
        final ColumnMetaData idCol = new ColumnMetaData(canonicalTable, "id", false, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(canonicalTable, List.of("id"), List.of(idCol));
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(lookup);
        final FieldAccessor idField = cache.fieldAccessorOrThrow(TestPersonDto.class, "id");
        final OrmTable ormTable = new OrmTable(
                TestPersonDto.class,
                metaData,
                Map.of(idField, idCol),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(TestPersonDto.class, ormTable);

        final DtoMapper dtoMapper = createDtoMapper(tableRegistry);

        final Row row = new Row(List.of(
                new RowColumn("non_existent_column", "value", null)
        ));

        // When / Then
        assertThrows(IllegalStateException.class, () -> dtoMapper.toDtos(TestPersonDto.class, null, List.of(row)));
    }

    private static DtoMapper createDtoMapper(final TableRegistry tableRegistry) {
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final TypeConverter typeConverter = mock(TypeConverter.class);
        when(typeConverter.convert(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(context.tableRegistry()).thenReturn(tableRegistry);
        when(context.typeConverter()).thenReturn(typeConverter);

        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        return new DtoMapper(dtoConstructor, context);
    }

    public static class TestPersonDto {
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }
}

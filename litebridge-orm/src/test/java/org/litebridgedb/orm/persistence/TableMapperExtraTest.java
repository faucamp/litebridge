package org.litebridgedb.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.orm.api.spec.ColumnSpec;
import org.litebridgedb.orm.api.spec.FieldSpec;
import org.litebridgedb.orm.api.spec.ManyToMany;
import org.litebridgedb.orm.api.spec.OneToMany;
import org.litebridgedb.orm.api.spec.TableSpec;
import org.litebridgedb.tracking.ChangeTracker;

import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TableMapperExtraTest {

    @Test
    void mapToTable_noFieldColumnMap() {
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMapper mapper = new TableMapper(databaseProvider, tableRegistry, changeTracker);

        final TableSpec tableSpec = mock(TableSpec.class);
        when(tableSpec.fieldColumnMap()).thenReturn(Collections.emptyMap());

        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), TestDto.class, tableSpec, Collections.emptySet()));
    }

    @Test
    void mapToTable_unmappedNonNullableColumn() throws SQLException {
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMapper mapper = new TableMapper(databaseProvider, tableRegistry, changeTracker);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final ColumnMetaData nameCol = new ColumnMetaData(table, "NAME", false, Types.VARCHAR);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol, nameCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(new FieldSpec("id", false), new ColumnSpec("ID")));

        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), TestDto.class, tableSpec, Collections.emptySet()));
    }

    @Test
    void mapToTable_columnDoesNotExist() throws SQLException {
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMapper mapper = new TableMapper(databaseProvider, tableRegistry, changeTracker);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(new FieldSpec("id", false), new ColumnSpec("ID"), new FieldSpec("name", false), new ColumnSpec("MISSING")));

        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), TestDto.class, tableSpec, Collections.emptySet()));
    }

    @Test
    void mapToTable_columnAlreadyMapped() throws SQLException {
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMapper mapper = new TableMapper(databaseProvider, tableRegistry, changeTracker);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(new FieldSpec("id", false), new ColumnSpec("ID"), new FieldSpec("otherId", false), new ColumnSpec("ID")));

        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), TestDto.class, tableSpec, Collections.emptySet()));
    }

    @Test
    void mapToTable_referencedDtoNotRegistered() throws SQLException {
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMapper mapper = new TableMapper(databaseProvider, tableRegistry, changeTracker);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final ColumnMetaData refIdCol = new ColumnMetaData(table, "REF_ID", true, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol, refIdCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(
                new FieldSpec("id", false), new ColumnSpec("ID"),
                new FieldSpec("ref", false), new ColumnSpec("REF_ID", null, "id")
        ));

        when(tableRegistry.containsTable(ReferencedDto.class)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), DtoWithRef.class, tableSpec, Collections.emptySet()));
    }

    @Test
    void mapToTable_referencedDtoNoJoinOn() throws SQLException {
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMapper mapper = new TableMapper(databaseProvider, tableRegistry, changeTracker);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final ColumnMetaData refIdCol = new ColumnMetaData(table, "REF_ID", true, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol, refIdCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(
                new FieldSpec("id", false), new ColumnSpec("ID"),
                new FieldSpec("ref", false), new ColumnSpec("REF_ID") // Missing joinOn
        ));

        when(tableRegistry.containsTable(ReferencedDto.class)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), DtoWithRef.class, tableSpec, Collections.emptySet()));
    }

    @Test
    void mapToTable_oneToManyNotACollection() throws SQLException {
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMapper mapper = new TableMapper(databaseProvider, tableRegistry, changeTracker);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(
                new FieldSpec("id", false), new OneToMany(new FieldSpec("id", false)) // 'id' is not a collection
        ));

        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), TestDto.class, tableSpec, Collections.emptySet()));
    }

    @Test
    void mapToTable_oneToManyBasicTypeCollection() throws SQLException {
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMapper mapper = new TableMapper(databaseProvider, tableRegistry, changeTracker);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(
                new FieldSpec("id", false), new ColumnSpec("ID"),
                new FieldSpec("tags", false), new OneToMany(new FieldSpec("id", false)) // 'tags' is List<String>
        ));

        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), DtoWithBasicCollection.class, tableSpec, Collections.emptySet()));
    }

    @Test
    void mapToTable_manyToManyNotACollection() throws SQLException {
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMapper mapper = new TableMapper(databaseProvider, tableRegistry, changeTracker);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol));

        final Table joinTable = new Table("", "public", "join_table");
        final ColumnMetaData joinCol = new ColumnMetaData(joinTable, "join_col", false, Types.BIGINT);
        final ColumnMetaData invJoinCol = new ColumnMetaData(joinTable, "inv_join_col", false, Types.BIGINT);
        final TableMetaData joinMetaData = new TableMetaData(joinTable, List.of("join_col", "inv_join_col"), List.of(joinCol, invJoinCol));

        when(databaseProvider.tableMetaData(any(), any())).thenAnswer(invocation -> {
            TableSpec spec = invocation.getArgument(0);
            if (spec.name().equals("TEST")) return metaData;
            if (spec.name().equals("join_table")) return joinMetaData;
            return null;
        });

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(
                new FieldSpec("id", false), new ManyToMany("join_table", "join_col", "inv_join_col")
        ));

        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), TestDto.class, tableSpec, Collections.emptySet()));
    }

    public static class TestDto {
        private Long id;
        private String name;
        private Long otherId;
    }

    public static class ReferencedDto {
        private Long id;
    }

    public static class DtoWithRef {
        private Long id;
        private ReferencedDto ref;
    }

    public static class DtoWithBasicCollection {
        private Long id;
        private List<String> tags;
    }
}

package org.litebridge.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.generator.ColumnValueGenerator;
import org.litebridge.orm.api.spec.ColumnSpec;
import org.litebridge.orm.api.spec.FieldSpec;
import org.litebridge.orm.api.spec.ManyToMany;
import org.litebridge.orm.api.spec.MultiColumnSpec;
import org.litebridge.orm.api.spec.NoFieldMapping;
import org.litebridge.orm.api.spec.OneToMany;
import org.litebridge.orm.api.spec.TableMapping;
import org.litebridge.orm.api.spec.TableSpec;
import org.litebridge.tracking.ChangeTracker;

import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.sql.Types;
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
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final TableSpec tableSpec = mock(TableSpec.class);
        when(tableSpec.fieldColumnMap()).thenReturn(Collections.emptyMap());

        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), TestDto.class, null, tableSpec, Collections.emptySet()));
    }

    @Test
    void mapToTable_unmappedNonNullableColumn() throws SQLException {
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final ColumnMetaData nameCol = new ColumnMetaData(table, "NAME", false, Types.VARCHAR);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol, nameCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(new FieldSpec("id", false), new ColumnSpec("ID")));

        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), TestDto.class, null, tableSpec, Collections.emptySet()));
    }

    @Test
    void mapToTable_columnDoesNotExist() throws SQLException {
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(new FieldSpec("id", false), new ColumnSpec("ID"), new FieldSpec("name", false), new ColumnSpec("MISSING")));

        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), TestDto.class, null, tableSpec, Collections.emptySet()));
    }

    @Test
    void mapToTable_columnAlreadyMapped() throws SQLException {
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(new FieldSpec("id", false), new ColumnSpec("ID"), new FieldSpec("otherId", false), new ColumnSpec("ID")));

        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), TestDto.class, null, tableSpec, Collections.emptySet()));
    }

    @Test
    void mapToTable_referencedDtoNotRegistered() throws SQLException {
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final ColumnMetaData refIdCol = new ColumnMetaData(table, "REF_ID", true, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol, refIdCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(
                new FieldSpec("id", false), new ColumnSpec("ID"),
                new FieldSpec("ref", false), new ColumnSpec("REF_ID", null, "id")
        ));

        when(tableRegistry.containsOrmTable(ReferencedDto.class)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), DtoWithRef.class, null, tableSpec, Collections.emptySet()));
    }

    @Test
    void mapToTable_referencedDtoNoJoinOn() throws SQLException {
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final ColumnMetaData refIdCol = new ColumnMetaData(table, "REF_ID", true, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol, refIdCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(
                new FieldSpec("id", false), new ColumnSpec("ID"),
                new FieldSpec("ref", false), new ColumnSpec("REF_ID") // Missing joinOn
        ));

        when(tableRegistry.containsOrmTable(ReferencedDto.class)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), DtoWithRef.class, null, tableSpec, Collections.emptySet()));
    }

    @Test
    void mapToTable_oneToManyNotACollection() throws SQLException {
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(
                new FieldSpec("id", false), new OneToMany(new FieldSpec("id", false)) // 'id' is not a collection
        ));

        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), TestDto.class, null, tableSpec, Collections.emptySet()));
    }

    @Test
    void mapToTable_oneToManyBasicTypeCollection() throws SQLException {
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(
                new FieldSpec("id", false), new ColumnSpec("ID"),
                new FieldSpec("tags", false), new OneToMany(new FieldSpec("id", false)) // 'tags' is List<String>
        ));

        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), DtoWithBasicCollection.class, null, tableSpec, Collections.emptySet()));
    }

    @Test
    void mapToTable_manyToManyNotACollection() throws SQLException {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol));

        final Table joinTable = new Table("", "public", "join_table");
        final ColumnMetaData joinCol = new ColumnMetaData(joinTable, "join_col", false, Types.BIGINT);
        final ColumnMetaData invJoinCol = new ColumnMetaData(joinTable, "inv_join_col", false, Types.BIGINT);
        final TableMetaData joinMetaData = new TableMetaData(joinTable, List.of("join_col", "inv_join_col"), List.of(joinCol, invJoinCol));

        when(databaseProvider.tableMetaData(any(), any())).thenAnswer(invocation -> {
            final Table tableArg = invocation.getArgument(0);
            if (tableArg.name().equals("TEST")) return metaData;
            if (tableArg.name().equals("join_table")) return joinMetaData;
            return null;
        });

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(
                new FieldSpec("id", false), new ManyToMany("join_table", new String[]{"join_col"}, new String[]{"inv_join_col"})
        ));

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), TestDto.class, null, tableSpec, Collections.emptySet()));
    }

    @Test
    void mapToTable_columnDoesNotExist_propertyAccessor() throws SQLException {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(
                new FieldSpec("id", true), new ColumnSpec("MISSING")
        ));

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), PropertyDto.class, null, tableSpec, Collections.emptySet()));
    }

    @Test
    void mapToTable_columnDoesNotExist_nonFieldSpec() throws SQLException {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(
                new org.litebridge.orm.api.spec.NoFieldMapping(), new ColumnSpec("MISSING")
        ));

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), TestDto.class, null, tableSpec, Collections.emptySet()));
    }

    @Test
    void mapToTable_withColumnGenerator() throws SQLException {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", true, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final ColumnValueGenerator mockGenerator = mock(ColumnValueGenerator.class);
        final TableSpec tableSpec = new TableSpec("TEST", Map.of(
                new FieldSpec("id", false), new ColumnSpec("ID", mockGenerator, null)
        ));

        // When
        final TableMapper.MappedTable result = mapper.mapToTable(MethodHandles.lookup(), TestDto.class, null, tableSpec, Set.of(TestDto.class));

        // Then
        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals(mockGenerator, idCol.getGenerator());
    }

    @Test
    void mapToTable_withPropertyAccessor() throws SQLException {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(
                new FieldSpec("id", true), new ColumnSpec("ID")
        ));

        // When
        final TableMapper.MappedTable result = mapper.mapToTable(MethodHandles.lookup(), PropertyDto.class, null, tableSpec, Set.of(PropertyDto.class));

        // Then
        org.junit.jupiter.api.Assertions.assertNotNull(result);
    }

    @Test
    void mapToTable_withSelfReferencingDto() throws SQLException {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "SELF_REF");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final ColumnMetaData parentIdCol = new ColumnMetaData(table, "PARENT_ID", true, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol, parentIdCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final TableSpec tableSpec = new TableSpec("SELF_REF", Map.of(
                new FieldSpec("id", false), new ColumnSpec("ID"),
                new FieldSpec("parent", false), new ColumnSpec("PARENT_ID", null, "ID")
        ));

        final OrmTable ormTableMock = mock(OrmTable.class);
        when(ormTableMock.getMetaData()).thenReturn(metaData);
        when(tableRegistry.getOrmTableOrThrow(SelfRefDto.class)).thenReturn(ormTableMock);

        // When
        final TableMapper.MappedTable result = mapper.mapToTable(MethodHandles.lookup(), SelfRefDto.class, null, tableSpec, Set.of(SelfRefDto.class));

        // Then
        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals(idCol, parentIdCol.getJoinColumn());
    }

    @Test
    void mapToTable_withInlineTableSpec_success() throws SQLException {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table parentTable = new Table("", "public", "PARENT");
        final ColumnMetaData parentIdCol = new ColumnMetaData(parentTable, "ID", false, Types.BIGINT);
        final ColumnMetaData refIdCol = new ColumnMetaData(parentTable, "REF_ID", true, Types.BIGINT);
        final TableMetaData parentMetaData = new TableMetaData(parentTable, List.of("ID"), List.of(parentIdCol, refIdCol));

        final Table childTable = new Table("", "public", "CHILD");
        final ColumnMetaData childIdCol = new ColumnMetaData(childTable, "ID", false, Types.BIGINT);
        final TableMetaData childMetaData = new TableMetaData(childTable, List.of("ID"), List.of(childIdCol));

        when(databaseProvider.tableMetaData(any(), any())).thenAnswer(inv -> {
            final Table t = inv.getArgument(0);
            if (t.name().equals("PARENT")) return parentMetaData;
            if (t.name().equals("CHILD")) return childMetaData;
            return null;
        });

        final TableSpec childTableSpec = new TableSpec("CHILD", Map.of(
                new FieldSpec("id", false), new ColumnSpec("ID")
        ));
        final TableMapping inlineSpec = new TableMapping(MethodHandles.lookup(), ReferencedDto.class, childTableSpec);

        final TableSpec parentTableSpec = new TableSpec("PARENT", Map.of(
                new FieldSpec("id", false), new ColumnSpec("ID"),
                new FieldSpec("ref", false), new ColumnSpec("REF_ID", null, "ID", inlineSpec)
        ));

        // When
        final TableMapper.MappedTable result = mapper.mapToTable(MethodHandles.lookup(), DtoWithRef.class, null, parentTableSpec, Set.of(DtoWithRef.class));

        // Then
        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals(childIdCol, refIdCol.getJoinColumn());
    }

    @Test
    void mapToTable_withInlineTableSpec_failure() throws SQLException {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table parentTable = new Table("", "public", "PARENT");
        final ColumnMetaData parentIdCol = new ColumnMetaData(parentTable, "ID", false, Types.BIGINT);
        final ColumnMetaData refIdCol = new ColumnMetaData(parentTable, "REF_ID", true, Types.BIGINT);
        final TableMetaData parentMetaData = new TableMetaData(parentTable, List.of("ID"), List.of(parentIdCol, refIdCol));

        when(databaseProvider.tableMetaData(any(), any())).thenAnswer(inv -> {
            final Table t = inv.getArgument(0);
            if (t.name().equals("PARENT")) return parentMetaData;
            throw new SQLException("Table not found: " + t.name());
        });

        final TableSpec childTableSpec = new TableSpec("CHILD", Map.of(
                new FieldSpec("id", false), new ColumnSpec("ID")
        ));
        final TableMapping inlineSpec = new TableMapping(MethodHandles.lookup(), ReferencedDto.class, childTableSpec);

        final TableSpec parentTableSpec = new TableSpec("PARENT", Map.of(
                new FieldSpec("id", false), new ColumnSpec("ID"),
                new FieldSpec("ref", false), new ColumnSpec("REF_ID", null, "ID", inlineSpec)
        ));

        // When / Then
        assertThrows(IllegalStateException.class, () -> mapper.mapToTable(MethodHandles.lookup(), DtoWithRef.class, null, parentTableSpec, Set.of(DtoWithRef.class)));
    }

    @Test
    void mapToTable_withMultiColumnSpec_dtoTarget() throws SQLException {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "COMPOSITE_TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final ColumnMetaData ref1Col = new ColumnMetaData(table, "REF_A", false, Types.BIGINT);
        final ColumnMetaData ref2Col = new ColumnMetaData(table, "REF_B", false, Types.VARCHAR);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol, ref1Col, ref2Col));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final Table refTable = new Table("", "public", "REF_TABLE");
        final ColumnMetaData pk1Col = new ColumnMetaData(refTable, "PK_A", false, Types.BIGINT);
        final ColumnMetaData pk2Col = new ColumnMetaData(refTable, "PK_B", false, Types.VARCHAR);
        final TableMetaData refMetaData = new TableMetaData(refTable, List.of("PK_A", "PK_B"), List.of(pk1Col, pk2Col));

        final OrmTable refOrmTable = mock(OrmTable.class);
        when(refOrmTable.getMetaData()).thenReturn(refMetaData);
        when(tableRegistry.getOrmTableOrThrow(ReferencedDto.class)).thenReturn(refOrmTable);
        when(tableRegistry.containsOrmTable(ReferencedDto.class)).thenReturn(true);

        final MultiColumnSpec multiColumnSpec = new MultiColumnSpec(new ColumnSpec[]{
                new ColumnSpec("REF_A", null, "PK_A"),
                new ColumnSpec("REF_B", null, "PK_B")
        });

        final TableSpec tableSpec = new TableSpec("COMPOSITE_TEST", Map.of(
                new FieldSpec("id", false), new ColumnSpec("ID"),
                new FieldSpec("ref", false), multiColumnSpec
        ));

        // When
        final TableMapper.MappedTable result = mapper.mapToTable(MethodHandles.lookup(), DtoWithRef.class, null, tableSpec, Set.of(DtoWithRef.class, ReferencedDto.class));

        // Then
        org.junit.jupiter.api.Assertions.assertNotNull(result);
    }

    @Test
    void mapToTable_withMultiColumnSpec_basicTypeAndNoFieldMapping() throws SQLException {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "COMPOSITE_TEST");
        final ColumnMetaData col1 = new ColumnMetaData(table, "COL_1", false, Types.BIGINT);
        final ColumnMetaData col2 = new ColumnMetaData(table, "COL_2", false, Types.VARCHAR);
        final TableMetaData metaData = new TableMetaData(table, List.of("COL_1", "COL_2"), List.of(col1, col2));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final MultiColumnSpec multiColumnSpec = new MultiColumnSpec(new ColumnSpec[]{
                new ColumnSpec("COL_1"),
                new ColumnSpec("COL_2")
        });

        final TableSpec tableSpec = new TableSpec("COMPOSITE_TEST", Map.of(
                new NoFieldMapping(), multiColumnSpec
        ));

        // When
        final TableMapper.MappedTable result = mapper.mapToTable(MethodHandles.lookup(), TestDto.class, null, tableSpec, Set.of(TestDto.class));

        // Then
        org.junit.jupiter.api.Assertions.assertNotNull(result);
    }

    @Test
    void mapToTable_noOpFieldAccessor_withJoinColumn() throws SQLException {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final ColumnMetaData fkCol = new ColumnMetaData(table, "FK_ID", true, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol, fkCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(tableRegistry.getOrmTableOrThrow(TestDto.class)).thenReturn(ormTable);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(
                new FieldSpec("id", false), new ColumnSpec("ID"),
                new NoFieldMapping(), new ColumnSpec("FK_ID", null, "ID")
        ));

        // When
        final TableMapper.MappedTable result = mapper.mapToTable(MethodHandles.lookup(), TestDto.class, null, tableSpec, Set.of(TestDto.class));

        // Then
        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals(idCol, fkCol.getJoinColumn());
    }

    @Test
    void mapToTable_manyToManyJoinTableMappingException() throws SQLException {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol));

        when(databaseProvider.tableMetaData(any(), any())).thenAnswer(invocation -> {
            final Table tableArg = invocation.getArgument(0);
            if (tableArg.name().equals("TEST")) return metaData;
            throw new SQLException("Join table error");
        });

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(
                new FieldSpec("id", false), new ColumnSpec("ID"),
                new FieldSpec("orders", false), new ManyToMany("non_existent_join_table", new String[]{"cust_id"}, new String[]{"order_id"})
        ));

        // When / Then
        assertThrows(IllegalStateException.class, () -> mapper.mapToTable(MethodHandles.lookup(), CustomerDto.class, null, tableSpec, Collections.emptySet()));
    }

    public enum TestEnum {
        VALUE
    }

    @Test
    void mapToTable_basicTypeEnum_throwsIllegalArgumentException() {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(new FieldSpec("name", false), new ColumnSpec("NAME")));

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), TestEnum.class, null, tableSpec, Set.of(TestEnum.class)));
    }

    @Test
    void mapToTable_withUnmappedNullableColumn_succeeds() throws SQLException {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final ColumnMetaData optionalCol = new ColumnMetaData(table, "OPTIONAL_DESC", true, Types.VARCHAR);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol, optionalCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(
                new FieldSpec("id", false), new ColumnSpec("ID")
        ));

        // When
        final TableMapper.MappedTable result = mapper.mapToTable(MethodHandles.lookup(), TestDto.class, null, tableSpec, Set.of(TestDto.class));

        // Then
        org.junit.jupiter.api.Assertions.assertNotNull(result);
    }

    @Test
    void mapToTable_referencedDtoInAllDtoClasses() throws SQLException {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final ColumnMetaData refIdCol = new ColumnMetaData(table, "REF_ID", true, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol, refIdCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(
                new FieldSpec("id", false), new ColumnSpec("ID"),
                new FieldSpec("ref", false), new ColumnSpec("REF_ID", null, "ID")
        ));

        when(tableRegistry.containsOrmTable(ReferencedDto.class)).thenReturn(false);

        // When - ReferencedDto is present in allDtoClasses
        final TableMapper.MappedTable result = mapper.mapToTable(MethodHandles.lookup(), DtoWithRef.class, null, tableSpec, Set.of(DtoWithRef.class, ReferencedDto.class));

        // Then
        org.junit.jupiter.api.Assertions.assertNotNull(result);
    }

    @Test
    void mapToTable_basicTypeField_withJoinColumn() throws SQLException {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final ColumnMetaData otherIdCol = new ColumnMetaData(table, "OTHER_ID", true, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol, otherIdCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(tableRegistry.getOrmTableOrThrow(Long.class)).thenReturn(ormTable);

        final TableSpec tableSpec = new TableSpec("TEST", Map.of(
                new FieldSpec("id", false), new ColumnSpec("ID"),
                new FieldSpec("otherId", false), new ColumnSpec("OTHER_ID", null, "ID")
        ));

        // When
        final TableMapper.MappedTable result = mapper.mapToTable(MethodHandles.lookup(), TestDto.class, null, tableSpec, Set.of(TestDto.class));

        // Then
        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals(idCol, otherIdCol.getJoinColumn());
    }

    @Test
    void mapToTable_withMultiColumnSpec_evaluatesTargetOrmTableSupplier() throws SQLException {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "COMPOSITE_TEST");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final ColumnMetaData ref1Col = new ColumnMetaData(table, "REF_A", false, Types.BIGINT);
        final ColumnMetaData ref2Col = new ColumnMetaData(table, "REF_B", false, Types.VARCHAR);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol, ref1Col, ref2Col));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final Table refTable = new Table("", "public", "REF_TABLE");
        final ColumnMetaData pk1Col = new ColumnMetaData(refTable, "PK_A", false, Types.BIGINT);
        final ColumnMetaData pk2Col = new ColumnMetaData(refTable, "PK_B", false, Types.VARCHAR);
        final TableMetaData refMetaData = new TableMetaData(refTable, List.of("PK_A", "PK_B"), List.of(pk1Col, pk2Col));

        final OrmTable refOrmTable = mock(OrmTable.class);
        when(refOrmTable.getMetaData()).thenReturn(refMetaData);
        when(tableRegistry.getOrmTableOrThrow(ReferencedDto.class)).thenReturn(refOrmTable);
        when(tableRegistry.containsOrmTable(ReferencedDto.class)).thenReturn(true);

        final MultiColumnSpec multiColumnSpec = new MultiColumnSpec(new ColumnSpec[]{
                new ColumnSpec("REF_A", null, "PK_A"),
                new ColumnSpec("REF_B", null, "PK_B")
        });

        final TableSpec tableSpec = new TableSpec("COMPOSITE_TEST", Map.of(
                new FieldSpec("id", false), new ColumnSpec("ID"),
                new FieldSpec("ref", false), multiColumnSpec
        ));

        // When
        final TableMapper.MappedTable result = mapper.mapToTable(MethodHandles.lookup(), DtoWithRef.class, null, tableSpec, Set.of(DtoWithRef.class, ReferencedDto.class));

        // Then
        org.junit.jupiter.api.Assertions.assertNotNull(result);
        final MappedCompositeKey mappedCompositeKey = (MappedCompositeKey) result.ormTable().mappedFieldTargetForField("ref");
        org.junit.jupiter.api.Assertions.assertNotNull(mappedCompositeKey.targetOrmTable());
        org.junit.jupiter.api.Assertions.assertEquals(refOrmTable, mappedCompositeKey.targetOrmTable().get());
    }

    @Test
    void mapToTable_manyToMany_withExistingJoinTableAndRightDtoSupplier() throws SQLException {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, mock(org.litebridge.db.spi.tx.TransactionManager.class));
        final TableMapper mapper = new TableMapper(tableRegistry, changeTracker, tableMetaDataCache);

        final Table table = new Table("", "public", "CUSTOMERS");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        final OrmTable existingJoinTable = mock(OrmTable.class);
        when(tableRegistry.getOrmTable("existing_join_table")).thenReturn(existingJoinTable);

        final OrmTable orderOrmTable = mock(OrmTable.class);
        when(tableRegistry.getOrmTableOrThrow(ReferencedDto.class)).thenReturn(orderOrmTable);

        final TableSpec tableSpec = new TableSpec("CUSTOMERS", Map.of(
                new FieldSpec("id", false), new ColumnSpec("ID"),
                new FieldSpec("orders", false), new ManyToMany("existing_join_table", new String[]{"cust_id"}, new String[]{"order_id"})
        ));

        // When
        final TableMapper.MappedTable result = mapper.mapToTable(MethodHandles.lookup(), CustomerDto.class, null, tableSpec, Set.of(CustomerDto.class, ReferencedDto.class));

        // Then
        org.junit.jupiter.api.Assertions.assertNotNull(result);
        final MappedManyToMany mappedManyToMany = (MappedManyToMany) result.ormTable().getManyToManyMappings().getFirst();
        org.junit.jupiter.api.Assertions.assertEquals(orderOrmTable, mappedManyToMany.targetOrmTable().get());
    }

    public static class TestDto {
        private Long id;
        private String name;
        private Long otherId;
    }

    public static class PropertyDto {
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(final Long id) {
            this.id = id;
        }
    }

    public static class SelfRefDto {
        private Long id;
        private SelfRefDto parent;
    }

    public static class CustomerDto {
        private Long id;
        private List<ReferencedDto> orders;
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

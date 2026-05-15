package org.litebridgedb.orm.api.dto.delete;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.api.spec.FieldSpec;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;
import org.litebridgedb.tracking.ChangeTracker;
import org.litebridgedb.tracking.ClassFieldAccessorCache;

import java.lang.invoke.MethodHandles;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DtoDeletorTest {

    @Test
    void where_string() {
        // Given
        OrmTable ormTable = ormTable();
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        TableRegistry tableRegistry = mock(TableRegistry.class);
        ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        DtoDeletor<TestDto> deletor = new DtoDeletor<>(TestDto.class, ormTable, tableRegistry, classFieldAccessorCache, databaseProvider);

        // When
        DtoDeleteWhereConditionClause<TestDto> result = deletor.where("id");

        // Then
        assertNotNull(result);
    }

    @Test
    void where_fieldSpec() {
        // Given
        OrmTable ormTable = ormTable();
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        TableRegistry tableRegistry = mock(TableRegistry.class);
        ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        DtoDeletor<TestDto> deletor = new DtoDeletor<>(TestDto.class, ormTable, tableRegistry, classFieldAccessorCache, databaseProvider);

        FieldColumnSpec fieldColumnSpec = mock(FieldColumnSpec.class);
        FieldSpec fieldSpec = mock(FieldSpec.class);
        when(fieldColumnSpec.field()).thenReturn(fieldSpec);
        when(fieldSpec.name()).thenReturn("id");

        // When
        DtoDeleteWhereConditionClause<TestDto> result = deletor.where(fieldColumnSpec);

        // Then
        assertNotNull(result);
    }

    private static OrmTable ormTable() {
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final Table table = new Table("", "public", "test_table");
        final ColumnMetaData idColumn = new ColumnMetaData(table, "id", false, Types.BIGINT);
        final TableMetaData tableMetaData = new TableMetaData(table, List.of("id"), List.of(idColumn));
        final org.litebridgedb.tracking.FieldAccessor idField = changeTracker.classFieldAccessorCache().fieldAccessor(TestDto.class, "id");
        return new OrmTable(TestDto.class, tableMetaData, Map.of(idField, idColumn), changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
    }

    private static class TestDto {
        private Long id;
    }
}

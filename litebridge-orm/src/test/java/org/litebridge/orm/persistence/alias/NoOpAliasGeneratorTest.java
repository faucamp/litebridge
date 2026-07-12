package org.litebridge.orm.persistence.alias;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.DirectFieldAccessor;
import org.litebridge.tracking.FieldAccessor;

import java.lang.invoke.MethodHandles;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NoOpAliasGeneratorTest {

    @Test
    void aliasTable() {
        // Given
        final NoOpAliasGenerator noOpAliasGenerator = new NoOpAliasGenerator();

        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));

        // When
        final Table result = noOpAliasGenerator.aliasTable(ormTable);

        // Then
        assertEquals(result, table);
        assertNull(result.alias());
    }

    @Test
    void aliasColumn() {
        // Given
        final NoOpAliasGenerator noOpAliasGenerator = new NoOpAliasGenerator();

        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final Table aliasedTable = table.as("tt");

        // When
        final Column result = noOpAliasGenerator.aliasColumn(aliasedTable, columnMetaData);

        // Then
        assertEquals(columnMetaData.name(), result.name());
        assertNull(result.alias());
    }

    private static class TestDto {
        private @Nullable Long id;
        private @Nullable String myVar;
    }
}
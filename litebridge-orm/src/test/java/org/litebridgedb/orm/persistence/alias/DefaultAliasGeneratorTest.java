package org.litebridgedb.orm.persistence.alias;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.litebridgedb.commons.ClassUtils;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.db.spi.MappedFieldTarget;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.tracking.ChangeTracker;
import org.litebridgedb.tracking.ClassFieldAccessorCache;
import org.litebridgedb.tracking.DirectFieldAccessor;
import org.litebridgedb.tracking.FieldAccessor;

import java.lang.invoke.MethodHandles;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultAliasGeneratorTest {

    @Test
    void aliasTable() {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        when(databaseProvider.transformAlias(anyString())).then(i -> i.getArgument(0));
        final DefaultAliasGenerator defaultAliasGenerator = new DefaultAliasGenerator(databaseProvider);

        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));

        // When
        final Table result = defaultAliasGenerator.aliasTable(ormTable);

        // Then
        assertNotEquals(result, table);
        assertTrue(result.equalsIgnoreAlias(table));
        assertEquals("tt", result.alias());
    }

    @Test
    void aliasColumn() {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        when(databaseProvider.transformAlias(anyString())).then(i -> i.getArgument(0));
        final DefaultAliasGenerator defaultAliasGenerator = new DefaultAliasGenerator(databaseProvider);

        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final Table aliasedTable = table.as("tt");

        // When
        final Column result = defaultAliasGenerator.aliasColumn(aliasedTable, columnMetaData);

        // Then
        assertEquals(columnMetaData.name(), result.name());
        assertEquals("ttmv", result.alias());

        // When 2
        final Column result2 = defaultAliasGenerator.aliasColumn(aliasedTable, columnMetaData);

        // Then 2
        assertEquals(columnMetaData.name(), result2.name());
        assertEquals("ttmv1", result2.alias());
    }

    private static class TestDto {
        private @Nullable Long id;
        private @Nullable String myVar;
    }
}
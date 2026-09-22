package org.litebridge.orm.persistence.alias;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.alias.DefaultAliasTransformer;
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
    void newTableAlias() {
        // Given
        final NoOpAliasGenerator defaultAliasGenerator = new NoOpAliasGenerator();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final String result = defaultAliasGenerator.newTableAlias(table);

        // Then
        assertEquals(table.name(), result);
    }

    @Test
    void newColumnAlias() {
        // Given
        final DefaultAliasGenerator defaultAliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "MY_VAR");

        // When
        final String result = defaultAliasGenerator.newColumnAlias(column);

        // Then
        assertEquals(column.name(), result);

        // When 2
        final String result2 = defaultAliasGenerator.newColumnAlias(column);

        // Then 2
        assertEquals(column.name(), result2);
    }

    private static class TestDto {
        private @Nullable Long id;
        private @Nullable String myVar;
    }
}
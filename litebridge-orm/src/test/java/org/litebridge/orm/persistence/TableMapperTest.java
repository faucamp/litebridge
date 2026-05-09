package org.litebridge.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.api.spec.ColumnMapping;
import org.litebridge.orm.api.spec.FieldMapping;
import org.litebridge.orm.api.spec.TableSpec;
import org.litebridge.tracking.ChangeTracker;

import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TableMapperTest {

    @Test
    void mapToTable_basic() throws SQLException {
        // Given
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        TableRegistry tableRegistry = mock(TableRegistry.class);
        ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        TableMapper mapper = new TableMapper(databaseProvider, tableRegistry, changeTracker);

        TableSpec tableSpec = new TableSpec("test_table", Map.of(
                FieldMapping.field("id"), ColumnMapping.column("ID"),
                FieldMapping.field("name"), ColumnMapping.column("NAME")
        ));

        org.litebridge.db.spi.Table table = new org.litebridge.db.spi.Table("", "public", "test_table");
        ColumnMetaData idColumn = new ColumnMetaData(table, "ID", false, Types.BIGINT);
        ColumnMetaData nameColumn = new ColumnMetaData(table, "NAME", true, Types.VARCHAR);
        TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idColumn, nameColumn));

        when(databaseProvider.tableMetaData(any(), any())).thenReturn(metaData);

        // When
        TableMapper.MappedTable result = mapper.mapToTable(MethodHandles.lookup(), TestDto.class, tableSpec);

        // Then
        assertNotNull(result);
        assertNotNull(result.ormTable());
    }

    public static class TestDto {
        private Long id;
        private String name;
    }
}

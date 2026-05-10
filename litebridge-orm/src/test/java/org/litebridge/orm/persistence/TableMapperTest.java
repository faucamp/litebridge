package org.litebridge.orm.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.api.spec.TableSpec;
import org.litebridge.orm.Litebridge;
import org.litebridge.tracking.ChangeTracker;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.lang.invoke.MethodHandles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TableMapperTest {
    private DatabaseProvider databaseProvider;
    private Litebridge litebridge;
    private TableMapper tableMapper;

    @BeforeEach
    void setUp() throws SQLException {
        databaseProvider = mock(DatabaseProvider.class);
        DataSource dataSource = mock(DataSource.class);
        litebridge = new Litebridge(databaseProvider, dataSource);
        
        TableRegistry tableRegistry = (TableRegistry) org.litebridge.commons.ObjectUtils.getFieldValue(litebridge, "tableRegistry", TableRegistry.class);
        ChangeTracker changeTracker = (ChangeTracker) org.litebridge.commons.ObjectUtils.getFieldValue(litebridge, "changeTracker", ChangeTracker.class);
        TransactionalDatabaseProvider transactionalDatabaseProvider = (TransactionalDatabaseProvider) org.litebridge.commons.ObjectUtils.getFieldValue(litebridge, "databaseProvider", TransactionalDatabaseProvider.class);
        
        tableMapper = new TableMapper(transactionalDatabaseProvider, tableRegistry, changeTracker);
    }

    @Test
    void mapToTable() throws SQLException {
        // Given
        Table table = new Table(null, null, "TEST");
        ColumnMetaData idCol = new ColumnMetaData(table, "ID", true, Types.BIGINT, 19);
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(new TableMetaData(table, List.of("ID"), List.of(idCol)));
        
        TableSpec tableSpec = new TableSpec(null, null, "TEST", Map.of(new org.litebridge.orm.api.spec.FieldSpec("id", false), new org.litebridge.orm.api.spec.ColumnSpec("ID", false, null, null)));
        
        // When
        TableMapper.MappedTable mappedTable = tableMapper.mapToTable(MethodHandles.lookup(), TestDto.class, tableSpec);
        
        // Then
        assertNotNull(mappedTable);
        assertNotNull(mappedTable.ormTable());
    }

    public static class TestDto {
        Long id;
    }
}

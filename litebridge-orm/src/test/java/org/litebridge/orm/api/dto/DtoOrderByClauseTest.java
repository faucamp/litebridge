package org.litebridge.orm.api.dto;

import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.api.select.model.OrderBySpec;
import org.litebridge.orm.persistence.AliasGenerator;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.FieldAccessor;
import org.litebridge.tracking.FieldAccessorImpl;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class DtoOrderByClauseTest {

    @Test
    void asc() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker();
        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker);
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final AliasGenerator aliasGenerator = new AliasGenerator();
        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, databaseProvider, aliasGenerator);
        final OrderBySpec orderBySpec = new OrderBySpec(new String[]{"myVar"});

        final DtoOrderByClause<TestDto> dtoDtoOrderByClause = new DtoOrderByClause<>(orderBySpec, dtoSelector);

        // When
        final DtoOrderByClauseChain<TestDto> result = dtoDtoOrderByClause.asc();

        // Then
        assertNotNull(result);
    }

    @Test
    void desc() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker();
        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker);
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final AliasGenerator aliasGenerator = new AliasGenerator();
        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, databaseProvider, aliasGenerator);
        final OrderBySpec orderBySpec = new OrderBySpec(new String[]{"myVar"});

        final DtoOrderByClause<TestDto> dtoDtoOrderByClause = new DtoOrderByClause<>(orderBySpec, dtoSelector);

        // When
        final DtoOrderByClauseChain<TestDto> result = dtoDtoOrderByClause.desc();

        // Then
        assertNotNull(result);
    }

    private static class TestDto {
        private String myVar;
    }
}
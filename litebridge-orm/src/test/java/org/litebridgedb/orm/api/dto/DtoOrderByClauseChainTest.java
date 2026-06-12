package org.litebridgedb.orm.api.dto;

import org.junit.jupiter.api.Test;
import org.litebridgedb.commons.ClassUtils;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.MappedFieldTarget;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.orm.config.LitebridgeConfig;
import org.litebridgedb.orm.persistence.DtoConstructor;
import org.litebridgedb.orm.persistence.alias.AliasGenerator;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;
import org.litebridgedb.orm.persistence.alias.DefaultAliasGenerator;
import org.litebridgedb.tracking.ChangeTracker;
import org.litebridgedb.tracking.ClassFieldAccessorCache;
import org.litebridgedb.tracking.DirectFieldAccessor;
import org.litebridgedb.tracking.FieldAccessor;

import java.lang.invoke.MethodHandles;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class DtoOrderByClauseChainTest {

    @Test
    void then() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(databaseProvider);
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, databaseProvider, aliasGenerator, new LitebridgeConfig());

        final DtoOrderByClauseChain<TestDto> dtoDtoOrderByClauseChain = new DtoOrderByClauseChain<>(dtoSelector);

        // When
        final DtoOrderByClause<TestDto> result = dtoDtoOrderByClauseChain.then("myVar");

        // Then
        assertNotNull(result);
    }

    private static class TestDto {
        private String myVar;
    }
}
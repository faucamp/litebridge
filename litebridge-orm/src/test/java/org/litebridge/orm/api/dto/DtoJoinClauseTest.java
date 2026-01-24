package org.litebridge.orm.api.dto;

import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;
import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.orm.api.select.model.JoinSpec;
import org.litebridge.orm.persistence.DefaultDtoMapper;
import org.litebridge.orm.persistence.DtoAliasRegistry;
import org.litebridge.orm.persistence.DtoMapper;
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

class DtoJoinClauseTest {

    @Test
    void on() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker();
        final OrmTable ormTable = new OrmTable(tableMetaData, fieldColumnMap, changeTracker);
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TypeConverter typeConverter = new DefaultTypeConverter();
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final DtoMapper dtoMapper = new DefaultDtoMapper(tableRegistry, typeConverter, dtoAliasRegistry);
        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, databaseProvider, dtoMapper, dtoAliasRegistry);
        final JoinSpec joinSpec = new JoinSpec("TEST_SCHEMA", "TEST_TABLE");
        final DtoJoinClause<TestDto> dtoJoinClause = new DtoJoinClause<>(joinSpec, dtoSelector);

        // When
        final DtoJoinConditionClause<TestDto> result = dtoJoinClause.on("myVar");

        // Then
        assertNotNull(result);
    }

    @Test
    void using() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", List.of("MY_VAR"), List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker();
        final OrmTable ormTable = new OrmTable(tableMetaData, fieldColumnMap, changeTracker);
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TypeConverter typeConverter = new DefaultTypeConverter();
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final DtoMapper dtoMapper = new DefaultDtoMapper(tableRegistry, typeConverter, dtoAliasRegistry);
        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(TestDto.class, ormTable, tableRegistry, databaseProvider, dtoMapper, dtoAliasRegistry);
        final JoinSpec joinSpec = new JoinSpec("TEST_SCHEMA", "TEST_TABLE");
        final DtoJoinClause<TestDto> dtoJoinClause = new DtoJoinClause<>(joinSpec, dtoSelector);

        // When
        final DtoJoinConditionClauseTerminal<TestDto> result = dtoJoinClause.using("myVar");

        // Then
        assertNotNull(result);
    }

    private static class TestDto {
        private String myVar;
    }
}
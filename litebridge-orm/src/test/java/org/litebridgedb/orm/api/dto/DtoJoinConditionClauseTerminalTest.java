package org.litebridgedb.orm.api.dto;

import org.junit.jupiter.api.Test;
import org.litebridgedb.commons.ClassUtils;
import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.MappedFieldTarget;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.orm.api.select.model.SelectSpec;
import org.litebridgedb.orm.api.spec.ColumnSpec;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.api.spec.FieldSpec;
import org.litebridgedb.orm.config.LitebridgeConfig;
import org.litebridgedb.orm.persistence.DtoConstructor;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;
import org.litebridgedb.orm.persistence.alias.AliasGenerator;
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

class DtoJoinConditionClauseTerminalTest {

    @Test
    void and() {
        // Given
        final TestContext<TestDto> context = testContext();

        // When
        final DtoJoinConditionClause<TestDto> result = context.terminal().and("myVar");

        // Then
        assertNotNull(result);
    }

    @Test
    void where() {
        // Given
        final TestContext<TestDto> context = testContext();
        final SelectSpec selectSpec = ObjectUtils.getFieldValue(context.dtoSelector(), "selectSpec", SelectSpec.class);
        final Table aliasedTable = context.aliasGenerator().aliasTable(context.ormTable());
        selectSpec.setTable(aliasedTable);

        final DtoJoinConditionClauseTerminal<TestDto> terminal = new DtoJoinConditionClauseTerminal<>(
                new DtoJoinSpec(TestDto.class, context.ormTable(), aliasedTable),
                context.dtoSelector(),
                context.aliasGenerator());

        // When
        final DtoWhereConditionClause<TestDto> result = terminal.where("myVar");

        // Then
        assertNotNull(result);
    }

    @Test
    void orderBy() {
        // Given
        final TestContext<TestDto> context = testContext();

        // When
        final DtoOrderByClause<TestDto> result = context.terminal().orderBy("myVar");

        // Then
        assertNotNull(result);
    }

    @Test
    void orderBy_fieldColumnSpec() {
        // Given
        final TestContext<TestDto> context = testContext();
        final FieldColumnSpec fieldColumnSpec = new FieldColumnSpec(new FieldSpec("myVar", false), new ColumnSpec("MY_VAR"));

        // When
        final DtoOrderByClause<TestDto> result = context.terminal().orderBy(fieldColumnSpec);

        // Then
        assertNotNull(result);
    }

    private static TestContext<TestDto> testContext() {
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "MY_VAR", false, Types.VARCHAR);
        final TableMetaData tableMetaData = new TableMetaData(
                "TEST_CATALOG",
                "TEST_SCHEMA",
                "TEST_TABLE",
                List.of("MY_VAR"),
                List.of(columnMetaData));
        final FieldAccessor fieldAccessor = new DirectFieldAccessor(
                ClassUtils.getField(TestDto.class, "myVar"),
                MethodHandles.lookup());
        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = Map.of(fieldAccessor, columnMetaData);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final OrmTable ormTable = new OrmTable(TestDto.class, tableMetaData, fieldColumnMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable);
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(databaseProvider);
        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(
                TestDto.class,
                ormTable,
                tableRegistry,
                changeTracker.classFieldAccessorCache(),
                dtoConstructor,
                databaseProvider,
                aliasGenerator,
                new LitebridgeConfig());
        final DtoJoinSpec joinSpec = new DtoJoinSpec(TestDto.class, ormTable, aliasGenerator.aliasTable(ormTable));
        final DtoJoinConditionClauseTerminal<TestDto> terminal = new DtoJoinConditionClauseTerminal<>(
                joinSpec,
                dtoSelector,
                aliasGenerator);

        return new TestContext<>(ormTable, aliasGenerator, dtoSelector, terminal);
    }

    private record TestContext<DTO>(
            OrmTable ormTable,
            AliasGenerator aliasGenerator,
            DtoSelector<DTO> dtoSelector,
            DtoJoinConditionClauseTerminal<DTO> terminal) {
    }

    private static class TestDto {
        private String myVar;
    }
}
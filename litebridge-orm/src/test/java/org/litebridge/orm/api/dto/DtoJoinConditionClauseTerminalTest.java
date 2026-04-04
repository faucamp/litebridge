package org.litebridge.orm.api.dto;

import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.api.spec.FieldColumnMapping;
import org.litebridge.orm.api.spec.FieldColumnSpec;
import org.litebridge.orm.persistence.AliasGenerator;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.DirectFieldAccessor;
import org.litebridge.tracking.FieldAccessor;

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
        final FieldColumnSpec fieldColumnSpec = FieldColumnMapping.f("myVar").c("MY_VAR");

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
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final AliasGenerator aliasGenerator = new AliasGenerator();
        final DtoSelector<TestDto> dtoSelector = new DtoSelector<>(
                TestDto.class,
                ormTable,
                tableRegistry,
                changeTracker.classFieldAccessorCache(),
                databaseProvider,
                aliasGenerator);
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
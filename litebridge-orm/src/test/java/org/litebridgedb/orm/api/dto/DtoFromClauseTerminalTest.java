package org.litebridgedb.orm.api.dto;

import org.junit.jupiter.api.Test;
import org.litebridgedb.commons.ClassUtils;
import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.convert.DefaultTypeConverter;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.db.spi.MappedFieldTarget;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.alias.DefaultAliasTransformer;
import org.litebridgedb.db.spi.function.SqlFunctionRegistry;
import org.litebridgedb.db.spi.query.Select;
import org.litebridgedb.db.spi.tx.ConnectionProvider;
import org.litebridgedb.db.spi.tx.TransactionManager;
import org.litebridgedb.orm.api.select.model.SelectSpec;
import org.litebridgedb.orm.api.spec.ColumnSpec;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.api.spec.FieldSpec;
import org.litebridgedb.orm.config.LitebridgeConfig;
import org.litebridgedb.orm.function.TestColumnExpressionFactory;
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
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DtoFromClauseTerminalTest {

    @Test
    void join() {
        // Given
        final TestContext<TestDto> context = testContext(TestDto.class, List.of("MY_VAR"), "myVar");
        final TestContext<JoinDto> joinContext = testContext(JoinDto.class, List.of("JOIN_VAR"), "joinVar");
        context.tableRegistry.addTable(JoinDto.class, joinContext.ormTable);
        final DtoFromClauseTerminal<TestDto> dtoFromClauseTerminal = new DtoFromClauseTerminal<>(context.dtoSelector);

        // When
        final DtoJoinClause<TestDto> result = dtoFromClauseTerminal.join(JoinDto.class);

        // Then
        assertNotNull(result);
    }

    @Test
    void join_contextScopedTable() {
        // Given
        final TestContext<TestDto> context = testContext(TestDto.class, List.of("MY_VAR"), "myVar");
        final TestContext<JoinDto> joinContext = testContext(JoinDto.class, List.of("JOIN_VAR"), "joinVar");
        context.ormTable.getContextTableRegistry().addTable(JoinDto.class, joinContext.ormTable);
        final DtoFromClauseTerminal<TestDto> dtoFromClauseTerminal = new DtoFromClauseTerminal<>(context.dtoSelector);

        // When
        final DtoJoinClause<TestDto> result = dtoFromClauseTerminal.join(JoinDto.class);

        // Then
        assertNotNull(result);
    }

    @Test
    void where() {
        // Given
        final TestContext<TestDto> context = testContext(TestDto.class, List.of("MY_VAR"), "myVar");
        final SelectSpec selectSpec = ObjectUtils.getFieldValue(context.dtoSelector, "selectSpec", SelectSpec.class);
        selectSpec.setTable(context.aliasGenerator.aliasTable(context.ormTable));

        final DtoFromClauseTerminal<TestDto> dtoFromClauseTerminal = new DtoFromClauseTerminal<>(context.dtoSelector);

        // When
        final DtoWhereConditionClause<TestDto> result = dtoFromClauseTerminal.where("myVar");

        // Then
        assertNotNull(result);
    }

    @Test
    void where_usesSelectedColumnAliasWhenSelectedColumnMatches() {
        // Given
        final TestContext<TestDto> context = testContext(TestDto.class, List.of("MY_VAR"), "myVar");
        final DtoFromClauseTerminal<TestDto> dtoFromClauseTerminal = context.dtoSelector.select("myVar");

        // When
        final DtoWhereConditionClause<TestDto> result = dtoFromClauseTerminal.where("myVar");

        // Then
        assertNotNull(result);
    }

    @Test
    void where_keepsUnaliasedColumnWhenSelectedColumnDoesNotMatch() {
        // Given
        final TestContext<CompositeKeyDto> context = testContext(CompositeKeyDto.class, List.of("ID1", "ID2"), "id1", "id2");
        final DtoFromClauseTerminal<CompositeKeyDto> dtoFromClauseTerminal = context.dtoSelector.select("id2");

        // When
        final DtoWhereConditionClause<CompositeKeyDto> result = dtoFromClauseTerminal.where("id1");

        // Then
        assertNotNull(result);
    }

    @Test
    void where_fieldColumnSpec() {
        // Given
        final TestContext<TestDto> context = testContext(TestDto.class, List.of("MY_VAR"), "myVar");
        final SelectSpec selectSpec = ObjectUtils.getFieldValue(context.dtoSelector, "selectSpec", SelectSpec.class);
        selectSpec.setTable(context.aliasGenerator.aliasTable(context.ormTable));

        final DtoFromClauseTerminal<TestDto> dtoFromClauseTerminal = new DtoFromClauseTerminal<>(context.dtoSelector);
        final FieldColumnSpec fieldColumnSpec = new FieldColumnSpec(new FieldSpec("myVar", false), new ColumnSpec("MY_VAR"));

        // When
        final DtoWhereConditionClause<TestDto> result = dtoFromClauseTerminal.where(fieldColumnSpec);

        // Then
        assertNotNull(result);
    }

    @Test
    void withId() throws SQLException {
        // Given
        final TestContext<TestDto> context = testContext(TestDto.class, List.of("MY_VAR"), "myVar");
        when(context.databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        when(context.databaseProvider.select(any(Select.class), any(ConnectionProvider.class))).thenReturn(Collections.emptyList());

        final DtoFromClauseTerminal<TestDto> dtoFromClauseTerminal = context.dtoSelector.select("myVar");

        // When
        final Optional<TestDto> result = dtoFromClauseTerminal.withId("testValue");

        // Then
        assertTrue(result.isEmpty());
        verify(context.databaseProvider).select(any(Select.class), any(ConnectionProvider.class));
    }

    @Test
    void withIdOrNull() throws SQLException {
        // Given
        final TestContext<TestDto> context = testContext(TestDto.class, List.of("MY_VAR"), "myVar");
        when(context.databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        when(context.databaseProvider.select(any(Select.class), any(ConnectionProvider.class))).thenReturn(Collections.emptyList());

        final DtoFromClauseTerminal<TestDto> dtoFromClauseTerminal = context.dtoSelector.select("myVar");

        // When
        final TestDto result = dtoFromClauseTerminal.withIdOrNull("testValue");

        // Then
        assertNull(result);
        verify(context.databaseProvider).select(any(Select.class), any(ConnectionProvider.class));
    }

    @Test
    void withIdOrThrow() throws SQLException {
        // Given
        final TestContext<TestDto> context = testContext(TestDto.class, List.of("MY_VAR"), "myVar");
        when(context.databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        when(context.databaseProvider.select(any(Select.class), any(ConnectionProvider.class))).thenReturn(Collections.emptyList());

        final DtoFromClauseTerminal<TestDto> dtoFromClauseTerminal = context.dtoSelector.select("myVar");

        // When / Then
        assertThrows(NoSuchElementException.class, () -> dtoFromClauseTerminal.withIdOrThrow("testValue"));
        verify(context.databaseProvider).select(any(Select.class), any(ConnectionProvider.class));
    }

    @Test
    void withIdOrThrow_exceptionSupplier() throws SQLException {
        // Given
        final TestContext<TestDto> context = testContext(TestDto.class, List.of("MY_VAR"), "myVar");
        when(context.databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        when(context.databaseProvider.select(any(Select.class), any(ConnectionProvider.class))).thenReturn(Collections.emptyList());

        final DtoFromClauseTerminal<TestDto> dtoFromClauseTerminal = context.dtoSelector.select("myVar");

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> dtoFromClauseTerminal.withIdOrThrow("testValue", IllegalArgumentException::new));
        verify(context.databaseProvider).select(any(Select.class), any(ConnectionProvider.class));
    }

    @Test
    void withId_compositePrimaryKey() throws SQLException {
        // Given
        final TestContext<CompositeKeyDto> context = testContext(CompositeKeyDto.class, List.of("ID1", "ID2"), "id1", "id2");
        when(context.databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        when(context.databaseProvider.select(any(Select.class), any(ConnectionProvider.class))).thenReturn(Collections.emptyList());

        final DtoFromClauseTerminal<CompositeKeyDto> dtoFromClauseTerminal = context.dtoSelector.select("id1", "id2");

        // When
        final Optional<CompositeKeyDto> result = dtoFromClauseTerminal.withId("testValue");

        // Then
        assertTrue(result.isEmpty());
        verify(context.databaseProvider).select(any(Select.class), any(ConnectionProvider.class));
    }

    @Test
    void orderBy() {
        // Given
        final TestContext<TestDto> context = testContext(TestDto.class, List.of("MY_VAR"), "myVar");
        final DtoFromClauseTerminal<TestDto> dtoFromClauseTerminal = new DtoFromClauseTerminal<>(context.dtoSelector);

        // When
        final DtoOrderByClause<TestDto> result = dtoFromClauseTerminal.orderBy("myVar");

        // Then
        assertNotNull(result);
    }

    @Test
    void orderBy_fieldColumnSpec() {
        // Given
        final TestContext<TestDto> context = testContext(TestDto.class, List.of("MY_VAR"), "myVar");
        final SelectSpec selectSpec = ObjectUtils.getFieldValue(context.dtoSelector, "selectSpec", SelectSpec.class);
        selectSpec.setTable(context.aliasGenerator.aliasTable(context.ormTable));

        final DtoFromClauseTerminal<TestDto> dtoFromClauseTerminal = new DtoFromClauseTerminal<>(context.dtoSelector);
        final FieldColumnSpec fieldColumnSpec = new FieldColumnSpec(new FieldSpec("myVar", false), new ColumnSpec("MY_VAR"));

        // When
        final DtoOrderByClause<TestDto> result = dtoFromClauseTerminal.orderBy(fieldColumnSpec);

        // Then
        assertNotNull(result);
    }

    private static <DTO> TestContext<DTO> testContext(final Class<DTO> dtoClass, final List<String> primaryKeyColumns, final String... fieldNames) {
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());

        final Map<FieldAccessor, MappedFieldTarget> fieldColumnMap = fieldColumnMap(dtoClass, table, fieldNames);
        final List<ColumnMetaData> columns = fieldColumnMap.values().stream()
                .map(ColumnMetaData.class::cast)
                .toList();
        final TableMetaData tableMetaData = new TableMetaData("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", primaryKeyColumns, columns);
        final OrmTable ormTable = new OrmTable(dtoClass, tableMetaData, fieldColumnMap, changeTracker, classFieldAccessorCache);
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(dtoClass, ormTable);

        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        when(sqlFunctionRegistry.selectColumnFactory()).thenReturn(new TestColumnExpressionFactory());
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        final TransactionalDatabaseProvider transactionalDatabaseProvider = new TransactionalDatabaseProvider(mock(TransactionManager.class), databaseProvider);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final DtoSelector<DTO> dtoSelector = new DtoSelector<>(dtoClass, ormTable, tableRegistry, classFieldAccessorCache, dtoConstructor, transactionalDatabaseProvider, aliasGenerator, new LitebridgeConfig());

        return new TestContext<>(ormTable, tableRegistry, databaseProvider, aliasGenerator, dtoSelector);
    }

    private static Map<FieldAccessor, MappedFieldTarget> fieldColumnMap(final Class<?> dtoClass, final Table table, final String... fieldNames) {
        return java.util.Arrays.stream(fieldNames)
                .collect(java.util.stream.Collectors.toMap(
                        fieldName -> new DirectFieldAccessor(ClassUtils.getField(dtoClass, fieldName), MethodHandles.lookup()),
                        fieldName -> new ColumnMetaData(table, toColumnName(fieldName), false, Types.VARCHAR)
                ));
    }

    private static String toColumnName(final String fieldName) {
        final StringBuilder columnName = new StringBuilder();

        for (int i = 0; i < fieldName.length(); i++) {
            final char character = fieldName.charAt(i);

            if (Character.isUpperCase(character)) {
                columnName.append('_');
            }

            columnName.append(Character.toUpperCase(character));
        }

        return columnName.toString();
    }

    private record TestContext<DTO>(OrmTable ormTable,
                                    TableRegistry tableRegistry,
                                    DatabaseProvider databaseProvider,
                                    AliasGenerator aliasGenerator,
                                    DtoSelector<DTO> dtoSelector) {
    }

    private static class TestDto {
        private String myVar;
    }

    private static class JoinDto {
        private String joinVar;
    }

    private static class CompositeKeyDto {
        private String id1;
        private String id2;
    }
}
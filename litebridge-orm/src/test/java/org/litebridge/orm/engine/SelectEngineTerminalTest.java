package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.RowColumn;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.VirtualTable;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.AliasedExpression;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.ConvertExpression;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.query.TypeConversionMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.engine.ast.LimitNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.engine.compiler.QueryCompiler;
import org.litebridge.orm.exception.NonUniqueResultException;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.persistence.DtoConstructor;
import org.litebridge.orm.persistence.MappingPlanCache;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;

import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SelectEngineTerminalTest {

    @Test
    void findSelectNodeThrowsWhenNoSelectNodeInAst() throws Exception {
        // Given
        final DtoConstructor dtoConstructor = mock(DtoConstructor.class);
        final SelectEngineTerminal terminal = new SelectEngineTerminal(dtoConstructor);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryPlanCache queryPlanCache = mock(QueryPlanCache.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        when(context.queryPlanCache()).thenReturn(queryPlanCache);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(queryPlanCache.get(anyInt())).thenReturn(new QueryPlanCache.CachedOperation("SELECT 1", Collections.emptyList(), null, null));
        when(databaseProvider.executeQuery(any(), any())).thenReturn(Collections.emptyList());

        final LimitNode limitNode = new LimitNode(null, 5, null);

        // When / Then
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> terminal.fetchList(limitNode, context));
        assertEquals("No SelectNode found in the query AST", ex.getMessage());
    }

    @Test
    void generateSqlCompilesWithoutCaching() {
        // Given
        final DtoConstructor dtoConstructor = mock(DtoConstructor.class);
        final SelectEngineTerminal terminal = new SelectEngineTerminal(dtoConstructor);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryCompiler compiler = mock(QueryCompiler.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);
        final QueryPlanCache queryPlanCache = mock(QueryPlanCache.class);

        when(context.createQueryCompiler()).thenReturn(compiler);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.queryPlanCache()).thenReturn(queryPlanCache);

        final SelectNode selectNode = new SelectNode(null, null, null, null, new ExpressionSpec[0], null);
        final Select selectOperation = mock(Select.class);
        final PreparedOperation preparedOperation = new PreparedOperation(selectOperation, List.of(new BindValue(10, Types.INTEGER)));
        when(compiler.compile(selectNode)).thenReturn(preparedOperation);
        when(databaseProvider.toSql(selectOperation, txManager)).thenReturn("SELECT * FROM users WHERE id = ?");

        // When
        final PreparedSql preparedSql = terminal.generateSql(selectNode, context);

        // Then
        assertEquals("SELECT * FROM users WHERE id = ?", preparedSql.sql());
        assertEquals(1, preparedSql.bindValues().size());
        assertNull(preparedSql.typeConversionMetaData());
        verify(queryPlanCache, never()).put(anyInt(), any());
    }

    @Test
    void executeThrowsIllegalStateExceptionOnSQLException() throws Exception {
        // Given
        final DtoConstructor dtoConstructor = mock(DtoConstructor.class);
        final SelectEngineTerminal terminal = new SelectEngineTerminal(dtoConstructor);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryCompiler compiler = mock(QueryCompiler.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);
        final QueryPlanCache queryPlanCache = mock(QueryPlanCache.class);

        when(context.createQueryCompiler()).thenReturn(compiler);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.queryPlanCache()).thenReturn(queryPlanCache);

        final SelectNode selectNode = new SelectNode(null, null, null, null, new ExpressionSpec[0], null);
        final Select selectOperation = mock(Select.class);
        when(selectOperation.expressions()).thenReturn(Collections.emptyList());
        final PreparedOperation preparedOperation = new PreparedOperation(selectOperation, Collections.emptyList());
        when(compiler.compile(selectNode)).thenReturn(preparedOperation);
        when(databaseProvider.toSql(selectOperation, txManager)).thenReturn("SELECT * FROM test");
        when(databaseProvider.executeQuery(any(PreparedSql.class), eq(txManager))).thenThrow(new SQLException("connection lost"));

        // When / Then
        final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> terminal.fetchList(selectNode, context));
        assertTrue(ex.getMessage().contains("Failed to execute query: SELECT * FROM test"));
        assertInstanceOf(SQLException.class, ex.getCause());
    }

    @Test
    void executeCacheMissFollowedByCacheHit() throws Exception {
        // Given
        final DtoConstructor dtoConstructor = mock(DtoConstructor.class);
        final SelectEngineTerminal terminal = new SelectEngineTerminal(dtoConstructor);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryCompiler compiler = mock(QueryCompiler.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);
        final QueryPlanCache queryPlanCache = new QueryPlanCache();

        when(context.createQueryCompiler()).thenReturn(compiler);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.queryPlanCache()).thenReturn(queryPlanCache);

        final SelectNode selectNode = new SelectNode(null, null, null, null, new ExpressionSpec[0], null);
        final Select selectOperation = mock(Select.class);
        when(selectOperation.expressions()).thenReturn(Collections.emptyList());
        final PreparedOperation preparedOperation = new PreparedOperation(selectOperation, Collections.emptyList());
        when(compiler.compile(selectNode)).thenReturn(preparedOperation);
        when(databaseProvider.toSql(selectOperation, txManager)).thenReturn("SELECT id FROM users");

        final Row row = new Row(List.of(new RowColumn("id", 42, new Column(new Table("users"), "id"))));
        when(databaseProvider.executeQuery(any(PreparedSql.class), eq(txManager))).thenReturn(List.of(row));

        // When: First execution (cache miss)
        final List<Row> firstResult = terminal.fetchList(selectNode, context);

        // Then: Cache contains operation, compiler called once
        assertEquals(1, firstResult.size());
        assertEquals(1, queryPlanCache.size());
        verify(compiler, times(1)).compile(selectNode);

        // When: Second execution (cache hit)
        final List<Row> secondResult = terminal.fetchList(selectNode, context);

        // Then: Second call retrieved from cache, compiler not called again
        assertEquals(1, secondResult.size());
        verify(compiler, times(1)).compile(selectNode);
        verify(databaseProvider, times(2)).executeQuery(any(PreparedSql.class), eq(txManager));
    }

    @Test
    void createTypeConversionMetaDataBranches() throws Exception {
        // Given
        final DtoConstructor dtoConstructor = mock(DtoConstructor.class);
        final SelectEngineTerminal terminal = new SelectEngineTerminal(dtoConstructor);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryCompiler compiler = mock(QueryCompiler.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);
        final QueryPlanCache queryPlanCache = new QueryPlanCache();
        final TableMetaDataCache tableMetaDataCache = mock(TableMetaDataCache.class);

        when(context.createQueryCompiler()).thenReturn(compiler);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.queryPlanCache()).thenReturn(queryPlanCache);
        when(context.tableMetaDataCache()).thenReturn(tableMetaDataCache);

        final Table table = new Table("users");
        final Column nameColumn = new Column(table, "name");
        final Column ageColumn = new Column(table, "age");

        final ColumnExpression colExprWithAlias = mock(ColumnExpression.class);
        when(colExprWithAlias.column()).thenReturn(nameColumn);
        when(colExprWithAlias.alias()).thenReturn("u_name");

        final ColumnExpression colExprWithoutAlias = mock(ColumnExpression.class);
        when(colExprWithoutAlias.column()).thenReturn(ageColumn);

        final ConvertExpression convertExpr = new ConvertExpression(colExprWithAlias, String.class);
        final SelectExpression otherExpr = mock(SelectExpression.class);

        final Select selectOperation = mock(Select.class);
        when(selectOperation.expressions()).thenReturn((List) List.of(otherExpr, convertExpr, colExprWithoutAlias));

        final SelectNode selectNode = new SelectNode(null, null, null, null, new ExpressionSpec[0], null);
        final PreparedOperation preparedOperation = new PreparedOperation(selectOperation, Collections.emptyList());
        when(compiler.compile(selectNode)).thenReturn(preparedOperation);
        when(databaseProvider.toSql(selectOperation, txManager)).thenReturn("SELECT other, name AS u_name, age FROM users");

        final TableMetaData tableMetaData = mock(TableMetaData.class);
        final ColumnMetaData nameMeta = new ColumnMetaData(table, "name", false, Types.VARCHAR);
        final ColumnMetaData ageMeta = new ColumnMetaData(table, "age", false, Types.INTEGER);
        when(tableMetaDataCache.ensureTableMetaData(table)).thenReturn(tableMetaData);
        when(tableMetaData.column("name")).thenReturn(nameMeta);
        when(tableMetaData.column("age")).thenReturn(ageMeta);

        when(databaseProvider.executeQuery(any(PreparedSql.class), eq(txManager))).thenReturn(Collections.emptyList());

        // When
        final List<Row> result = terminal.fetchList(selectNode, context);

        // Then
        assertTrue(result.isEmpty());
        final QueryPlanCache.CachedOperation cached = queryPlanCache.get(selectNode.hashCode());
        assertNotNull(cached);
        final TypeConversionMetaData metaData = cached.typeConversionMetaData();
        assertNotNull(metaData);
        assertEquals(3, metaData.typeOverrides().length);
        assertEquals(String.class, metaData.typeOverrides()[1]);
        assertSame(nameMeta, metaData.columnLabelsToColumnMetaData().get("u_name"));
        assertSame(ageMeta, metaData.columnLabelsToColumnMetaData().get("age"));
    }

    @Test
    void fetchOneBranchesInSqlMode() throws Exception {
        // Given
        final DtoConstructor dtoConstructor = mock(DtoConstructor.class);
        final SelectEngineTerminal terminal = new SelectEngineTerminal(dtoConstructor);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryPlanCache queryPlanCache = mock(QueryPlanCache.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);

        when(context.queryPlanCache()).thenReturn(queryPlanCache);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.mode()).thenReturn(LitebridgeContext.Mode.SQL);

        final SelectNode selectNode = new SelectNode(null, null, null, null, new ExpressionSpec[0], null);
        final QueryPlanCache.CachedOperation cachedOperation = new QueryPlanCache.CachedOperation("SELECT 1", Collections.emptyList(), null, null);
        when(queryPlanCache.get(anyInt())).thenReturn(cachedOperation);

        // Scenario 1: 0 rows
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(Collections.emptyList());

        final Optional<Row> emptyOpt = terminal.fetchOne(selectNode, context);
        assertFalse(emptyOpt.isPresent());

        final Row nullRow = terminal.fetchOneOrNull(selectNode, context);
        assertNull(nullRow);

        assertThrows(NoSuchElementException.class, () -> terminal.fetchOneOrThrow(selectNode, context));
        assertThrows(CustomException.class, () -> terminal.fetchOneOrThrow(selectNode, context, () -> new CustomException("empty")));

        // Scenario 2: 1 row
        final Row singleRow = new Row(List.of(new RowColumn("id", 1, new Column(new Table("users"), "id"))));
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(singleRow));

        final Optional<Row> singleOpt = terminal.fetchOne(selectNode, context);
        assertTrue(singleOpt.isPresent());
        assertSame(singleRow, singleOpt.get());

        final Row fetchedRow = terminal.fetchOneOrNull(selectNode, context);
        assertSame(singleRow, fetchedRow);

        final Row throwRow = terminal.fetchOneOrThrow(selectNode, context);
        assertSame(singleRow, throwRow);

        final Row customThrowRow = terminal.fetchOneOrThrow(selectNode, context, () -> new CustomException("not thrown"));
        assertSame(singleRow, customThrowRow);

        // Scenario 3: Multiple rows (> 1)
        final Row secondRow = new Row(List.of(new RowColumn("id", 2, new Column(new Table("users"), "id"))));
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(singleRow, secondRow));

        final IllegalStateException nonUniqueEx = assertThrows(IllegalStateException.class, () -> terminal.fetchOneOrNull(selectNode, context));
        assertEquals("Expected exactly one result, but got 2", nonUniqueEx.getMessage());

        // Scenario 4: 1 row with resultTypes in SQL mode
        final SelectNode typedSelectNode = new SelectNode(null, null, null, null, new ExpressionSpec[0], new Class<?>[]{Integer.class});
        final TypeConverter typeConverter = mock(TypeConverter.class);
        when(context.typeConverter()).thenReturn(typeConverter);
        when(typeConverter.convert(1, Integer.class)).thenReturn(100);
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(singleRow));

        final Integer convertedRowValue = terminal.fetchOneOrNull(typedSelectNode, context);
        assertNotNull(convertedRowValue);
        assertEquals(100, convertedRowValue);
    }

    @Test
    void fetchFirstBranchesInSqlMode() throws Exception {
        // Given
        final DtoConstructor dtoConstructor = mock(DtoConstructor.class);
        final SelectEngineTerminal terminal = new SelectEngineTerminal(dtoConstructor);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryPlanCache queryPlanCache = mock(QueryPlanCache.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);

        when(context.queryPlanCache()).thenReturn(queryPlanCache);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.mode()).thenReturn(LitebridgeContext.Mode.SQL);

        final SelectNode selectNode = new SelectNode(null, null, null, null, new ExpressionSpec[0], null);
        final QueryPlanCache.CachedOperation cachedOperation = new QueryPlanCache.CachedOperation("SELECT 1", Collections.emptyList(), null, null);
        when(queryPlanCache.get(anyInt())).thenReturn(cachedOperation);

        // Scenario 1: 0 rows
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(Collections.emptyList());

        final Optional<Row> emptyOpt = terminal.fetchFirst(selectNode, context);
        assertFalse(emptyOpt.isPresent());

        final Row nullRow = terminal.fetchFirstOrNull(selectNode, context);
        assertNull(nullRow);

        assertThrows(NoSuchElementException.class, () -> terminal.fetchFirstOrThrow(selectNode, context));
        assertThrows(CustomException.class, () -> terminal.fetchFirstOrThrow(selectNode, context, () -> new CustomException("empty")));

        // Scenario 2: 1 or more rows
        final Column col = new Column(new Table("users"), "id");
        final RowColumn rowColumn1 = new RowColumn("id", 1, col);
        final Row row1 = new Row(List.of(rowColumn1));
        final RowColumn rowColumn2 = new RowColumn("id", 2, col);
        final Row row2 = new Row(List.of(rowColumn2));
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(row1, row2));

        final Optional<Row> firstOpt = terminal.fetchFirst(selectNode, context);
        assertTrue(firstOpt.isPresent());
        assertSame(row1, firstOpt.get());

        final Row firstRow = terminal.fetchFirstOrNull(selectNode, context);
        assertSame(row1, firstRow);

        final Row throwRow = terminal.fetchFirstOrThrow(selectNode, context);
        assertSame(row1, throwRow);

        final Row customThrowRow = terminal.fetchFirstOrThrow(selectNode, context, () -> new CustomException("not thrown"));
        assertSame(row1, customThrowRow);
    }

    @Test
    void fetchStreamAndFetchListInSqlMode() throws Exception {
        // Given
        final DtoConstructor dtoConstructor = mock(DtoConstructor.class);
        final SelectEngineTerminal terminal = new SelectEngineTerminal(dtoConstructor);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryPlanCache queryPlanCache = mock(QueryPlanCache.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);

        when(context.queryPlanCache()).thenReturn(queryPlanCache);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);

        final SelectNode selectNode = new SelectNode(null, null, null, null, new ExpressionSpec[0], null);
        final QueryPlanCache.CachedOperation cachedOperation = new QueryPlanCache.CachedOperation("SELECT 1", Collections.emptyList(), null, null);
        when(queryPlanCache.get(anyInt())).thenReturn(cachedOperation);

        final Table table = new Table("users");
        final Column col = new Column(table, "id");
        final RowColumn rowColumn1 = new RowColumn("id", 1, col);
        final Row row1 = new Row(List.of(rowColumn1));
        final RowColumn rowColumn2 = new RowColumn("id", 2, col);
        final Row row2 = new Row(List.of(rowColumn2));
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(row1, row2));

        // When
        final Stream<Row> stream = terminal.fetchStream(selectNode, context);
        final List<Row> list = terminal.fetchList(selectNode, context);

        // Then
        assertEquals(List.of(row1, row2), stream.toList());
        assertEquals(List.of(row1, row2), list);
    }

    @Test
    void convertRowValuesBranches() throws Exception {
        // Given
        final DtoConstructor dtoConstructor = mock(DtoConstructor.class);
        final SelectEngineTerminal terminal = new SelectEngineTerminal(dtoConstructor);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryPlanCache queryPlanCache = mock(QueryPlanCache.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);
        final TypeConverter typeConverter = mock(TypeConverter.class);

        when(context.queryPlanCache()).thenReturn(queryPlanCache);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.typeConverter()).thenReturn(typeConverter);

        final QueryPlanCache.CachedOperation cachedOperation = new QueryPlanCache.CachedOperation("SELECT 1", Collections.emptyList(), null, null);
        when(queryPlanCache.get(anyInt())).thenReturn(cachedOperation);

        final Table table = new Table("users");
        final Column idCol = new Column(table, "id");
        final Column nameCol = new Column(table, "name");

        // Branch 1: Row size does not match resultTypes length
        final SelectNode mismatchNode = new SelectNode(null, null, null, null, new ExpressionSpec[0], new Class<?>[]{String.class, Integer.class});
        final Row singleColumnRow = new Row(List.of(new RowColumn(idCol.name(), 1, idCol)));
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(singleColumnRow));

        final IllegalStateException mismatchEx = assertThrows(IllegalStateException.class, () -> terminal.fetchList(mismatchNode, context));
        assertEquals("Row size 1 does not match result type array length 2", mismatchEx.getMessage());

        // Branch 2: Matching size, with a null resultType at index 0 and non-null at index 1
        final SelectNode matchingNode = new SelectNode(null, null, null, null, new ExpressionSpec[0], new Class<?>[]{null, String.class});
        final RowColumn rowColumn1 = new RowColumn(idCol.name(), 10, idCol);
        final RowColumn rowColumn2 = new RowColumn(nameCol.name(), 999, nameCol);
        final List columns = new ArrayList();
        columns.add(rowColumn1);
        columns.add(rowColumn2);
        final Row twoColumnRow = new Row(columns);
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(twoColumnRow));
        when(typeConverter.convert(999, String.class)).thenReturn("Alice");

        final List<Row> convertedRows = terminal.fetchList(matchingNode, context);
        assertEquals(1, convertedRows.size());
        assertEquals(10, convertedRows.getFirst().column(0).value());
        assertEquals("Alice", convertedRows.getFirst().column(1).value());
    }

    @Test
    void fetchInDtoModeWithInterfaceAndContextualDto() throws Exception {
        // Given
        final DtoConstructor dtoConstructor = mock(DtoConstructor.class);
        final SelectEngineTerminal terminal = new SelectEngineTerminal(dtoConstructor);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryPlanCache queryPlanCache = mock(QueryPlanCache.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);
        final TypeConverter typeConverter = mock(TypeConverter.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);

        when(context.queryPlanCache()).thenReturn(queryPlanCache);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.typeConverter()).thenReturn(typeConverter);
        when(context.tableRegistry()).thenReturn(tableRegistry);
        when(context.mode()).thenReturn(LitebridgeContext.Mode.DTO);

        final QueryPlanCache.CachedOperation cachedOperation = new QueryPlanCache.CachedOperation("SELECT 1", Collections.emptyList(), null, null);
        when(queryPlanCache.get(anyInt())).thenReturn(cachedOperation);

        // Rows is empty so DtoMapper returns empty list cleanly
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(Collections.emptyList());

        // Scenario 1: Interface mapping (dtoClass is TestInterface.class, ormTable.getDtoClassInterfaces() contains TestInterface.class)
        final OrmTable interfaceOrmTable = mock(OrmTable.class);
        when(interfaceOrmTable.dtoClass()).thenReturn((Class) UserDto.class);
        when(interfaceOrmTable.getDtoClassInterfaces()).thenReturn(Set.of(TestInterface.class));
        when(tableRegistry.getOrmTableOrThrow(UserDto.class)).thenReturn(interfaceOrmTable);

        final SelectNode interfaceNode = new SelectNode(null, UserDto.class, null, null, new ExpressionSpec[0], new Class<?>[]{TestInterface.class});
        final List<TestInterface> interfaceResults = terminal.fetchList(interfaceNode, context);
        assertTrue(interfaceResults.isEmpty());

        // Scenario 2: Contextual DTO (contextDtoClass != null)
        final OrmTable contextOrmTable = mock(OrmTable.class);
        when(contextOrmTable.dtoClass()).thenReturn((Class) UserDto.class);
        when(contextOrmTable.getDtoClassInterfaces()).thenReturn(Collections.emptySet());
        when(tableRegistry.getOrmTableInContextOrThrow(UserDto.class, ContextDto.class)).thenReturn(contextOrmTable);

        final SelectNode contextualNode = new SelectNode(UserDto.class, ContextDto.class, null, null, new ExpressionSpec[0], null);
        final List<UserDto> contextResults = terminal.fetchList(contextualNode, context);
        assertTrue(contextResults.isEmpty());
        verify(tableRegistry).getOrmTableInContextOrThrow(UserDto.class, ContextDto.class);
    }

    @Test
    void fetchDtoModeTerminalOperationsAndSyncPersistedDto() throws Exception {
        // Given
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(lookup);
        final Table table = new Table("users");
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", false, Types.BIGINT);
        final TableMetaData meta = new TableMetaData(table, List.of("id"), List.of(idCol));
        final FieldAccessor idField = cache.fieldAccessorOrThrow(UserDto.class, "id");

        final OrmTable ormTable = new OrmTable(
                UserDto.class,
                meta,
                Map.of(idField, idCol),
                new ChangeTracker(lookup),
                cache);

        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(UserDto.class, ormTable);

        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final SelectEngineTerminal terminal = new SelectEngineTerminal(dtoConstructor);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryPlanCache queryPlanCache = mock(QueryPlanCache.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);
        final TypeConverter typeConverter = mock(TypeConverter.class);
        final MappingPlanCache mappingPlanCache = new MappingPlanCache();

        when(context.queryPlanCache()).thenReturn(queryPlanCache);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.typeConverter()).thenReturn(typeConverter);
        when(context.tableRegistry()).thenReturn(tableRegistry);
        when(context.mappingPlanCache()).thenReturn(mappingPlanCache);
        when(context.getRelatedDtoStrategy()).thenReturn(RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN);
        when(context.mode()).thenReturn(LitebridgeContext.Mode.DTO);

        when(typeConverter.convert(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));

        final SelectNode selectNode = new SelectNode(UserDto.class, null, null, null, new ExpressionSpec[0], null);
        final QueryPlanCache.CachedOperation cachedOperation = new QueryPlanCache.CachedOperation("SELECT id FROM users", Collections.emptyList(), null, null);
        when(queryPlanCache.get(anyInt())).thenReturn(cachedOperation);

        // Case 1: 0 rows returned
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(Collections.emptyList());

        final Optional<UserDto> emptyOpt = terminal.fetchOne(selectNode, context);
        assertFalse(emptyOpt.isPresent());

        final UserDto nullDto = terminal.fetchOneOrNull(selectNode, context);
        assertNull(nullDto);

        assertThrows(NoSuchElementException.class, () -> terminal.fetchOneOrThrow(selectNode, context));
        assertThrows(CustomException.class, () -> terminal.fetchOneOrThrow(selectNode, context, () -> new CustomException("empty")));

        final Optional<UserDto> firstEmptyOpt = terminal.fetchFirst(selectNode, context);
        assertFalse(firstEmptyOpt.isPresent());

        final UserDto firstNullDto = terminal.fetchFirstOrNull(selectNode, context);
        assertNull(firstNullDto);

        assertThrows(NoSuchElementException.class, () -> terminal.fetchFirstOrThrow(selectNode, context));
        assertThrows(CustomException.class, () -> terminal.fetchFirstOrThrow(selectNode, context, () -> new CustomException("empty")));

        // Case 2: 1 row returned
        final Row singleRow = new Row(new ArrayList<>(List.of(new RowColumn("id", 101L, new Column(table, "id")))));
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(singleRow));

        final Optional<UserDto> singleOpt = terminal.fetchOne(selectNode, context);
        assertTrue(singleOpt.isPresent());
        assertEquals(101L, singleOpt.get().id);

        final UserDto singleFetched = terminal.fetchOneOrNull(selectNode, context);
        assertNotNull(singleFetched);
        assertEquals(101L, singleFetched.id);
        assertTrue(ormTable.isPersistedDto(singleFetched));

        final UserDto singleThrow = terminal.fetchOneOrThrow(selectNode, context);
        assertEquals(101L, singleThrow.id);

        final UserDto singleCustomThrow = terminal.fetchOneOrThrow(selectNode, context, () -> new CustomException("not thrown"));
        assertEquals(101L, singleCustomThrow.id);

        final Optional<UserDto> singleFirstOpt = terminal.fetchFirst(selectNode, context);
        assertTrue(singleFirstOpt.isPresent());
        assertEquals(101L, singleFirstOpt.get().id);

        final UserDto singleFirst = terminal.fetchFirstOrNull(selectNode, context);
        assertNotNull(singleFirst);
        assertEquals(101L, singleFirst.id);

        final UserDto singleFirstThrow = terminal.fetchFirstOrThrow(selectNode, context);
        assertEquals(101L, singleFirstThrow.id);

        final UserDto singleFirstCustomThrow = terminal.fetchFirstOrThrow(selectNode, context, () -> new CustomException("not thrown"));
        assertEquals(101L, singleFirstCustomThrow.id);

        // Case 3: > 1 rows (2 rows) returned
        final Row row2 = new Row(new ArrayList<>(List.of(new RowColumn("id", 102L, new Column(table, "id")))));
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(singleRow, row2));

        final NonUniqueResultException nonUniqueEx = assertThrows(NonUniqueResultException.class, () -> terminal.fetchOneOrNull(selectNode, context));
        assertEquals("Expected exactly one mapped result, but got 2", nonUniqueEx.getMessage());
        assertThrows(NonUniqueResultException.class, () -> terminal.fetchOne(selectNode, context));

        final Optional<UserDto> multiFirstOpt = terminal.fetchFirst(selectNode, context);
        assertTrue(multiFirstOpt.isPresent());
        assertEquals(101L, multiFirstOpt.get().id);

        final UserDto multiFirst = terminal.fetchFirstOrNull(selectNode, context);
        assertNotNull(multiFirst);
        assertEquals(101L, multiFirst.id);

        final UserDto multiFirstThrow = terminal.fetchFirstOrThrow(selectNode, context);
        assertEquals(101L, multiFirstThrow.id);

        final UserDto multiFirstCustomThrow = terminal.fetchFirstOrThrow(selectNode, context, () -> new CustomException("not thrown"));
        assertEquals(101L, multiFirstCustomThrow.id);
    }

    @Test
    void fetchDtoModeTypeOverridesAndUnwrap() throws Exception {
        // Given
        final DtoConstructor dtoConstructor = mock(DtoConstructor.class);
        final SelectEngineTerminal terminal = new SelectEngineTerminal(dtoConstructor);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryPlanCache queryPlanCache = mock(QueryPlanCache.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);
        final TypeConverter typeConverter = mock(TypeConverter.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);

        when(context.queryPlanCache()).thenReturn(queryPlanCache);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.typeConverter()).thenReturn(typeConverter);
        when(context.tableRegistry()).thenReturn(tableRegistry);
        when(context.mode()).thenReturn(LitebridgeContext.Mode.DTO);

        final QueryPlanCache.CachedOperation cachedOperation = new QueryPlanCache.CachedOperation("SELECT 1", Collections.emptyList(), null, null);
        when(queryPlanCache.get(anyInt())).thenReturn(cachedOperation);

        final Table table = new Table("users");
        final Column idCol = new Column(table, "id");
        final Column nameCol = new Column(table, "name");

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.dtoClass()).thenReturn((Class) UserDto.class);
        when(ormTable.getDtoClassInterfaces()).thenReturn(Collections.emptySet());
        when(tableRegistry.getOrmTableOrThrow(UserDto.class)).thenReturn(ormTable);

        // Branch A: Multiple type overrides -> DTO class is Row.class
        final SelectNode multipleOverridesNode = new SelectNode(UserDto.class, null, null, null, new ExpressionSpec[0], new Class<?>[]{Long.class, String.class});
        final Row twoColumnRow = new Row(new ArrayList<>(List.of(new RowColumn("id", 1L, idCol), new RowColumn("name", "Bob", nameCol))));
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(twoColumnRow));

        final List<Row> rowsResult = terminal.fetchList(multipleOverridesNode, context);
        assertEquals(1, rowsResult.size());
        assertSame(twoColumnRow, rowsResult.getFirst());

        // Branch B: Single type override -> unwrap(dtoClass, rows, typeConverter)
        final SelectNode singleOverrideNode = new SelectNode(UserDto.class, null, null, null, new ExpressionSpec[0], new Class<?>[]{Long.class});
        final Row singleColRow = new Row(new ArrayList<>(List.of(new RowColumn("id", "555", idCol))));
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(singleColRow));
        when(typeConverter.convert("555", Long.class)).thenReturn(555L);

        final List<Long> unwrappedResult = terminal.fetchList(singleOverrideNode, context);
        assertEquals(1, unwrappedResult.size());
        assertEquals(555L, unwrappedResult.getFirst());

        // Branch C: unwrap when type is Row.class
        final SelectNode rowClassOverrideNode = new SelectNode(UserDto.class, null, null, null, new ExpressionSpec[0], new Class<?>[]{UserDto.class, Row.class});
        final Row rowForUnwrap = new Row(new ArrayList<>(List.of(new RowColumn("id", 10L, idCol), new RowColumn("name", "Test", nameCol))));
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(rowForUnwrap));

        final List<Row> rowClassResult = terminal.fetchList(rowClassOverrideNode, context);
        assertEquals(1, rowClassResult.size());
        assertSame(rowForUnwrap, rowClassResult.getFirst());
    }

    @Test
    void convertRowValueSkipsWhenAlreadyAssignable() throws Exception {
        // Given
        final DtoConstructor dtoConstructor = mock(DtoConstructor.class);
        final SelectEngineTerminal terminal = new SelectEngineTerminal(dtoConstructor);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryPlanCache queryPlanCache = mock(QueryPlanCache.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);
        final TypeConverter typeConverter = mock(TypeConverter.class);

        when(context.queryPlanCache()).thenReturn(queryPlanCache);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.typeConverter()).thenReturn(typeConverter);

        final QueryPlanCache.CachedOperation cachedOperation = new QueryPlanCache.CachedOperation("SELECT 1", Collections.emptyList(), null, null);
        when(queryPlanCache.get(anyInt())).thenReturn(cachedOperation);

        final Table table = new Table("users");
        final Column idCol = new Column(table, "id");
        final Row row = new Row(List.of(new RowColumn("id", 123L, idCol)));
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(row));

        final SelectNode node = new SelectNode(null, null, null, null, new ExpressionSpec[0], new Class<?>[]{Number.class});

        // When
        final List<Row> result = terminal.fetchList(node, context);

        // Then
        assertEquals(1, result.size());
        assertEquals(123L, result.getFirst().column(0).value());
        // Verify typeConverter.convert was NEVER called because 123L is already assignable to Number.class
        verify(typeConverter, never()).convert(any(), any());
    }

    @Test
    void createTypeConversionMetaDataWithVirtualTableAndDelegateExpressions() throws Exception {
        // Given
        final DtoConstructor dtoConstructor = mock(DtoConstructor.class);
        final SelectEngineTerminal terminal = new SelectEngineTerminal(dtoConstructor);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryCompiler compiler = mock(QueryCompiler.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);
        final QueryPlanCache queryPlanCache = new QueryPlanCache();

        when(context.createQueryCompiler()).thenReturn(compiler);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.queryPlanCache()).thenReturn(queryPlanCache);

        final VirtualTable virtualTable = new VirtualTable("vt");
        final Column virtualCol = new Column(virtualTable, "val");

        final ColumnExpression innerColExpr = mock(ColumnExpression.class);
        when(innerColExpr.column()).thenReturn(virtualCol);
        when(innerColExpr.tableAlias()).thenReturn("vt_alias");

        final TestAliasedDelegateExpression delegateExpr = new TestAliasedDelegateExpression(innerColExpr, "val_alias");

        final Select selectOperation = mock(Select.class);
        when(selectOperation.expressions()).thenReturn((List) List.of(delegateExpr));

        final SelectNode selectNode = new SelectNode(null, null, null, null, new ExpressionSpec[0], null);
        final PreparedOperation preparedOperation = new PreparedOperation(selectOperation, Collections.emptyList());
        when(compiler.compile(selectNode)).thenReturn(preparedOperation);
        when(databaseProvider.toSql(selectOperation, txManager)).thenReturn("SELECT val AS val_alias FROM (VALUES (1)) AS vt");
        when(databaseProvider.executeQuery(any(PreparedSql.class), eq(txManager))).thenReturn(Collections.emptyList());

        // When
        final List<Row> result = terminal.fetchList(selectNode, context);

        // Then
        assertTrue(result.isEmpty());
        final QueryPlanCache.CachedOperation cached = queryPlanCache.get(selectNode.hashCode());
        assertNotNull(cached);
        final TypeConversionMetaData metaData = cached.typeConversionMetaData();
        assertNotNull(metaData);
        assertEquals("vt_alias", metaData.columnLabelsToTableAliases().get("val_alias"));
        assertNotNull(metaData.columnLabelsToColumnMetaData().get("val_alias"));
    }

    @Test
    void fetchSqlModeExtendedBranches() throws Exception {
        // Given
        final DtoConstructor dtoConstructor = mock(DtoConstructor.class);
        final SelectEngineTerminal terminal = new SelectEngineTerminal(dtoConstructor);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryPlanCache queryPlanCache = mock(QueryPlanCache.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);
        final TypeConverter typeConverter = mock(TypeConverter.class);

        when(context.queryPlanCache()).thenReturn(queryPlanCache);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.typeConverter()).thenReturn(typeConverter);
        when(context.mode()).thenReturn(LitebridgeContext.Mode.SQL);

        final QueryPlanCache.CachedOperation cachedOperation = new QueryPlanCache.CachedOperation("SELECT 1", Collections.emptyList(), null, null);
        when(queryPlanCache.get(anyInt())).thenReturn(cachedOperation);

        final Table table = new Table("users");
        final Column idCol = new Column(table, "id");
        final Column nameCol = new Column(table, "name");

        // Case 1: selectNode.table() != null with single result type in SQL mode
        final SelectNode tableWithSingleResultTypeNode = new SelectNode("users", null, null, new ExpressionSpec[0], new Class<?>[]{String.class});
        final Row singleColRow = new Row(new ArrayList<>(List.of(new RowColumn("id", 42, idCol))));
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(singleColRow));
        when(typeConverter.convert(42, String.class)).thenReturn("42_str");

        final String result = terminal.fetchOneOrNull(tableWithSingleResultTypeNode, context);
        assertEquals("42_str", result);

        // Case 2: row == null on fetchOneOrNullImpl in SQL mode with single result type
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(Collections.emptyList());
        final Row nullRow = terminal.fetchOneOrNull(tableWithSingleResultTypeNode, context);
        assertNull(nullRow);

        // Case 3: selectNode.table() == null and multiple result types in SQL mode
        final SelectNode multipleTypesSqlNode = new SelectNode(null, null, null, null, new ExpressionSpec[0], new Class<?>[]{String.class, String.class});
        final Row twoColRow = new Row(new ArrayList<>(List.of(new RowColumn("id", 1, idCol), new RowColumn("name", "Alice", nameCol))));
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(twoColRow));
        when(typeConverter.convert(1, String.class)).thenReturn("1_str");

        final Row multipleTypesResult = terminal.fetchOneOrNull(multipleTypesSqlNode, context);
        assertNotNull(multipleTypesResult);
        assertEquals("1_str", multipleTypesResult.column(0).value());
        assertEquals("Alice", multipleTypesResult.column(1).value());
    }

    private static class CustomException extends RuntimeException {
        CustomException(final String message) {
            super(message);
        }
    }

    private static class TestAliasedDelegateExpression implements DelegateExpression, AliasedExpression {
        private final SelectExpression target;
        private final String alias;

        TestAliasedDelegateExpression(final SelectExpression target, final String alias) {
            this.target = target;
            this.alias = alias;
        }

        @Override
        public SelectExpression target() {
            return target;
        }

        @Override
        public String alias() {
            return alias;
        }

        @Override
        public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
            return target.toSql(operation, clause, parent);
        }
    }

    interface TestInterface {
    }

    static class UserDto implements TestInterface {
        public Long id;
    }

    static class ContextDto {
        private Long tenantId;
    }
}

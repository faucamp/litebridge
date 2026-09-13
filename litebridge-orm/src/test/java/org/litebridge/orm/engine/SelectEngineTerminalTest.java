package org.litebridge.orm.engine;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.ConvertExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.query.TypeConversionMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.orm.engine.ast.LimitNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.engine.compiler.QueryCompiler;
import org.litebridge.orm.exception.NonUniqueResultException;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.persistence.DtoConstructor;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
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
        final AliasTransformer aliasTransformer = mock(AliasTransformer.class);

        when(context.createQueryCompiler()).thenReturn(compiler);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.queryPlanCache()).thenReturn(queryPlanCache);
        when(databaseProvider.aliasTransformer()).thenReturn(aliasTransformer);

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
        final AliasTransformer aliasTransformer = mock(AliasTransformer.class);

        when(context.createQueryCompiler()).thenReturn(compiler);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.queryPlanCache()).thenReturn(queryPlanCache);
        when(databaseProvider.aliasTransformer()).thenReturn(aliasTransformer);

        final SelectNode selectNode = new SelectNode(null, null, null, null, new ExpressionSpec[0], null);
        final Select selectOperation = mock(Select.class);
        when(selectOperation.expressions()).thenReturn(Collections.emptyList());
        final PreparedOperation preparedOperation = new PreparedOperation(selectOperation, Collections.emptyList());
        when(compiler.compile(selectNode)).thenReturn(preparedOperation);
        when(databaseProvider.toSql(selectOperation, txManager)).thenReturn("SELECT id FROM users");

        final Row row = new Row().withColumn(new Column(new Table("users"), "id"), 42);
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
        final AliasTransformer aliasTransformer = mock(AliasTransformer.class);
        final TableMetaDataCache tableMetaDataCache = mock(TableMetaDataCache.class);

        when(context.createQueryCompiler()).thenReturn(compiler);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.queryPlanCache()).thenReturn(queryPlanCache);
        when(context.tableMetaDataCache()).thenReturn(tableMetaDataCache);
        when(databaseProvider.aliasTransformer()).thenReturn(aliasTransformer);

        final Table table = new Table("users");
        final Column colWithAlias = new Column(table, "name", "u_name");
        final Column colWithoutAlias = new Column(table, "age", null);

        final ColumnExpression colExprWithAlias = mock(ColumnExpression.class);
        when(colExprWithAlias.column()).thenReturn(colWithAlias);

        final ColumnExpression colExprWithoutAlias = mock(ColumnExpression.class);
        when(colExprWithoutAlias.column()).thenReturn(colWithoutAlias);

        final ConvertExpression convertExpr = new ConvertExpression(colExprWithAlias, String.class);
        final SelectExpression otherExpr = mock(SelectExpression.class);

        final Select selectOperation = mock(Select.class);
        when(selectOperation.expressions()).thenReturn(List.of(otherExpr, convertExpr, colExprWithoutAlias));

        final SelectNode selectNode = new SelectNode(null, null, null, null, new ExpressionSpec[0], null);
        final PreparedOperation preparedOperation = new PreparedOperation(selectOperation, Collections.emptyList());
        when(compiler.compile(selectNode)).thenReturn(preparedOperation);
        when(databaseProvider.toSql(selectOperation, txManager)).thenReturn("SELECT other, name AS u_name, age FROM users");

        when(aliasTransformer.transformAlias("u_name")).thenReturn("u_name");
        when(aliasTransformer.transformAlias("age")).thenReturn("age");

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
        final Row singleRow = new Row().withColumn(new Column("users", "id"), 1);
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
        final Row secondRow = new Row().withColumn(new Column(new Table("users"), "id"), 2);
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(singleRow, secondRow));

        final IllegalStateException nonUniqueEx = assertThrows(IllegalStateException.class, () -> terminal.fetchOneOrNull(selectNode, context));
        assertEquals("Expected exactly one result, but got 2", nonUniqueEx.getMessage());

        // Scenario 4: 1 row with resultTypes in SQL mode
        final SelectNode typedSelectNode = new SelectNode(null, null, null, null, new ExpressionSpec[0], new Class<?>[]{Integer.class});
        final TypeConverter typeConverter = mock(TypeConverter.class);
        when(context.typeConverter()).thenReturn(typeConverter);
        when(typeConverter.convert(1, Integer.class)).thenReturn(100);
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(singleRow));

        final Row convertedRow = terminal.fetchOneOrNull(typedSelectNode, context);
        assertNotNull(convertedRow);
        assertEquals(100, convertedRow.column(0).value());
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
        final Row row1 = new Row().withColumn(new Column("users", "id"), 1);
        final Row row2 = new Row().withColumn(new Column("users", "id"), 2);
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

        final Row row1 = new Row().withColumn(new Column("users", "id"), 1);
        final Row row2 = new Row().withColumn(new Column("users", "id"), 2);
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

        // Branch 1: Row size does not match resultTypes length
        final SelectNode mismatchNode = new SelectNode(null, null, null, null, new ExpressionSpec[0], new Class<?>[]{String.class, Integer.class});
        final Row singleColumnRow = new Row().withColumn(new Column(new Table("users"), "id"), 1);
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(singleColumnRow));

        final IllegalStateException mismatchEx = assertThrows(IllegalStateException.class, () -> terminal.fetchList(mismatchNode, context));
        assertEquals("Row size 1 does not match result type array length 2", mismatchEx.getMessage());

        // Branch 2: Matching size, with a null resultType at index 0 and non-null at index 1
        final SelectNode matchingNode = new SelectNode(null, null, null, null, new ExpressionSpec[0], new Class<?>[]{null, String.class});
        final Column col1 = new Column(new Table("users"), "id");
        final Column col2 = new Column(new Table("users"), "name");
        final Row twoColumnRow = new Row().withColumn(col1, 10).withColumn(col2, "Alice");
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(twoColumnRow));
        when(typeConverter.convert("Alice", String.class)).thenReturn("ALICE_CONVERTED");

        final List<Row> convertedRows = terminal.fetchList(matchingNode, context);
        assertEquals(1, convertedRows.size());
        assertEquals(10, convertedRows.getFirst().column(0).value());
        assertEquals("ALICE_CONVERTED", convertedRows.getFirst().column(1).value());
    }

    @Test
    void fetchInDtoModeSingleTypeOverrideAndRowOverride() throws Exception {
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

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.dtoClass()).thenReturn((Class) UserDto.class);
        when(ormTable.getDtoClassInterfaces()).thenReturn(Collections.emptySet());
        when(tableRegistry.getOrmTableOrThrow(UserDto.class)).thenReturn(ormTable);

        final QueryPlanCache.CachedOperation cachedOperation = new QueryPlanCache.CachedOperation("SELECT name", Collections.emptyList(), null, null);
        when(queryPlanCache.get(anyInt())).thenReturn(cachedOperation);

        // Scenario 1: Single type override (String.class)
        final SelectNode singleOverrideNode = new SelectNode(null, UserDto.class, null, null, new ExpressionSpec[0], new Class<?>[]{String.class});
        final Column col = new Column(new Table("users"), "name");
        final Row row1 = new Row().withColumn(col, "Alice");
        final Row row2 = new Row().withColumn(col, "Bob");
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(row1, row2));
        when(typeConverter.convert("Alice", String.class)).thenReturn("Alice");
        when(typeConverter.convert("Bob", String.class)).thenReturn("Bob");

        final List<String> names = terminal.fetchList(singleOverrideNode, context);
        assertEquals(List.of("Alice", "Bob"), names);

        // Calling fetchOneOrNull on multi-result in DTO mode throws NonUniqueResultException
        final NonUniqueResultException nonUniqueEx = assertThrows(NonUniqueResultException.class, () -> terminal.fetchOneOrNull(singleOverrideNode, context));
        assertEquals("Expected exactly one mapped result, but got 2", nonUniqueEx.getMessage());

        // Calling fetchFirstOrNull returns the first
        final String firstName = terminal.fetchFirstOrNull(singleOverrideNode, context);
        assertEquals("Alice", firstName);

        // When empty list in DTO mode
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(Collections.emptyList());
        assertNull(terminal.fetchOneOrNull(singleOverrideNode, context));
        assertNull(terminal.fetchFirstOrNull(singleOverrideNode, context));

        // Scenario 2: Multiple type overrides -> dtoClass becomes Row.class
        final SelectNode multiOverrideNode = new SelectNode(null, UserDto.class, null, null, new ExpressionSpec[0], new Class<?>[]{String.class, Integer.class});
        final Row multiColRow = new Row().withColumn(col, "Alice").withColumn(new Column(new Table("users"), "age"), 30);
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(multiColRow));
        when(typeConverter.convert("Alice", String.class)).thenReturn("Alice");
        when(typeConverter.convert(30, Integer.class)).thenReturn(30);

        final List<Row> rowResults = terminal.fetchList(multiOverrideNode, context);
        assertEquals(1, rowResults.size());

        // Scenario 3: Single type override to Row.class
        final SelectNode rowOverrideNode = new SelectNode(null, UserDto.class, null, null, new ExpressionSpec[0], new Class<?>[]{Row.class});
        final Row singleColRow = new Row().withColumn(col, "Alice");
        when(databaseProvider.executeQuery(any(), eq(txManager))).thenReturn(List.of(singleColRow));

        final List<Row> unwrappedRows = terminal.fetchList(rowOverrideNode, context);
        assertEquals(1, unwrappedRows.size());
        assertSame(singleColRow, unwrappedRows.getFirst());
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
        when(tableRegistry.getTableInContextOrThrow(UserDto.class, ContextDto.class)).thenReturn(contextOrmTable);

        final SelectNode contextualNode = new SelectNode(null, UserDto.class, ContextDto.class, null, new ExpressionSpec[0], null);
        final List<UserDto> contextResults = terminal.fetchList(contextualNode, context);
        assertTrue(contextResults.isEmpty());
        verify(tableRegistry).getTableInContextOrThrow(UserDto.class, ContextDto.class);
    }

    private static class CustomException extends RuntimeException {
        CustomException(final String message) {
            super(message);
        }
    }

    interface TestInterface {
    }

    static class UserDto implements TestInterface {
        private Long id;
    }

    static class ContextDto {
        private Long tenantId;
    }
}

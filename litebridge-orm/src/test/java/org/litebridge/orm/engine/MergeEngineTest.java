package org.litebridge.orm.engine;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.Merge;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.insert.InsertValuesStep;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.compiler.QueryCompiler;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;

import java.sql.Types;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MergeEngineTest {

    @Test
    void operationTypeNameAndLogger() {
        // Given
        final MergeEngine engine = new MergeEngine();

        // When / Then
        assertEquals("MERGE", engine.operationTypeName());
        assertNotNull(engine.logger());
    }

    @Test
    void mergeIntoDtoClass() throws Exception {
        // Given
        final MergeEngine engine = new MergeEngine();
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryCompiler compiler = mock(QueryCompiler.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);
        final QueryPlanCache queryPlanCache = new QueryPlanCache();
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMetaDataCache tableMetaDataCache = mock(TableMetaDataCache.class);

        when(context.createQueryCompiler()).thenReturn(compiler);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.queryPlanCache()).thenReturn(queryPlanCache);
        when(context.tableRegistry()).thenReturn(tableRegistry);
        when(context.tableMetaDataCache()).thenReturn(tableMetaDataCache);

        final Table table = new Table("users");
        final TableMetaData tableMetaData = mock(TableMetaData.class);
        when(tableMetaData.toTable()).thenReturn(table);
        final ColumnMetaData pkMeta = new ColumnMetaData(table, "id", false, Types.BIGINT, 0, 0, true, null, null);
        when(tableMetaData.primaryKey()).thenReturn(List.of(pkMeta));
        when(tableMetaDataCache.ensureTableMetaData(table)).thenReturn(tableMetaData);

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(tableMetaData);
        when(tableRegistry.getOrmTableOrThrow(UserDto.class)).thenReturn(ormTable);

        final Insert operation = mock(Insert.class);
        when(operation.rows()).thenReturn(1);
        when(operation.columns()).thenReturn(Collections.emptyList());
        when(operation.returnGeneratedKeys()).thenReturn(true);
        final PreparedOperation preparedOperation = new PreparedOperation(operation, Collections.emptyList());
        when(compiler.compile(any(QueryNode.class))).thenReturn(preparedOperation);
        when(databaseProvider.toSql(operation, txManager)).thenReturn("MERGE INTO users");

        final UpdateResult updateResult = mock(UpdateResult.class);
        when(databaseProvider.executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), eq(txManager))).thenReturn(updateResult);

        final QueryNode mergeNode = new MergeNode(null, UserDto.class);

        // When
        final UpdateResult result = engine.mergeInto(UserDto.class, step -> new InsertValuesStep(mergeNode, context), context);

        // Then
        assertSame(updateResult, result);
        verify(databaseProvider).executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), eq(txManager));
    }

    @Test
    void mergeIntoTableName() throws Exception {
        // Given
        final MergeEngine engine = new MergeEngine();
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryCompiler compiler = mock(QueryCompiler.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);
        final QueryPlanCache queryPlanCache = new QueryPlanCache();
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMetaDataCache tableMetaDataCache = mock(TableMetaDataCache.class);

        when(context.createQueryCompiler()).thenReturn(compiler);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.queryPlanCache()).thenReturn(queryPlanCache);
        when(context.tableRegistry()).thenReturn(tableRegistry);
        when(context.tableMetaDataCache()).thenReturn(tableMetaDataCache);

        final Table table = new Table("orders");
        when(tableRegistry.getOrCreateSpiTable("orders")).thenReturn(table);

        final TableMetaData tableMetaData = mock(TableMetaData.class);
        final ColumnMetaData pkMeta = new ColumnMetaData(table, "id", false, Types.BIGINT, 0, 0, true, null, null);
        when(tableMetaData.primaryKey()).thenReturn(List.of(pkMeta));
        when(tableMetaDataCache.ensureTableMetaData(table)).thenReturn(tableMetaData);

        final Insert operation = mock(Insert.class);
        when(operation.rows()).thenReturn(1);
        when(operation.columns()).thenReturn(Collections.emptyList());
        when(operation.returnGeneratedKeys()).thenReturn(true);
        final PreparedOperation preparedOperation = new PreparedOperation(operation, Collections.emptyList());
        when(compiler.compile(any(QueryNode.class))).thenReturn(preparedOperation);
        when(databaseProvider.toSql(operation, txManager)).thenReturn("MERGE INTO orders");

        final UpdateResult updateResult = mock(UpdateResult.class);
        when(databaseProvider.executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), eq(txManager))).thenReturn(updateResult);

        final QueryNode mergeNode = new MergeNode("orders", null);

        // When
        final UpdateResult result = engine.mergeInto("orders", step -> new InsertValuesStep(mergeNode, context), context);

        // Then
        assertSame(updateResult, result);
        verify(databaseProvider).executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), eq(txManager));
    }

    static class UserDto {
        private Long id;
    }
}

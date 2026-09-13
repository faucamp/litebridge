package org.litebridge.orm.engine;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.InsertResult;
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

class InsertEngineTest {

    static class UserDto {
        private Long id;
        private String name;
    }

    @Test
    void operationTypeNameAndLogger() {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final InsertEngine engine = new InsertEngine(tableRegistry);

        // When / Then
        assertEquals("INSERT", engine.operationTypeName());
        assertNotNull(engine.logger());
    }

    @Test
    void insertWithDtoClass() throws Exception {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final InsertEngine engine = new InsertEngine(tableRegistry);
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
        final TableMetaData tableMetaData = mock(TableMetaData.class);
        when(tableMetaData.toTable()).thenReturn(table);
        final ColumnMetaData pkMeta = new ColumnMetaData(table, "id", false, Types.BIGINT, 0, 0, true, null, null);
        when(tableMetaData.primaryKey()).thenReturn(List.of(pkMeta));
        when(tableMetaDataCache.ensureTableMetaData(table)).thenReturn(tableMetaData);

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(tableMetaData);
        when(tableRegistry.getOrmTableOrThrow(UserDto.class)).thenReturn(ormTable);

        final Insert insertOperation = mock(Insert.class);
        when(insertOperation.rows()).thenReturn(1);
        when(insertOperation.columns()).thenReturn(Collections.emptyList());
        when(insertOperation.returnGeneratedKeys()).thenReturn(true);

        final PreparedOperation preparedOperation = new PreparedOperation(insertOperation, List.of(new BindValue(1L, Types.BIGINT)));
        when(compiler.compile(any())).thenReturn(preparedOperation);
        when(databaseProvider.toSql(insertOperation, txManager)).thenReturn("INSERT INTO users (id) VALUES (?)");

        final InsertResult insertResult = mock(InsertResult.class);
        when(databaseProvider.executeUpdate(any(PreparedSql.class), eq(InsertResult.class), eq(txManager))).thenReturn(insertResult);

        // When
        final InsertResult result = engine.insert(UserDto.class, step -> step.into("id").values(1L), context);

        // Then
        assertSame(insertResult, result);
        verify(tableRegistry).getOrmTableOrThrow(UserDto.class);
        verify(databaseProvider).executeUpdate(any(PreparedSql.class), eq(InsertResult.class), eq(txManager));
    }

    @Test
    void insertWithTableName() throws Exception {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final InsertEngine engine = new InsertEngine(tableRegistry);
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

        final Table table = new Table("orders");
        when(tableRegistry.getOrCreateSpiTable("orders")).thenReturn(table);

        final TableMetaData tableMetaData = mock(TableMetaData.class);
        final ColumnMetaData pkMeta = new ColumnMetaData(table, "id", false, Types.BIGINT, 0, 0, true, null, null);
        when(tableMetaData.primaryKey()).thenReturn(List.of(pkMeta));
        when(tableMetaDataCache.ensureTableMetaData(table)).thenReturn(tableMetaData);

        final Insert insertOperation = mock(Insert.class);
        when(insertOperation.rows()).thenReturn(1);
        when(insertOperation.columns()).thenReturn(Collections.emptyList());
        when(insertOperation.returnGeneratedKeys()).thenReturn(true);

        final PreparedOperation preparedOperation = new PreparedOperation(insertOperation, List.of(new BindValue(100, Types.INTEGER)));
        when(compiler.compile(any())).thenReturn(preparedOperation);
        when(databaseProvider.toSql(insertOperation, txManager)).thenReturn("INSERT INTO orders (total) VALUES (?)");

        final InsertResult insertResult = mock(InsertResult.class);
        when(databaseProvider.executeUpdate(any(PreparedSql.class), eq(InsertResult.class), eq(txManager))).thenReturn(insertResult);

        // When
        final InsertResult result = engine.insert("orders", step -> step.into("total").values(100), context);

        // Then
        assertSame(insertResult, result);
        verify(tableRegistry).getOrCreateSpiTable("orders");
        verify(databaseProvider).executeUpdate(any(PreparedSql.class), eq(InsertResult.class), eq(txManager));
    }
}

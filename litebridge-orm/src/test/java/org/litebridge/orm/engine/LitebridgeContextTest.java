package org.litebridge.orm.engine;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.engine.compiler.QueryCompiler;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.AliasGenerator;
import org.litebridge.orm.persistence.alias.NoOpAliasGenerator;
import org.litebridge.tracking.ClassFieldAccessorCache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LitebridgeContextTest {

    @Test
    void initialiseContextInDtoMode() {
        // Given
        final LitebridgeConfig config = new LitebridgeConfig(RelatedDtoStrategy.NULL_IF_NO_JOIN);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final TypeConverter typeConverter = mock(TypeConverter.class);
        when(databaseProvider.sqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        when(databaseProvider.typeConverter()).thenReturn(typeConverter);

        final QueryPlanCache queryPlanCache = mock(QueryPlanCache.class);
        final AliasGenerator aliasGenerator = new NoOpAliasGenerator();
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMetaDataCache tableMetaDataCache = mock(TableMetaDataCache.class);
        final ClassFieldAccessorCache classFieldAccessorCache = mock(ClassFieldAccessorCache.class);
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final SelectEngine selectEngine = mock(SelectEngine.class);

        // When
        final LitebridgeContext context = new LitebridgeContext(
                LitebridgeContext.Mode.DTO,
                config,
                databaseProvider,
                queryPlanCache,
                aliasGenerator,
                tableRegistry,
                tableMetaDataCache,
                classFieldAccessorCache,
                transactionManager,
                selectEngine
        );

        // Then
        assertEquals(LitebridgeContext.Mode.DTO, context.mode());
        assertSame(config, context.config());
        assertSame(databaseProvider, context.databaseProvider());
        assertSame(sqlFunctionRegistry, context.sqlFunctionRegistry());
        assertSame(queryPlanCache, context.queryPlanCache());
        assertSame(aliasGenerator, context.aliasGenerator());
        assertSame(tableRegistry, context.tableRegistry());
        assertSame(tableMetaDataCache, context.tableMetaDataCache());
        assertSame(classFieldAccessorCache, context.classFieldAccessorCache());
        assertSame(transactionManager, context.transactionManager());
        assertSame(typeConverter, context.typeConverter());
        assertSame(selectEngine, context.selectEngine());
        assertEquals(RelatedDtoStrategy.NULL_IF_NO_JOIN, context.getRelatedDtoStrategy());
        assertNotNull(context.selectExpressionMapper());

        final QueryCompiler compiler = context.createQueryCompiler();
        assertNotNull(compiler);

        context.setRelatedDtoStrategy(RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN);
        assertEquals(RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN, context.getRelatedDtoStrategy());
    }

    @Test
    void initialiseContextInSqlMode() {
        // Given
        final LitebridgeConfig config = new LitebridgeConfig();
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final TypeConverter typeConverter = mock(TypeConverter.class);
        when(databaseProvider.sqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        when(databaseProvider.typeConverter()).thenReturn(typeConverter);

        final QueryPlanCache queryPlanCache = mock(QueryPlanCache.class);
        final AliasGenerator aliasGenerator = new NoOpAliasGenerator();
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMetaDataCache tableMetaDataCache = mock(TableMetaDataCache.class);
        final ClassFieldAccessorCache classFieldAccessorCache = mock(ClassFieldAccessorCache.class);
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final SelectEngine selectEngine = mock(SelectEngine.class);

        // When
        final LitebridgeContext context = new LitebridgeContext(
                LitebridgeContext.Mode.SQL,
                config,
                databaseProvider,
                queryPlanCache,
                aliasGenerator,
                tableRegistry,
                tableMetaDataCache,
                classFieldAccessorCache,
                transactionManager,
                selectEngine
        );

        // Then
        assertEquals(LitebridgeContext.Mode.SQL, context.mode());
        assertNotNull(context.selectExpressionMapper());
    }

    @Test
    void initialiseContextInNativeSqlMode() {
        // Given
        final LitebridgeConfig config = new LitebridgeConfig();
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final TypeConverter typeConverter = mock(TypeConverter.class);
        when(databaseProvider.sqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        when(databaseProvider.typeConverter()).thenReturn(typeConverter);

        final QueryPlanCache queryPlanCache = mock(QueryPlanCache.class);
        final AliasGenerator aliasGenerator = new NoOpAliasGenerator();
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMetaDataCache tableMetaDataCache = mock(TableMetaDataCache.class);
        final ClassFieldAccessorCache classFieldAccessorCache = mock(ClassFieldAccessorCache.class);
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final SelectEngine selectEngine = mock(SelectEngine.class);

        // When
        final LitebridgeContext context = new LitebridgeContext(
                LitebridgeContext.Mode.NATIVE_SQL,
                config,
                databaseProvider,
                queryPlanCache,
                aliasGenerator,
                tableRegistry,
                tableMetaDataCache,
                classFieldAccessorCache,
                transactionManager,
                selectEngine
        );

        // Then
        assertEquals(LitebridgeContext.Mode.NATIVE_SQL, context.mode());
        assertNotNull(context.selectExpressionMapper());
    }
}

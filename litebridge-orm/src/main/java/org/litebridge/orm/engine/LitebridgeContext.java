package org.litebridge.orm.engine;

import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.orm.api.select.dto.DtoProtoExpressionResolver;
import org.litebridge.orm.api.select.model.ProtoExpressionResolver;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.api.select.sql.SqlProtoExpressionResolver;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.engine.compiler.QueryCompiler;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.AliasGenerator;
import org.litebridge.tracking.ClassFieldAccessorCache;

/**
 * A context object that provides access to core Litebridge components and configuration.
 */
public final class LitebridgeContext {

    private final Mode mode;
    private final LitebridgeConfig config;
    private final DatabaseProvider databaseProvider;
    private final SqlFunctionRegistry sqlFunctionRegistry;
    private final QueryPlanCache queryPlanCache;
    private final AliasGenerator aliasGenerator;
    private final TableRegistry tableRegistry;
    private final TableMetaDataCache tableMetaDataCache;
    private final TypeConverter typeConverter;
    private final SelectExpressionMapper selectExpressionMapper;
    private final ClassFieldAccessorCache classFieldAccessorCache;
    private final TransactionManager transactionManager;
    private final SelectEngine selectEngine;
    private RelatedDtoStrategy relatedDtoStrategy;

    /**
     * Create a new Litebridge context with the specified components.
     *
     * @param mode           The mode of operation for the Litebridge context.
     * @param config         Configuration for managing runtime behaviour
     * @param queryPlanCache A cache for storing execution plans based on query structure.
     * @param aliasGenerator An alias generator for creating unique table and column aliases.
     */
    public LitebridgeContext(final Mode mode,
                             final LitebridgeConfig config,
                             final DatabaseProvider databaseProvider,
                             final QueryPlanCache queryPlanCache,
                             final AliasGenerator aliasGenerator,
                             final TableRegistry tableRegistry,
                             final TableMetaDataCache tableMetaDataCache,
                             final ClassFieldAccessorCache classFieldAccessorCache,
                             final TransactionManager transactionManager,
                             final SelectEngine selectEngine) {
        this.mode = mode;
        this.config = config;
        this.databaseProvider = databaseProvider;
        this.sqlFunctionRegistry = databaseProvider.sqlFunctionRegistry();
        this.queryPlanCache = queryPlanCache;
        this.aliasGenerator = aliasGenerator;
        this.relatedDtoStrategy = config.relatedDtoStrategy();
        this.tableRegistry = tableRegistry;
        this.tableMetaDataCache = tableMetaDataCache;
        this.classFieldAccessorCache = classFieldAccessorCache;
        this.transactionManager = transactionManager;
        this.typeConverter = databaseProvider.typeConverter();
        this.selectExpressionMapper = createSelectExpressionMapper();
        this.selectEngine = selectEngine;
    }

    public Mode mode() {
        return mode;
    }

    public LitebridgeConfig config() {
        return config;
    }

    /**
     * Provides access to the SQL function registry.
     *
     * @return the {@link SqlFunctionRegistry} instance for the context
     */
    public SqlFunctionRegistry sqlFunctionRegistry() {
        return sqlFunctionRegistry;
    }

    /**
     * Provides access to the query plan cache.
     *
     * @return the {@link QueryPlanCache} instance for the context
     */
    public QueryPlanCache queryPlanCache() {
        return queryPlanCache;
    }

    /**
     * Provides access to the alias generator.
     *
     * @return the {@link AliasGenerator} instance for the context
     */
    public AliasGenerator aliasGenerator() {
        return aliasGenerator;
    }

    /**
     * Provides access to the table metadata cache.
     *
     * @return the {@link TableMetaDataCache} instance for the context
     */
    public TableMetaDataCache tableMetaDataCache() {
        return tableMetaDataCache;
    }

    /**
     * Provides access to the table registry.
     *
     * @return the {@link TableRegistry} instance for the context
     */
    public TableRegistry tableRegistry() {
        return tableRegistry;
    }

    /**
     * Provides access to the database provider.
     *
     * @return the {@link DatabaseProvider} instance for the context
     */
    public DatabaseProvider databaseProvider() {
        return databaseProvider;
    }

    /**
     * Provides access to the transaction manager.
     *
     * @return the {@link TransactionManager} instance for the context
     */
    public TransactionManager transactionManager() {
        return transactionManager;
    }

    /**
     * Provides access to the class field accessor cache.
     *
     * @return the {@link ClassFieldAccessorCache} instance for the context
     */
    public ClassFieldAccessorCache classFieldAccessorCache() {
        return classFieldAccessorCache;
    }

    /**
     * Provides access to the type converter.
     *
     * @return the {@link TypeConverter} instance for the context
     */
    public TypeConverter typeConverter() {
        return typeConverter;
    }

    /**
     * Provides access to the select expression mapper.
     *
     * @return the {@link SelectExpressionMapper} instance for the context
     */
    public SelectExpressionMapper selectExpressionMapper() {
        return selectExpressionMapper;
    }

    /**
     * Provides access to the select engine.
     *
     * @return the {@link SelectEngine} instance for the context
     */
    public SelectEngine selectEngine() {
        return selectEngine;
    }

    /**
     * Gets the context's related DTO retrieval strategy.
     *
     * @return the context's related DTO retrieval strategy
     */
    public RelatedDtoStrategy getRelatedDtoStrategy() {
        return relatedDtoStrategy;
    }

    /**
     * Sets the context's related DTO retrieval strategy.
     *
     * @param relatedDtoStrategy the related DTO retrieval strategy to set
     */
    public void setRelatedDtoStrategy(final RelatedDtoStrategy relatedDtoStrategy) {
        this.relatedDtoStrategy = relatedDtoStrategy;
    }

    /**
     * Creates a new query compiler for the context.
     *
     * @return the new query compiler
     */
    public QueryCompiler createQueryCompiler() {
        return new QueryCompiler(this);
    }

    private SelectExpressionMapper createSelectExpressionMapper() {
        final ProtoExpressionResolver protoExpressionResolver;

        if (mode == Mode.DTO) {
            protoExpressionResolver = new DtoProtoExpressionResolver(aliasGenerator, classFieldAccessorCache(), tableRegistry());
        } else {
            protoExpressionResolver = new SqlProtoExpressionResolver();
        }

        return new SelectExpressionMapper(databaseProvider.sqlFunctionRegistry(), protoExpressionResolver, tableMetaDataCache, databaseProvider.typeConverter());
    }

    public enum Mode {
        DTO,
        SQL,
        NATIVE_SQL;
    }
}

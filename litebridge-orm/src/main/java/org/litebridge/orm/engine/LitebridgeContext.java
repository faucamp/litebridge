package org.litebridge.orm.engine;

import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.AliasGenerator;
import org.litebridge.tracking.ClassFieldAccessorCache;

/**
 * A context object that provides access to core Litebridge components and configuration.
 * <p>
 * This record provides the core configuration and utility components required for
 * query generation, function resolution, and execution.
 */
public final class LitebridgeContext {

    private final LitebridgeConfig config;
    private final FromClauseEngine fromClauseEngine;
    private final SqlFunctionRegistry sqlFunctionRegistry;
    private final QueryPlanCache queryPlanCache;
    private final AliasGenerator aliasGenerator;
    private final TableMetaDataCache tableMetaDataCache;
    private final TypeConverter typeConverter;
    private final SelectExpressionMapper selectExpressionMapper;
    private RelatedDtoStrategy relatedDtoStrategy;

    /**
     * Create a new Litebridge context with the specified components.
     *
     * @param config                 Configuration for managing runtime behaviour
     * @param fromClauseEngine       The engine responsible for managing table registries alias generation, and facilitating query specifications.
     * @param sqlFunctionRegistry    A registry for resolving SQL functions used in expressions.
     * @param queryPlanCache         A cache for storing execution plans based on query structure.
     * @param aliasGenerator         An alias generator for creating unique table and column aliases.
     * @param typeConverter          A converter for converting between Java types and database types.
     * @param selectExpressionMapper A mapper for resolving query expressions.
     */
    public LitebridgeContext(final LitebridgeConfig config,
                             final FromClauseEngine fromClauseEngine,
                             final SqlFunctionRegistry sqlFunctionRegistry,
                             final QueryPlanCache queryPlanCache,
                             final AliasGenerator aliasGenerator,
                             final TableMetaDataCache tableMetaDataCache,
                             final TypeConverter typeConverter,
                             final SelectExpressionMapper selectExpressionMapper) {
        this.config = config;
        this.fromClauseEngine = fromClauseEngine;
        this.sqlFunctionRegistry = sqlFunctionRegistry;
        this.queryPlanCache = queryPlanCache;
        this.aliasGenerator = aliasGenerator;
        this.relatedDtoStrategy = config.relatedDtoStrategy();
        this.tableMetaDataCache = tableMetaDataCache;
        this.typeConverter = typeConverter;
        this.selectExpressionMapper = selectExpressionMapper;
    }

    public LitebridgeConfig config() {
        return config;
    }

    public FromClauseEngine fromClauseEngine() {
        return fromClauseEngine;
    }

    public SqlFunctionRegistry sqlFunctionRegistry() {
        return sqlFunctionRegistry;
    }

    public QueryPlanCache queryPlanCache() {
        return queryPlanCache;
    }

    public AliasGenerator aliasGenerator() {
        return aliasGenerator;
    }

    public TableMetaDataCache tableMetaDataCache() {
        return tableMetaDataCache;
    }

    public TableRegistry tableRegistry() {
        return fromClauseEngine.tableRegistry();
    }

    public DatabaseProvider databaseProvider() {
        return fromClauseEngine.databaseProvider();
    }

    public TransactionManager transactionManager() {
        return fromClauseEngine.databaseProvider().transactionManager();
    }

    public ClassFieldAccessorCache classFieldAccessorCache() {
        return fromClauseEngine.changeTracker().classFieldAccessorCache();
    }

    public TypeConverter typeConverter() {
        return typeConverter;
    }

    public SelectExpressionMapper selectExpressionMapper() {
        return selectExpressionMapper;
    }

    public RelatedDtoStrategy getRelatedDtoStrategy() {
        return relatedDtoStrategy;
    }

    public void setRelatedDtoStrategy(final RelatedDtoStrategy relatedDtoStrategy) {
        this.relatedDtoStrategy = relatedDtoStrategy;
    }

    public QueryCompiler createQueryCompiler() {
        return new QueryCompiler(fromClauseEngine.tableRegistry(), aliasGenerator);
    }
}

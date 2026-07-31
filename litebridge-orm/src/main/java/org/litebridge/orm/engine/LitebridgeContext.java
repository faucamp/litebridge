package org.litebridge.orm.engine;

import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.persistence.alias.AliasGenerator;

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
    private RelatedDtoStrategy relatedDtoStrategy;

    /**
     * Create a new Litebridge context with the specified components.
     *
     * @param config              Configuration for managing runtime behaviour
     * @param fromClauseEngine    The engine responsible for managing table registries alias generation, and facilitating query specifications.
     * @param sqlFunctionRegistry A registry for resolving SQL functions used in expressions.
     * @param queryPlanCache      A cache for storing execution plans based on query structure.
     * @param aliasGenerator      An alias generator for creating unique table and column aliases.
     */
    public LitebridgeContext(LitebridgeConfig config,
                             FromClauseEngine fromClauseEngine,
                             SqlFunctionRegistry sqlFunctionRegistry,
                             QueryPlanCache queryPlanCache,
                             AliasGenerator aliasGenerator) {
        this.config = config;
        this.fromClauseEngine = fromClauseEngine;
        this.sqlFunctionRegistry = sqlFunctionRegistry;
        this.queryPlanCache = queryPlanCache;
        this.aliasGenerator = aliasGenerator;
        this.relatedDtoStrategy = config.relatedDtoStrategy();
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

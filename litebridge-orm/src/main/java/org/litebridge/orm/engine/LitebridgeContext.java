package org.litebridge.orm.engine;

import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.persistence.alias.AliasGenerator;

/**
 * A context object that provides access to core Litebridge components and configuration.
 * <p>
 * This record provides the core configuration and utility components required for
 * query generation, function resolution, and execution.
 * <p>
 * It encapsulates:
 *
 * @param config              Configuration for managing runtime behaviour
 * @param fromClauseEngine    The engine responsible for managing table registries alias generation, and facilitating query specifications.
 * @param sqlFunctionRegistry A registry for resolving SQL functions used in expressions.
 * @param queryPlanCache      A cache for storing execution plans based on query structure.
 * @param aliasGenerator      An alias generator for creating unique table and column aliases.
 */
public record LitebridgeContext(LitebridgeConfig config,
                                FromClauseEngine fromClauseEngine,
                                SqlFunctionRegistry sqlFunctionRegistry,
                                QueryPlanCache queryPlanCache,
                                AliasGenerator aliasGenerator) {
}

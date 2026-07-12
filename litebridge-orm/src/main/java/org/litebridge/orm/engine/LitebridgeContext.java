package org.litebridge.orm.engine;

import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.orm.config.LitebridgeConfig;

/**
 * Represents the context for operation execution within the Litebridge framework.
 * <p>
 * This record provides the core configuration and utility components required for
 * query generation, function resolution, and execution.
 * <p>
 * It encapsulates:
 *
 * @param config              Configuration for managing runtime behaviour
 * @param fromClauseEngine    The engine responsible for managing table registries alias generation, and facilitating query specifications.
 * @param sqlFunctionRegistry A registry for resolving SQL functions used in expressions.
 */
public record LitebridgeContext(LitebridgeConfig config,
                                FromClauseEngine fromClauseEngine,
                                SqlFunctionRegistry sqlFunctionRegistry) {
}

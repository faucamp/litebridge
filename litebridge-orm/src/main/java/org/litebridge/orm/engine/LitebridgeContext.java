package org.litebridge.orm.engine;

import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.config.LitebridgeConfig;

public record LitebridgeContext(LitebridgeConfig config,
                                FromClauseEngine fromClauseEngine,
                                SqlFunctionRegistry sqlFunctionRegistry) {
}

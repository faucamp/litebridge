package org.litebridgedb.orm.engine;

import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.orm.api.select.model.SelectExpressionMapper;
import org.litebridgedb.orm.config.LitebridgeConfig;

public record LitebridgeContext(LitebridgeConfig config,
                                FromClauseEngine fromClauseEngine,
                                SqlFunctionRegistry sqlFunctionRegistry,
                                SelectExpressionMapper selectExpressionMapper) {
}

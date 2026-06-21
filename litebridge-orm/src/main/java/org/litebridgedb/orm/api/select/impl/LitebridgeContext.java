package org.litebridgedb.orm.api.select.impl;

import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.orm.api.select.model.SelectExpressionMapper;
import org.litebridgedb.orm.config.LitebridgeConfig;
import org.litebridgedb.orm.engine.FromClauseEngine;

public record LitebridgeContext(LitebridgeConfig config,
                                FromClauseEngine fromClauseEngine,
                                SqlFunctionRegistry sqlFunctionRegistry,
                                SelectExpressionMapper selectExpressionMapper) {
}

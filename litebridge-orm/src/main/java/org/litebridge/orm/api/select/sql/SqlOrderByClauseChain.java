package org.litebridge.orm.api.select.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.api.select.impl.OrderByClauseTerminalImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;

public final class SqlOrderByClauseChain
        extends OrderByClauseTerminalImpl<Row>
        implements OrderByClauseChain<Row, SqlOrderByClause, SqlOrderByClauseChain> {

    public SqlOrderByClauseChain(final QueryNode node, final SelectEngineTerminal selectEngineTerminal, final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlOrderByClause then(final String... columns) {
        return new SqlOrderByClause(columns, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlOrderByClause then(final ExpressionSpec... expressions) {
        return new SqlOrderByClause(expressions, node, selectEngineTerminal, litebridgeContext);
    }
}

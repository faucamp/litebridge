package org.litebridge.orm.api.select.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.api.select.impl.OrderByClauseTerminalImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Represents a chain of ORDER BY clauses in a SQL-based query.
 */
public final class SqlOrderByClauseChain
        extends OrderByClauseTerminalImpl<Row>
        implements OrderByClauseChain<Row, SqlOrderByClause, SqlOrderByClauseChain> {

    /**
     * Creates a new instance of {@code SqlOrderByClauseChain}.
     *
     * @param node                 the current query node
     * @param selectEngineTerminal the terminal select engine
     * @param litebridgeContext    the Litebridge context
     */
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

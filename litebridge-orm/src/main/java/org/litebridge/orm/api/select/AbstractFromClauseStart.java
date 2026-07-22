package org.litebridge.orm.api.select;

import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.sql.SqlFromClauseTerminal;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.expression.ExpressionSpec;

abstract sealed class AbstractFromClauseStart permits FromClauseStart, FromClauseStartTypeOverride {

    /**
     * The current query node.
     */
    protected final QueryNode node;

    /**
     * The from clause engine.
     */
    protected final FromClauseEngine fromClauseEngine;

    protected AbstractFromClauseStart(final QueryNode node, final FromClauseEngine fromClauseEngine) {
        this.node = node;
        this.fromClauseEngine = fromClauseEngine;
    }

    /**
     * Starts a FROM clause for the given SQL table.
     *
     * @param table the table name.
     * @return the SQL from clause terminal.
     */
    public SqlFromClauseTerminal from(final String table) {
        return fromClauseEngine.from(node, table);
    }
}

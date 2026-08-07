package org.litebridge.orm.api.select;

import org.litebridge.orm.api.select.ast.SelectNode;
import org.litebridge.orm.api.sql.SqlFromClauseTerminal;
import org.litebridge.orm.engine.FromClauseEngine;

abstract sealed class AbstractFromClauseStart permits FromClauseStart, FromClauseStartTypeOverride {

    /**
     * The current query node.
     */
    protected final SelectNode node;

    /**
     * The from clause engine.
     */
    protected final FromClauseEngine fromClauseEngine;

    protected AbstractFromClauseStart(final SelectNode node, final FromClauseEngine fromClauseEngine) {
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

package org.litebridge.orm.api.select;

import org.litebridge.orm.api.sql.SqlFromClauseTerminal;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.expression.ExpressionSpec;

abstract sealed class AbstractFromClauseStart permits FromClauseStart, FromClauseStartTypeOverride{

    /**
     * The expression specifications to select.
     */
    protected final ExpressionSpec[] expressionSpecs;

    /**
     * The from clause engine.
     */
    protected final FromClauseEngine fromClauseEngine;

    protected AbstractFromClauseStart(FromClauseEngine fromClauseEngine) {
        this(new ExpressionSpec[0], fromClauseEngine);
    }

    protected AbstractFromClauseStart(final ExpressionSpec[] expressionSpecs,
                                   final FromClauseEngine fromClauseEngine) {
        this.expressionSpecs = expressionSpecs;
        this.fromClauseEngine = fromClauseEngine;
    }

    /**
     * Starts a FROM clause for the given SQL table.
     *
     * @param table the table name.
     * @return the SQL from clause terminal.
     */
    public SqlFromClauseTerminal from(final String table) {
        return fromClauseEngine.from(expressionSpecs, table);
    }
}

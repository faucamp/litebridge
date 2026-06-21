package org.litebridgedb.orm.api.select;

import org.litebridgedb.orm.api.sql.SqlFromClauseTerminal;
import org.litebridgedb.orm.engine.FromClauseEngine;
import org.litebridgedb.orm.expression.ExpressionSpec;

abstract sealed class AbstractFromClauseStart permits FromClauseStart, FromClauseStartTypeOverride{

    protected final ExpressionSpec[] expressionSpecs;
    protected final FromClauseEngine fromClauseEngine;

    protected AbstractFromClauseStart(FromClauseEngine fromClauseEngine) {
        this(new ExpressionSpec[0], fromClauseEngine);
    }

    protected AbstractFromClauseStart(final ExpressionSpec[] expressionSpecs,
                                   final FromClauseEngine fromClauseEngine) {
        this.expressionSpecs = expressionSpecs;
        this.fromClauseEngine = fromClauseEngine;
    }

    public SqlFromClauseTerminal from(final String table) {
        return fromClauseEngine.from(expressionSpecs, table);
    }
}

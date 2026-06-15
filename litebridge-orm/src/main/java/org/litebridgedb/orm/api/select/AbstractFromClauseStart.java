package org.litebridgedb.orm.api.select;

import org.litebridgedb.orm.api.sql.SqlFromClauseTerminal;
import org.litebridgedb.orm.engine.FromClauseEngine;
import org.litebridgedb.orm.function.Expression;

abstract sealed class AbstractFromClauseStart permits FromClauseStart, FromClauseStartTypeOverride{

    protected final Expression[] expressions;
    protected final FromClauseEngine fromClauseEngine;

    protected AbstractFromClauseStart(FromClauseEngine fromClauseEngine) {
        this(new Expression[0], fromClauseEngine);
    }

    protected AbstractFromClauseStart(final Expression[] expressions,
                                   final FromClauseEngine fromClauseEngine) {
        this.expressions = expressions;
        this.fromClauseEngine = fromClauseEngine;
    }

    public SqlFromClauseTerminal from(final String table) {
        return fromClauseEngine.from(expressions, table);
    }
}

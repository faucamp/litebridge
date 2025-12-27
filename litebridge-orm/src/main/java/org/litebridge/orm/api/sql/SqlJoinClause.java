package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.impl.AbstractJoinClause;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.api.select.model.JoinSpec;

public final class SqlJoinClause extends AbstractJoinClause<Row,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal> {

    public SqlJoinClause(final JoinSpec joinSpec, final AbstractSelector<Row> delegate) {
        super(joinSpec, delegate);
    }

    @Override
    public SqlJoinConditionClause on(final String column) {
        final Column spiColumn = new Column(joinSpec.table(), column);
        final SqlJoinConditionClauseTerminal joinConditionClauseTerminal = new SqlJoinConditionClauseTerminal(joinSpec, delegate);
        return new SqlJoinConditionClause(joinSpec.newCondition(spiColumn), joinConditionClauseTerminal);
    }

    @Override
    public SqlJoinConditionClauseTerminal using(final String column) {
        joinSpec.using(column);
        return new SqlJoinConditionClauseTerminal(joinSpec, delegate);
    }
}

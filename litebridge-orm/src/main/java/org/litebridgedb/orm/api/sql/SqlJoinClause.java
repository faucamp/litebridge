package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.orm.api.select.impl.AbstractJoinClause;

public final class SqlJoinClause extends AbstractJoinClause<Row,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal,
        SqlSelectSpec,
        SqlJoinSpec> {

    public SqlJoinClause(final SqlJoinSpec joinSpec, final SqlSelector delegate) {
        super(joinSpec, delegate);
    }

    /**
     * Adds a join ON condition to the current join clause based on the specified lhs.
     * The join condition constrains the relationship between the tables being joined.
     *
     * @param column the name of the lhs to be used in the join condition
     * @return an instance of the join condition clause to allow further configuration
     */
    public SqlJoinConditionClause on(final String column) {
        final Column spiColumn = new Column(joinSpec.table(), column);
        final SqlJoinConditionClauseTerminal joinConditionClauseTerminal = new SqlJoinConditionClauseTerminal(joinSpec, (SqlSelector) delegate);
        return new SqlJoinConditionClause(joinSpec.newCondition(spiColumn), joinConditionClauseTerminal, delegate.litebridgeContext());
    }

    /**
     * Adds a join USING condition to the current join clause using the specified lhs.
     * This method simplifies the join condition by specifying a single lhs that is
     * shared between two tables in the join.
     *
     * @param column the name of the lhs to be used for the join condition
     * @return an instance of the terminal join condition clause to finalize the join conditions
     */
    public SqlJoinConditionClauseTerminal using(final String column) {
        joinSpec.using(column);
        return new SqlJoinConditionClauseTerminal(joinSpec, (SqlSelector) delegate);
    }
}

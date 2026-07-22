package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.ast.JoinNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.impl.AbstractJoinClause;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

public final class SqlJoinClause extends AbstractJoinClause<Row,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal,
        SqlSelectSpec,
        SqlJoinSpec> {

    public SqlJoinClause(final SqlJoinSpec joinSpec, final SqlSelector delegate) {
        super(joinSpec, delegate);
    }

    /**
     * Adds a join ON condition to the current join clause based on the specified column.
     * The join condition constrains the relationship between the tables being joined.
     *
     * @param column the name of the column to be used in the join condition
     * @return an instance of the join condition clause to allow further configuration
     */
    public SqlJoinConditionClause on(final String column) {
        final Column spiColumn = new Column(joinSpec.table(), column);
        final ConditionSpec conditionSpec = joinSpec.currentConditionGroupSpec().newCondition(LogicOperator.NOOP, new SelectColumnSpec(spiColumn));

        final java.util.function.Function<org.litebridge.orm.api.select.ast.QueryNode, SqlJoinConditionClauseTerminal> recreator = n -> new SqlJoinConditionClauseTerminal(joinSpec, (SqlSelector) delegate.withNode(n));

        return new SqlJoinConditionClause(conditionSpec, delegate.litebridgeContext(), LogicOperator.NOOP, new SelectColumnSpec(spiColumn), delegate.node(), recreator);
    }

    /**
     * Adds a join USING condition to the current join clause using the specified column.
     * This method simplifies the join condition by specifying a single column that is
     * shared between two tables in the join.
     *
     * @param column the name of the column to be used for the join condition
     * @return an instance of the terminal join condition clause to finalize the join conditions
     */
    public SqlJoinConditionClauseTerminal using(final String column) {
        final Column spiColumn = new Column(joinSpec.table(), column);
        final ConditionSpec conditionSpec = joinSpec.currentConditionGroupSpec().newCondition(LogicOperator.NOOP, new SelectColumnSpec(spiColumn));
        final java.util.function.Function<org.litebridge.orm.api.select.ast.QueryNode, SqlJoinConditionClauseTerminal> recreator = n -> new SqlJoinConditionClauseTerminal(joinSpec, (SqlSelector) delegate.withNode(n));
        final SqlJoinConditionClause conditionClause = new SqlJoinConditionClause(conditionSpec, delegate.litebridgeContext(), LogicOperator.NOOP, new SelectColumnSpec(spiColumn), delegate.node(), recreator);
        return conditionClause.using(column);
    }
}

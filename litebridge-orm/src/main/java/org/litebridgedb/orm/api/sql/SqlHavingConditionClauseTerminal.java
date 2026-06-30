package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.orm.api.select.HavingConditionClauseTerminal;
import org.litebridgedb.orm.api.select.impl.AbstractHavingClauseTerminal;
import org.litebridgedb.orm.expression.ExpressionSpec;

public final class SqlHavingConditionClauseTerminal
        extends AbstractHavingClauseTerminal<Row,
        SqlOrderByClause,
        SqlOrderByClauseChain,
        SqlSelectSpec>

        implements HavingConditionClauseTerminal<Row,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    public SqlHavingConditionClauseTerminal(final SqlSelector delegate) {
        super(delegate);
    }

    @Override
    public SqlHavingConditionClause and(final String column) {
        final Column spiColumn = new Column(selectSpec.getTable(), column);
        return new SqlHavingConditionClause(selectSpec.newWhereCondition(spiColumn), this, delegate.litebridgeContext());
    }

    @Override
    public SqlHavingConditionClause and(final ExpressionSpec expression) {
        return new SqlHavingConditionClause(selectSpec.newWhereCondition(expression), this, delegate.litebridgeContext());
    }

    @Override
    public SqlOrderByClause orderBy(final String... columns) {
        return new SqlOrderByClause(selectSpec.newOrderBy(selectSpec.createSelectColumnSpecs(columns)), (SqlSelector) delegate);
    }

    @Override
    public SqlOrderByClause orderBy(final ExpressionSpec... columns) {
        return new SqlOrderByClause(selectSpec.newOrderBy(columns), (SqlSelector) delegate);
    }
}

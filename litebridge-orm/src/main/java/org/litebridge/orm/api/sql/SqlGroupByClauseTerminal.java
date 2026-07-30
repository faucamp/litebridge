package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.ast.HavingNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.impl.AbstractGroupByClauseTerminal;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

public class SqlGroupByClauseTerminal extends AbstractGroupByClauseTerminal<Row,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain,
        SqlSelectSpec> {

    public SqlGroupByClauseTerminal(final SqlSelector delegate) {
        super(delegate);
    }

    @Override
    public SqlHavingConditionClause having(final ExpressionSpec expression) {
        return new SqlHavingConditionClause(delegate.litebridgeContext(),
                LogicOperator.NOOP,
                expression,
                null,
                node -> new SqlHavingConditionClauseTerminal((SqlSelector) delegate.withNode(new HavingNode(delegate.node(), node))));
    }

    @Override
    public SqlOrderByClause orderBy(final String... columns) {
        return orderBy(SqlSelectSpec.createSelectColumnSpecs(columns).toArray(ExpressionSpec[]::new));
    }

    @Override
    public SqlOrderByClause orderBy(final ExpressionSpec... columns) {
        return new SqlOrderByClause(columns, (SqlSelector) delegate);
    }
}

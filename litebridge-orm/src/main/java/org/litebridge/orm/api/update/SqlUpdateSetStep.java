package org.litebridge.orm.api.update;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

public final class SqlUpdateSetStep

        extends UpdateSetStep<Row,
        SqlUpdateStep,
        SqlUpdateWhereConditionClause,
        SqlUpdateWhereConditionClauseTerminal> {

    public SqlUpdateSetStep(final String column, final QueryNode node, final Function<QueryNode, SqlUpdateStep> updateStepCreator) {
        super(column, node, updateStepCreator);
    }

    public SqlUpdateSetStep(final ExpressionSpec expressionSpec, final QueryNode node, final Function<QueryNode, SqlUpdateStep> updateStepCreator) {
        super(expressionSpec, node, updateStepCreator);
    }
}

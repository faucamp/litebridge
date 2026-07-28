package org.litebridge.orm.api.sql.update;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.api.sql.condition.SqlConditionClauseStart;
import org.litebridge.orm.api.update.impl.AbstractUpdater;
import org.litebridge.orm.api.update.model.UpdateSpec;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;

import java.util.function.Function;

public final class SqlUpdater extends AbstractUpdater<UpdateSpec> implements SqlUpdateStep {

    public SqlUpdater(final Table table,
                      final TransactionalDatabaseProvider databaseProvider,
                      final SelectExpressionMapper selectExpressionMapper,
                      final LitebridgeContext litebridgeContext) {
        super(new UpdateSpec(table, selectExpressionMapper), databaseProvider, litebridgeContext);
    }

    @Override
    public SqlUpdateWhereConditionClause where(final String column) {
        return whereImpl(LogicOperator.NOOP, column);
    }

    @Override
    public SqlUpdateWhereConditionClause where(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.NOOP, expression);
    }

    @Override
    public SqlUpdateSetStep set(final String column) {
        final Column col = new Column(updateSpec.table(), column);
        return new SqlUpdateSetStep(col, this);
    }

    @Override
    public SqlUpdateSetStep set(final ColumnExpressionSpec column) {
        return new SqlUpdateSetStep(column.getColumn(), this);
    }

    SqlUpdateWhereConditionClause whereImpl(final LogicOperator logicOperator, final String column) {
        return whereImpl(logicOperator, new SelectColumnSpec(new Column(updateSpec.table(), column)));
    }

    SqlUpdateWhereConditionClause whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        //TODO: fix
        final ConditionSpec conditionSpec = updateSpec.currentConditionGroupSpec().newCondition(logicOperator, expression);
        final Function<QueryNode, SqlUpdateWhereConditionClauseTerminal> recreator = n -> new SqlUpdateWhereConditionClauseTerminalImpl(this);
        return new SqlUpdateWhereConditionClause(litebridgeContext, logicOperator, expression, recreator);
    }

    SqlUpdateWhereConditionClauseTerminalImpl whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<Row> query) {
//        final ConditionGroupSpec subgroup = updateSpec.pushConditionGroupSpec(logicOperator);
//        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(subgroup, updateSpec.table(), litebridgeContext.fromClauseEngine());
//        query.apply(conditionClauseStart);
//        updateSpec.popConditionGroupSpec();
//        return new SqlUpdateWhereConditionClauseTerminalImpl(this);
        //TODO: reimplement
        throw new UnsupportedOperationException("Need to reimplement");
    }
}

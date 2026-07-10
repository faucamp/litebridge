package org.litebridgedb.orm.api.sql.update;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.orm.api.condition.QueryConditionBuilder;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.api.select.model.SelectExpressionMapper;
import org.litebridgedb.orm.api.sql.condition.SqlConditionClauseStart;
import org.litebridgedb.orm.api.update.impl.AbstractUpdater;
import org.litebridgedb.orm.api.update.model.UpdateSpec;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;

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
        final ConditionSpec conditionSpec = updateSpec.currentConditionGroupSpec().newCondition(logicOperator, expression);
        return new SqlUpdateWhereConditionClause(conditionSpec, new SqlUpdateWhereConditionClauseTerminalImpl(this), litebridgeContext);
    }

    SqlUpdateWhereConditionClauseTerminalImpl whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder query) {
        final ConditionGroupSpec subgroup = updateSpec.pushConditionGroupSpec(logicOperator);
        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(subgroup, updateSpec.table(), litebridgeContext.fromClauseEngine());
        query.apply(conditionClauseStart);
        updateSpec.popConditionGroupSpec();
        return new SqlUpdateWhereConditionClauseTerminalImpl(this);
    }
}

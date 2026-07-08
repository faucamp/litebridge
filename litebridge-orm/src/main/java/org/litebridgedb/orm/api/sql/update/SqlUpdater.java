package org.litebridgedb.orm.api.sql.update;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.SelectExpressionMapper;
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
        super(new UpdateSpec(selectExpressionMapper), databaseProvider, litebridgeContext);
        updateSpec.setTable(table);
    }

    @Override
    public SqlUpdateWhereConditionClause where(final String column) {
        return where(new SelectColumnSpec(new Column(updateSpec.getTable(), column)));
    }

    @Override
    public SqlUpdateWhereConditionClause where(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.AND, expression);
    }

    @Override
    public SqlUpdateSetStep set(final String column) {
        final Column col = new Column(updateSpec.getTable(), column);
        return new SqlUpdateSetStep(col, this);
    }

    @Override
    public SqlUpdateSetStep set(final ColumnExpressionSpec column) {
        return new SqlUpdateSetStep(column.getColumn(), this);
    }

    private SqlUpdateWhereConditionClause whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final ConditionGroupSpec conditionGroupSpec = updateSpec.newWhereConditionGroup(logicOperator);
        return new SqlUpdateWhereConditionClause(conditionGroupSpec.newCondition(expression), new SqlUpdateWhereConditionClauseTerminalImpl(this), litebridgeContext);
    }
}

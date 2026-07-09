package org.litebridgedb.orm.api.sql.delete;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.orm.api.condition.QueryConditionBuilder;
import org.litebridgedb.orm.api.delete.impl.AbstractDeletor;
import org.litebridgedb.orm.api.delete.model.DeleteSpec;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.api.select.model.SelectExpressionMapper;
import org.litebridgedb.orm.api.sql.condition.SqlConditionClauseStart;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;

public final class SqlDeletor extends AbstractDeletor<DeleteSpec> implements SqlDeleteWhereClause {

    private final LitebridgeContext litebridgeContext;

    public SqlDeletor(final Table table,
                      final TransactionalDatabaseProvider databaseProvider,
                      final SelectExpressionMapper selectExpressionMapper,
                      final LitebridgeContext litebridgeContext) {
        super(new DeleteSpec(table, selectExpressionMapper), databaseProvider);
        this.litebridgeContext = litebridgeContext;
    }

    @Override
    public SqlDeleteWhereConditionClause where(final String column) {
        return whereImpl(LogicOperator.NOOP, column);
    }

    @Override
    public SqlDeleteWhereConditionClause where(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.NOOP, expression);
    }

    SqlDeleteWhereConditionClause whereImpl(final LogicOperator logicOperator, final String column) {
        return whereImpl(logicOperator, new SelectColumnSpec(new Column(deleteSpec.table(), column)));
    }

    SqlDeleteWhereConditionClause whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final ConditionSpec conditionSpec = deleteSpec.currentConditionGroupSpec().newCondition(logicOperator, expression);
        return new SqlDeleteWhereConditionClause(conditionSpec, new SqlDeleteWhereConditionClauseTerminalImpl(this), litebridgeContext);
    }

    SqlDeleteWhereConditionClauseTerminal whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<Row> query) {
        final ConditionGroupSpec subgroup = deleteSpec.pushConditionGroupSpec(logicOperator);
        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(subgroup, deleteSpec.table(), litebridgeContext.fromClauseEngine());
        query.apply(conditionClauseStart);
        return new SqlDeleteWhereConditionClauseTerminalImpl(this);
    }
}

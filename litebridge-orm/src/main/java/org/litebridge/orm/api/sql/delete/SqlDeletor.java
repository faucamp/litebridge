package org.litebridge.orm.api.sql.delete;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.delete.impl.AbstractDeletor;
import org.litebridge.orm.api.delete.model.DeleteSpec;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.api.sql.condition.SqlConditionClauseStart;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;

import java.util.function.Function;

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
        //TODO: fix
        final ConditionSpec conditionSpec = deleteSpec.currentConditionGroupSpec().newCondition(logicOperator, expression);
        final Function<QueryNode, SqlDeleteWhereConditionClauseTerminal> recreator = n -> new SqlDeleteWhereConditionClauseTerminalImpl(this);
        return new SqlDeleteWhereConditionClause(litebridgeContext, logicOperator, expression, recreator);
    }

    SqlDeleteWhereConditionClauseTerminal whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<Row> query) {
//        final ConditionGroupSpec subgroup = deleteSpec.pushConditionGroupSpec(logicOperator);
//        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(subgroup, deleteSpec.table(), litebridgeContext.fromClauseEngine());
//        query.apply(conditionClauseStart);
//        deleteSpec.popConditionGroupSpec();
//        return new SqlDeleteWhereConditionClauseTerminalImpl(this);
        //TODO: reimplement
        throw new UnsupportedOperationException("Need to reimplement");
    }
}

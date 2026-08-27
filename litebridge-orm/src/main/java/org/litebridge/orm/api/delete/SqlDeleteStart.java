package org.litebridge.orm.api.delete;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.ast.DeleteNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.api.update.UpdateStepBase;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

public final class SqlDeleteStart extends UpdateStepBase

        implements DeleteStart<Row,
        SqlDeleteWhereConditionClause,
        SqlDeleteWhereConditionClauseTerminal>,
        DeleteTerminal {

    private final String tableName;
    private final DeleteNode deleteNode;

    public SqlDeleteStart(final String table,
                          final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
        this.tableName = table;
        this.deleteNode = new DeleteNode(null, table, null);
    }

    @Override
    public SqlDeleteWhereConditionClause where(final String column) {
        return whereImpl(column, null);
    }

    @Override
    public SqlDeleteWhereConditionClause where(final ExpressionSpec expression) {
        return whereImpl(null, expression);
    }

    private SqlDeleteWhereConditionClause whereImpl(final @Nullable String column, final @Nullable ExpressionSpec expression) {
        return new SqlDeleteWhereConditionClause(litebridgeContext,
                LogicOperator.NOOP,
                column,
                expression,
                node -> new SqlDeleteWhereConditionClauseTerminalImpl(tableName, new WhereNode(deleteNode, node), litebridgeContext));
    }

    QueryNode node() {
        return deleteNode;
    }
}

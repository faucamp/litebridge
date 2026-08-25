package org.litebridge.orm.api.update;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.ast.UpdateNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

public final class SqlUpdateStart extends UpdateStepBase

        implements UpdateStart<Row,
        SqlUpdateStep,
        SqlUpdateWhereConditionClause,
        SqlUpdateWhereConditionClauseTerminal> {

    private final UpdateNode updateNode;
    private final String tableName;

    public SqlUpdateStart(final String table,
                          final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
        this.updateNode = new UpdateNode(null, table, null);
        this.tableName = table;
    }

    @Override
    public SqlUpdateSetStep set(final String field) {
        return new SqlUpdateSetStep(field, updateNode, node -> new SqlUpdateStep(tableName, node, litebridgeContext));
    }

    @Override
    public SqlUpdateSetStep set(final ExpressionSpec expression) {
        return new SqlUpdateSetStep(expression, updateNode, node -> new SqlUpdateStep(tableName, node, litebridgeContext));
    }
}

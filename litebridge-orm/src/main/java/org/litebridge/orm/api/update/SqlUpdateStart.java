package org.litebridge.orm.api.update;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.UpdateNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * SQL-mode entry step for constructing an {@code UPDATE} statement.
 */
public final class SqlUpdateStart extends UpdateStepBase

        implements UpdateStart<Row,
        SqlUpdateStep,
        SqlUpdateWhereConditionClause,
        SqlUpdateWhereConditionClauseTerminal> {

    private final UpdateNode updateNode;
    private final String tableName;

    /**
     * Creates a new {@code SqlUpdateStart} instance.
     *
     * @param table             the table to update
     * @param litebridgeContext the Litebridge context
     */
    public SqlUpdateStart(final String table,
                          final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
        this.updateNode = new UpdateNode(null, table, null);
        this.tableName = table;
    }

    /**
     * Sets a field to be updated in the SQL {@code UPDATE} statement.
     *
     * @param field the name of the DTO/entity field to be updated
     * @return the next step in the update operation: setting the value of the target field
     */
    @Override
    public SqlUpdateSetStep set(final String field) {
        return new SqlUpdateSetStep(field, updateNode, node -> new SqlUpdateStep(tableName, node, litebridgeContext));
    }

    /**
     * Sets a column to be updated in the SQL {@code UPDATE} statement via an expression.
     *
     * @param expression the expression specifying the target column
     * @return the next step in the update operation: setting the value of the target column
     */
    @Override
    public SqlUpdateSetStep set(final ExpressionSpec expression) {
        return new SqlUpdateSetStep(expression, updateNode, node -> new SqlUpdateStep(tableName, node, litebridgeContext));
    }
}

package org.litebridge.orm.api.update;

import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

public final class SqlUpdateStep extends UpdateStepBase
        implements UpdateStep<Row,
        SqlUpdateWhereConditionClause,
        SqlUpdateWhereConditionClauseTerminal> {

    private final String tableName;
    private QueryNode node;

    public SqlUpdateStep(final String tableName,
                         final QueryNode node,
                         final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
        this.tableName = tableName;
        this.node = node;
    }

    @Override
    public SqlUpdateSetStep set(final String field) {
        return new SqlUpdateSetStep(field, node, node -> {
            this.node = node;
            return this;
        });
    }

    @Override
    public SqlUpdateSetStep set(final ExpressionSpec expression) {
        return new SqlUpdateSetStep(expression, node, node -> {
            this.node = node;
            return this;
        });
    }

    @Override
    public SqlUpdateWhereConditionClause where(final String column) {
        final Table table = litebridgeContext.tableRegistry().getOrCreateSpiTable(tableName);
        final ColumnMetaData columnMetaData = litebridgeContext.tableMetaDataCache()
                .ensureTableMetaData(table)
                .column(column);
        return where(new SelectColumnSpec(columnMetaData.toColumn()));
    }

    @Override
    public SqlUpdateWhereConditionClause where(final ExpressionSpec expression) {
        return new SqlUpdateWhereConditionClause(litebridgeContext,
                LogicOperator.NOOP,
                expression,
                node -> new SqlUpdateWhereConditionClauseTerminalImpl(tableName, new WhereNode(this.node, node), litebridgeContext));
    }
}

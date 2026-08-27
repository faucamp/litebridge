package org.litebridge.orm.api.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.ast.OrderByNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.Objects;

public final class SqlOrderByClause implements OrderByClause<Row, SqlOrderByClause, SqlOrderByClauseChain> {

    private final String @Nullable [] columns;
    private final ExpressionSpec @Nullable [] expressions;
    private QueryNode node;
    private final SelectEngineTerminal selectEngineTerminal;
    private final LitebridgeContext litebridgeContext;

    public SqlOrderByClause(final ExpressionSpec[] expressions,
                            final QueryNode node,
                            final SelectEngineTerminal selectEngineTerminal,
                            final LitebridgeContext litebridgeContext) {
        this(null, expressions, node, selectEngineTerminal, litebridgeContext);
    }

    public SqlOrderByClause(final String[] columns,
                            final QueryNode node,
                            final SelectEngineTerminal selectEngineTerminal,
                            final LitebridgeContext litebridgeContext) {
        this(columns, null, node, selectEngineTerminal, litebridgeContext);
    }

    private SqlOrderByClause(
            final String @Nullable [] columns,
            final ExpressionSpec @Nullable [] expressions,
            final QueryNode node,
            final SelectEngineTerminal selectEngineTerminal,
            final LitebridgeContext litebridgeContext) {
        this.columns = columns;
        this.expressions = expressions;
        this.node = node;
        this.selectEngineTerminal = selectEngineTerminal;
        this.litebridgeContext = litebridgeContext;
    }

    @Override
    public SqlOrderByClauseChain asc() {
        return createSqlOrderByClauseChain(true);
    }

    @Override
    public SqlOrderByClauseChain desc() {
        return createSqlOrderByClauseChain(false);
    }

    private SqlOrderByClauseChain createSqlOrderByClauseChain(final boolean ascending) {
        if (expressions != null) {
            for (final ExpressionSpec expression : expressions) {
                node = new OrderByNode(node, null, expression, ascending);
            }
        } else {
            for (final String column : Objects.requireNonNull(columns)) {
                node = new OrderByNode(node, column, null, ascending);
            }
        }

        return new SqlOrderByClauseChain(node, selectEngineTerminal, litebridgeContext);
    }
}

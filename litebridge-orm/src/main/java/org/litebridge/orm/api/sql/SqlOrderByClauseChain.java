package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.impl.OrderByClauseTerminalImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.expression.ExpressionSpec;

public final class SqlOrderByClauseChain
        extends OrderByClauseTerminalImpl<Row>
        implements OrderByClauseChain<Row, SqlOrderByClause, SqlOrderByClauseChain> {

    public SqlOrderByClauseChain(final QueryNode node, final SelectEngineTerminal selectEngineTerminal, final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlOrderByClause then(final String... columns) {
//        return new SqlOrderByClause(SqlSelectSpec.createSelectColumnSpecs(columns).toArray(ExpressionSpec[]::new), (SqlSelector) delegate);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public SqlOrderByClause then(final ExpressionSpec... columns) {
//        return new SqlOrderByClause(columns, (SqlSelector) delegate);
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Row;
import org.litebridgedb.orm.api.select.OrderByClauseChain;
import org.litebridgedb.orm.api.select.impl.OrderByClauseTerminalImpl;
import org.litebridgedb.orm.expression.ExpressionSpec;

public final class SqlOrderByClauseChain
        extends OrderByClauseTerminalImpl<Row, SqlSelectSpec>
        implements OrderByClauseChain<Row, SqlOrderByClause, SqlOrderByClauseChain> {

    public SqlOrderByClauseChain(final SqlSelector delegate) {
        super(delegate);
    }

    @Override
    public SqlOrderByClause then(final String... columns) {
        return new SqlOrderByClause(selectSpec.newOrderBy(selectSpec.createSelectColumnSpecs(columns)), (SqlSelector) delegate);
    }

    @Override
    public SqlOrderByClause then(final ExpressionSpec... columns) {
        return new SqlOrderByClause(selectSpec.newOrderBy(columns), (SqlSelector) delegate);
    }
}

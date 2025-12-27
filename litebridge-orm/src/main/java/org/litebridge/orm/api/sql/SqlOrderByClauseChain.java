package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.api.select.impl.OrderByClauseTerminalImpl;

public final class SqlOrderByClauseChain
        extends OrderByClauseTerminalImpl<Row>
        implements OrderByClauseChain<Row, SqlOrderByClause, SqlOrderByClauseChain> {

    public SqlOrderByClauseChain(final SqlSelector delegate) {
        super(delegate);
    }

    @Override
    public SqlOrderByClause then(final String... columns) {
        return new SqlOrderByClause(selectSpec.newOrderBy(columns), (SqlSelector) delegate);
    }
}

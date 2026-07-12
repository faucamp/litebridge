package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.model.OrderBySpec;

public final class SqlOrderByClause implements OrderByClause<Row, SqlOrderByClause, SqlOrderByClauseChain> {

    private final OrderBySpec orderBySpec;
    private final SqlSelector delegate;

    public SqlOrderByClause(final OrderBySpec orderBySpec, final SqlSelector delegate) {
        this.orderBySpec = orderBySpec;
        this.delegate = delegate;
    }

    @Override
    public SqlOrderByClauseChain asc() {
        orderBySpec.setAsc(true);
        return new SqlOrderByClauseChain(delegate);
    }

    @Override
    public SqlOrderByClauseChain desc() {
        orderBySpec.setAsc(false);
        return new SqlOrderByClauseChain(delegate);
    }
}

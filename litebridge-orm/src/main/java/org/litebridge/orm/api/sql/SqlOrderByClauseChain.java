package org.litebridge.orm.api.sql;

import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.api.select.impl.OrderByClauseTerminalImpl;

import java.util.LinkedHashMap;

public final class SqlOrderByClauseChain
        extends OrderByClauseTerminalImpl<LinkedHashMap<String, Object>>
        implements OrderByClauseChain<LinkedHashMap<String, Object>, SqlOrderByClause, SqlOrderByClauseChain> {

    public SqlOrderByClauseChain(final SqlSelector delegate) {
        super(delegate);
    }

    @Override
    public SqlOrderByClause then(final String... columns) {
        return new SqlOrderByClause(selectSpec.newOrderBy(columns), (SqlSelector) delegate);
    }
}

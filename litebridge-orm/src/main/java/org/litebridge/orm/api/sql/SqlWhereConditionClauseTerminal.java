package org.litebridge.orm.api.sql;

import org.litebridge.orm.api.select.WhereConditionClauseTerminal;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.api.select.impl.AbstractWhereClauseTerminal;

import java.util.LinkedHashMap;

public final class SqlWhereConditionClauseTerminal
        extends AbstractWhereClauseTerminal<LinkedHashMap<String, Object>, SqlOrderByClause, SqlOrderByClauseChain>
        implements WhereConditionClauseTerminal<LinkedHashMap<String, Object>,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    public SqlWhereConditionClauseTerminal(final AbstractSelector<LinkedHashMap<String, Object>> delegate) {
        super(delegate);
    }

    @Override
    public SqlWhereConditionClause and(final String column) {
        return new SqlWhereConditionClause(selectSpec.newWhereCondition(column), this);
    }

    @Override
    public SqlOrderByClause orderBy(final String... columns) {
        return new SqlOrderByClause(selectSpec.newOrderBy(columns), (SqlSelector) delegate);
    }
}

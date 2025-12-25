package org.litebridge.orm.api.sql;

import org.litebridge.orm.api.select.WhereConditionClauseTerminal;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.api.select.impl.WhereClauseTerminalImpl;

import java.util.LinkedHashMap;

public class SqlWhereConditionClauseTerminal
        extends WhereClauseTerminalImpl<LinkedHashMap<String, Object>>
        implements WhereConditionClauseTerminal<LinkedHashMap<String, Object>, SqlWhereConditionClause, SqlWhereConditionClauseTerminal> {

    public SqlWhereConditionClauseTerminal(final AbstractSelector<LinkedHashMap<String, Object>> delegate) {
        super(delegate);
    }

    @Override
    public SqlWhereConditionClause and(final String column) {
        return new SqlWhereConditionClause(selectSpec.newWhereCondition(column), this);
    }
}

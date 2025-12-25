package org.litebridge.orm.api.sql;

import org.litebridge.orm.api.select.WhereConditionClause;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.api.select.model.ConditionSpec;

import java.util.LinkedHashMap;

public class SqlWhereConditionClause
        extends ConditionClauseImpl<LinkedHashMap<String, Object>,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal>

        implements WhereConditionClause<LinkedHashMap<String, Object>, SqlWhereConditionClause, SqlWhereConditionClauseTerminal> {

    public SqlWhereConditionClause(final ConditionSpec conditionSpec, final SqlWhereConditionClauseTerminal conditionTerminal) {
        super(conditionSpec, conditionTerminal);
    }
}

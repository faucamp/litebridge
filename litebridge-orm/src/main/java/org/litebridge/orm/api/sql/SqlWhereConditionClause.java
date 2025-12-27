package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.WhereConditionClause;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.api.select.model.ConditionSpec;

public final class SqlWhereConditionClause
        extends ConditionClauseImpl<Row,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal>

        implements WhereConditionClause<Row,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    public SqlWhereConditionClause(final ConditionSpec conditionSpec, final SqlWhereConditionClauseTerminal conditionTerminal) {
        super(conditionSpec, conditionTerminal);
    }
}

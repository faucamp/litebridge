package org.litebridgedb.orm.api.sql.update;

import org.litebridgedb.db.spi.Row;
import org.litebridgedb.orm.api.select.impl.ConditionClauseImpl;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.api.update.UpdateWhereConditionClause;

public class SqlUpdateWhereConditionClause

        extends ConditionClauseImpl<Row,
        SqlUpdateWhereConditionClause,
        SqlUpdateWhereConditionClauseTerminal>

        implements UpdateWhereConditionClause<Row,
        SqlUpdateWhereConditionClause,
        SqlUpdateWhereConditionClauseTerminal> {

    public SqlUpdateWhereConditionClause(final ConditionSpec conditionSpec, final SqlUpdateWhereConditionClauseTerminal conditionTerminal) {
        super(conditionSpec, conditionTerminal);
    }
}

package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.WhereConditionClause;
import org.litebridge.orm.api.select.WhereConditionClauseTerminal;
import org.litebridge.orm.api.select.model.ConditionSpec;

public class WhereConditionClauseImpl<DTO>
        extends ConditionClauseImpl<DTO, WhereConditionClause<DTO>, WhereConditionClauseTerminal<DTO>>
        implements WhereConditionClause<DTO> {

    public WhereConditionClauseImpl(final ConditionSpec conditionSpec, final WhereConditionClauseTerminal<DTO> conditionTerminal) {
        super(conditionSpec, conditionTerminal);
    }
}

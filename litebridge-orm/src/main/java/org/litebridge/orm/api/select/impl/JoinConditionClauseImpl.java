package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.JoinConditionClauseTerminal;

public class JoinConditionClauseImpl<DTO> extends ConditionClauseImpl<DTO, JoinConditionClauseTerminal<DTO>> implements JoinConditionClause<DTO> {

    public JoinConditionClauseImpl(final ConditionSpec condition, final JoinConditionClauseTerminal<DTO> conditionTerminal) {
        super(condition, conditionTerminal);
    }
}

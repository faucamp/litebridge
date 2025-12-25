package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.model.JoinSpec;
import org.litebridge.orm.api.select.JoinClause;
import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.JoinConditionClauseTerminal;
import org.litebridge.orm.api.select.WhereConditionClause;

public class JoinConditionClauseTerminalImpl<DTO>
        extends WhereClauseTerminalImpl<DTO>
        implements JoinConditionClauseTerminal<DTO, JoinConditionClauseImpl<DTO, JoinConditionClauseTerminalImpl<DTO>>, JoinConditionClauseTerminalImpl<DTO>> {

    private final JoinSpec joinSpec;

    public JoinConditionClauseTerminalImpl(final JoinSpec joinSpec, final AbstractSelector<DTO> delegate) {
        super(delegate);
        this.joinSpec = joinSpec;
    }

    @Override
    public JoinConditionClauseImpl<DTO, JoinConditionClauseTerminalImpl<DTO>> and(final String column) {
        return new JoinConditionClauseImpl<>(joinSpec.newCondition(), this);
    }

//    @Override
    public WhereConditionClause<DTO> where(final String column) {
        return null;
    }

//    @Override
    public JoinClauseImpl<DTO> join(final String table) {
        return new JoinClauseImpl<>(selectSpec.newJoinSpec(table), delegate);
    }
}

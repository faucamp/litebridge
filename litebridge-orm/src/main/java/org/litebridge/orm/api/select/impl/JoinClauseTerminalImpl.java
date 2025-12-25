package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.JoinClause;
import org.litebridge.orm.api.select.JoinClauseTerminal;
import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.JoinConditionClauseTerminal;
import org.litebridge.orm.api.select.WhereConditionClause;

public class JoinClauseTerminalImpl<DTO,
        JC extends JoinClause<DTO, JCC, SELF>,
        JCC extends JoinConditionClause<DTO, JCC, SELF>,
        SELF extends JoinConditionClauseTerminal<DTO, JCC, SELF>>

        extends WhereClauseTerminalImpl<DTO>
        implements JoinClauseTerminal<DTO, JC, JCC, SELF> {

    public JoinClauseTerminalImpl(final AbstractSelector<DTO> delegate) {
        super(delegate);
    }

    @Override
    public WhereConditionClause<DTO> where(final String column) {
        return new WhereConditionClauseImpl<>(selectSpec.newWhereCondition(column), new WhereConditionClauseTerminalImpl<>(delegate));
    }

    @Override
    public JC join(final String table) {
        return (JC) new JoinClauseImpl<>(selectSpec.newJoinSpec(table), delegate);
    }
}

package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.JoinConditionClauseTerminal;
import org.litebridge.orm.api.select.model.JoinSpec;

public abstract class AbstractJoinConditionClauseTerminal<DTO,
        JCC extends JoinConditionClause<DTO, JCC, SELF>,
        SELF extends JoinConditionClauseTerminal<DTO, JCC, SELF>>

        extends WhereClauseTerminalImpl<DTO>
        implements JoinConditionClauseTerminal<DTO, JCC, SELF> {

    protected final JoinSpec joinSpec;

    public AbstractJoinConditionClauseTerminal(final JoinSpec joinSpec, final AbstractSelector<DTO> delegate) {
        super(delegate);
        this.joinSpec = joinSpec;
    }
}

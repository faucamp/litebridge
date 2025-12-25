package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.JoinClause;
import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.JoinConditionClauseTerminal;
import org.litebridge.orm.api.select.model.JoinSpec;

public abstract class AbstractJoinClause<DTO,
        JCC extends JoinConditionClause<DTO, JCC, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, JCC, JCCT>>

        implements JoinClause<DTO, JCC, JCCT> {

    protected final AbstractSelector<DTO> delegate;
    protected final JoinSpec joinSpec;

    public AbstractJoinClause(final JoinSpec joinSpec, final AbstractSelector<DTO> delegate) {
        this.joinSpec = joinSpec;
        this.delegate = delegate;
    }
}

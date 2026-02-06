package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.JoinConditionClauseTerminal;
import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.api.select.model.JoinSpec;
import org.litebridge.orm.api.select.model.SelectSpec;

public abstract class AbstractJoinConditionClauseTerminal<DTO,
        JCC extends JoinConditionClause<DTO, JCC, SELF>,
        SELF extends JoinConditionClauseTerminal<DTO, JCC, SELF>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>,
        SSP extends SelectSpec>

        extends AbstractWhereClauseTerminal<DTO, OBC, OBCC, SSP>
        implements JoinConditionClauseTerminal<DTO, JCC, SELF> {

    protected final JoinSpec joinSpec;

    public AbstractJoinConditionClauseTerminal(final JoinSpec joinSpec, final AbstractSelector<DTO, SSP> delegate) {
        super(delegate);
        this.joinSpec = joinSpec;
    }
}

package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.JoinClause;
import org.litebridge.orm.api.select.JoinClauseTerminal;
import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.JoinConditionClauseTerminal;
import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.api.select.WhereConditionClause;
import org.litebridge.orm.api.select.WhereConditionClauseTerminal;
import org.litebridge.orm.api.select.model.SelectSpec;

public abstract class AbstractJoinClauseTerminal<DTO,
        JC extends JoinClause<DTO, JCC, SELF>,
        JCC extends JoinConditionClause<DTO, JCC, SELF>,
        SELF extends JoinConditionClauseTerminal<DTO, JCC, SELF>,
        WCC extends WhereConditionClause<DTO, WCC, WCCT, OBC, OBCC>,
        WCCT extends WhereConditionClauseTerminal<DTO, WCC, WCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>,
        SSP extends SelectSpec>

        extends AbstractWhereClauseTerminal<DTO, OBC, OBCC, SSP>
        implements JoinClauseTerminal<DTO, JC, JCC, SELF, WCC, WCCT, OBC, OBCC> {

    public AbstractJoinClauseTerminal(final AbstractSelector<DTO, SSP> delegate) {
        super(delegate);
    }
}

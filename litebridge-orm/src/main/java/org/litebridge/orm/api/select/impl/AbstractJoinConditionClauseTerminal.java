package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.GroupByClauseTerminal;
import org.litebridge.orm.api.select.HavingConditionClause;
import org.litebridge.orm.api.select.HavingConditionClauseTerminal;
import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.JoinConditionClauseTerminal;
import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.api.select.ast.JoinNode;
import org.litebridge.orm.api.select.model.JoinSpec;
import org.litebridge.orm.api.select.model.SelectSpec;

public abstract class AbstractJoinConditionClauseTerminal<DTO,
        JCC extends JoinConditionClause<DTO, JCC, SELF>,
        SELF extends JoinConditionClauseTerminal<DTO, JCC, SELF>,
        GBCT extends GroupByClauseTerminal<DTO, HCC, HCCT, OBC, OBCC>,
        HCC extends HavingConditionClause<DTO, HCC, HCCT, OBC, OBCC>,
        HCCT extends HavingConditionClauseTerminal<DTO, HCC, HCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>,
        SSP extends SelectSpec,
        JSP extends JoinSpec>

        extends AbstractWhereClauseTerminal<DTO, GBCT, HCC, HCCT, OBC, OBCC, SSP>
        implements JoinConditionClauseTerminal<DTO, JCC, SELF> {

    protected final JoinNode joinNode;

    public AbstractJoinConditionClauseTerminal(final JoinNode joinNode, final AbstractSelector<DTO, SSP> delegate) {
        super(delegate);
        this.joinNode = joinNode;
    }
}

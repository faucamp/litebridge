package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.FromClauseTerminal;
import org.litebridge.orm.api.select.GroupByClauseTerminal;
import org.litebridge.orm.api.select.HavingConditionClause;
import org.litebridge.orm.api.select.HavingConditionClauseTerminal;
import org.litebridge.orm.api.select.JoinClause;
import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.JoinConditionClauseTerminal;
import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.api.select.WhereConditionClause;
import org.litebridge.orm.api.select.WhereConditionClauseTerminal;
import org.litebridge.orm.api.select.model.SelectSpec;

public abstract class AbstractFromClauseTerminal<DTO,
        JC extends JoinClause<DTO, JCC, JCCT>,
        JCC extends JoinConditionClause<DTO, JCC, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, JCC, JCCT>,
        WCC extends WhereConditionClause<DTO, WCC, WCCT, GBCT, HCC, HCCT, OBC, OBCC>,
        WCCT extends WhereConditionClauseTerminal<DTO, WCC, WCCT, GBCT, HCC, HCCT, OBC, OBCC>,
        GBCT extends GroupByClauseTerminal<DTO, HCC, HCCT, OBC, OBCC>,
        HCC extends HavingConditionClause<DTO, HCC, HCCT, OBC, OBCC>,
        HCCT extends HavingConditionClauseTerminal<DTO, HCC, HCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>,
        SSP extends SelectSpec>

        extends AbstractJoinClauseTerminal<DTO, JC, JCC, JCCT, WCC, WCCT, GBCT, HCC, HCCT, OBC, OBCC, SSP>
        implements FromClauseTerminal<DTO, JC, JCC, JCCT, WCC, WCCT, GBCT, HCC, HCCT, OBC, OBCC> {

    public AbstractFromClauseTerminal(final AbstractSelector<DTO, SSP> delegate) {
        super(delegate);
    }
}

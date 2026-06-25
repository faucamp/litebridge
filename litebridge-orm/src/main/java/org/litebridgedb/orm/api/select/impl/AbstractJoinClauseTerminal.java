package org.litebridgedb.orm.api.select.impl;

import org.litebridgedb.orm.api.select.GroupByClauseTerminal;
import org.litebridgedb.orm.api.select.HavingConditionClause;
import org.litebridgedb.orm.api.select.HavingConditionClauseTerminal;
import org.litebridgedb.orm.api.select.JoinClause;
import org.litebridgedb.orm.api.select.JoinClauseTerminal;
import org.litebridgedb.orm.api.select.JoinConditionClause;
import org.litebridgedb.orm.api.select.JoinConditionClauseTerminal;
import org.litebridgedb.orm.api.select.OrderByClause;
import org.litebridgedb.orm.api.select.OrderByClauseChain;
import org.litebridgedb.orm.api.select.WhereConditionClause;
import org.litebridgedb.orm.api.select.WhereConditionClauseTerminal;
import org.litebridgedb.orm.api.select.model.SelectSpec;

public abstract class AbstractJoinClauseTerminal<DTO,
        JC extends JoinClause<DTO, JCC, SELF>,
        JCC extends JoinConditionClause<DTO, JCC, SELF>,
        SELF extends JoinConditionClauseTerminal<DTO, JCC, SELF>,
        WCC extends WhereConditionClause<DTO, WCC, WCCT, GBCT, HCC, HCCT, OBC, OBCC>,
        WCCT extends WhereConditionClauseTerminal<DTO, WCC, WCCT, GBCT, HCC, HCCT, OBC, OBCC>,
        GBCT extends GroupByClauseTerminal<DTO, HCC, HCCT, OBC, OBCC>,
        HCC extends HavingConditionClause<DTO, HCC, HCCT, OBC, OBCC>,
        HCCT extends HavingConditionClauseTerminal<DTO, HCC, HCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>,
        SSP extends SelectSpec>

        extends AbstractWhereClauseTerminal<DTO, GBCT, HCC, HCCT, OBC, OBCC, SSP>
        implements JoinClauseTerminal<DTO, JC, JCC, SELF, WCC, WCCT, GBCT, HCC, HCCT, OBC, OBCC> {

    public AbstractJoinClauseTerminal(final AbstractSelector<DTO, SSP> delegate) {
        super(delegate);
    }
}

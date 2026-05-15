package org.litebridgedb.orm.api.select.impl;

import org.litebridgedb.orm.api.select.FromClauseTerminal;
import org.litebridgedb.orm.api.select.JoinClause;
import org.litebridgedb.orm.api.select.JoinConditionClause;
import org.litebridgedb.orm.api.select.JoinConditionClauseTerminal;
import org.litebridgedb.orm.api.select.OrderByClause;
import org.litebridgedb.orm.api.select.OrderByClauseChain;
import org.litebridgedb.orm.api.select.WhereConditionClause;
import org.litebridgedb.orm.api.select.WhereConditionClauseTerminal;
import org.litebridgedb.orm.api.select.model.SelectSpec;

public abstract class AbstractFromClauseTerminal<DTO,
        JC extends JoinClause<DTO, JCC, JCCT>,
        JCC extends JoinConditionClause<DTO, JCC, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, JCC, JCCT>,
        WCC extends WhereConditionClause<DTO, WCC, WCCT, OBC, OBCC>,
        WCCT extends WhereConditionClauseTerminal<DTO, WCC, WCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>,
        SSP extends SelectSpec>

        extends AbstractJoinClauseTerminal<DTO, JC, JCC, JCCT, WCC, WCCT, OBC, OBCC, SSP>
        implements FromClauseTerminal<DTO, JC, JCC, JCCT, WCC, WCCT, OBC, OBCC> {

    public AbstractFromClauseTerminal(final AbstractSelector<DTO, SSP> delegate) {
        super(delegate);
    }
}

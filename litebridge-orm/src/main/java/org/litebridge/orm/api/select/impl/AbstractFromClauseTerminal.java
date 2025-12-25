package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.FromClauseTerminal;
import org.litebridge.orm.api.select.JoinClause;
import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.JoinConditionClauseTerminal;
import org.litebridge.orm.api.select.WhereConditionClause;
import org.litebridge.orm.api.select.WhereConditionClauseTerminal;

public abstract class AbstractFromClauseTerminal<DTO,
        JC extends JoinClause<DTO, JCC, JCCT>,
        JCC extends JoinConditionClause<DTO, JCC, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, JCC, JCCT>,
        WCC extends WhereConditionClause<DTO, WCC, WCCT>,
        WCCT extends WhereConditionClauseTerminal<DTO, WCC, WCCT>>

        extends AbstractJoinClauseTerminal<DTO, JC, JCC, JCCT, WCC, WCCT>
        implements FromClauseTerminal<DTO, JC, JCC, JCCT, WCC, WCCT> {

    public AbstractFromClauseTerminal(final AbstractSelector<DTO> delegate) {
        super(delegate);
    }
}

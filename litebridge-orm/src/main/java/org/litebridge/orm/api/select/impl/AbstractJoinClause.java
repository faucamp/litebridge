package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.JoinClause;
import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.JoinConditionClauseTerminal;
import org.litebridge.orm.api.select.model.JoinSpec;
import org.litebridge.orm.api.select.model.SelectSpec;

public abstract class AbstractJoinClause<DTO,
        JCC extends JoinConditionClause<DTO, JCC, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, JCC, JCCT>,
        SSP extends SelectSpec,
        JSP extends JoinSpec>

        implements JoinClause<DTO, JCC, JCCT> {

    protected final AbstractSelector<DTO, SSP> delegate;

    public AbstractJoinClause(final AbstractSelector<DTO, SSP> delegate) {
        this.delegate = delegate;
    }
}

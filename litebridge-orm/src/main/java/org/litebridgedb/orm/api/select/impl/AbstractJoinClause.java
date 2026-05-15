package org.litebridgedb.orm.api.select.impl;

import org.litebridgedb.orm.api.select.JoinClause;
import org.litebridgedb.orm.api.select.JoinConditionClause;
import org.litebridgedb.orm.api.select.JoinConditionClauseTerminal;
import org.litebridgedb.orm.api.select.model.JoinSpec;
import org.litebridgedb.orm.api.select.model.SelectSpec;

public abstract class AbstractJoinClause<DTO,
        JCC extends JoinConditionClause<DTO, JCC, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, JCC, JCCT>,
        SSP extends SelectSpec,
        JSP extends JoinSpec>

        implements JoinClause<DTO, JCC, JCCT> {

    protected final AbstractSelector<DTO, SSP> delegate;
    protected final JSP joinSpec;

    public AbstractJoinClause(final JSP joinSpec, final AbstractSelector<DTO, SSP> delegate) {
        this.joinSpec = joinSpec;
        this.delegate = delegate;
    }
}

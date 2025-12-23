package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.model.JoinSpec;
import org.litebridge.orm.api.select.JoinClause;
import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.JoinConditionClauseTerminal;

public class JoinClauseImpl<DTO> implements JoinClause<DTO> {

    private final AbstractSelector<DTO> delegate;
    private final JoinSpec joinSpec;

    public JoinClauseImpl(final JoinSpec joinSpec, final AbstractSelector<DTO> delegate) {
        this.joinSpec = joinSpec;
        this.delegate = delegate;
    }

    @Override
    public JoinConditionClause<DTO> on(final String column) {
        final JoinConditionClauseTerminal<DTO> joinConditionClauseTerminal = new JoinConditionClauseTerminalImpl<>(joinSpec, delegate);
        return new JoinConditionClauseImpl<>(joinSpec.newCondition(), joinConditionClauseTerminal);
    }
}

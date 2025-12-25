package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.FromClauseTerminal;
import org.litebridge.orm.api.select.JoinClause;
import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.JoinConditionClauseTerminal;

public class FromClauseTerminalImpl<DTO,
        JC extends JoinClause<DTO, JCC, JCCT>,
        JCC extends JoinConditionClause<DTO, JCC, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, JCC, JCCT>>

        extends JoinClauseTerminalImpl<DTO, JC, JCC, JCCT>
        implements FromClauseTerminal<DTO, JC, JCC, JCCT> {

    public FromClauseTerminalImpl(final AbstractSelector<DTO> delegate) {
        super(delegate);
    }
}

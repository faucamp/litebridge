package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.api.select.WhereClauseTerminal;

public abstract class AbstractWhereClauseTerminal<DTO,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends OrderByClauseTerminalImpl<DTO>
        implements WhereClauseTerminal<DTO, OBC, OBCC> {

    public AbstractWhereClauseTerminal(final AbstractSelector<DTO> delegate) {
        super(delegate);
    }

}

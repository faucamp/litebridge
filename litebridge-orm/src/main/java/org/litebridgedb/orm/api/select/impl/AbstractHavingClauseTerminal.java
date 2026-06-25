package org.litebridgedb.orm.api.select.impl;

import org.litebridgedb.orm.api.select.HavingClauseTerminal;
import org.litebridgedb.orm.api.select.OrderByClause;
import org.litebridgedb.orm.api.select.OrderByClauseChain;
import org.litebridgedb.orm.api.select.model.SelectSpec;

public abstract class AbstractHavingClauseTerminal<DTO,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>,
        SSP extends SelectSpec>

        extends OrderByClauseTerminalImpl<DTO, SSP>
        implements HavingClauseTerminal<DTO, OBC, OBCC> {

    public AbstractHavingClauseTerminal(final AbstractSelector<DTO, SSP> delegate) {
        super(delegate);
    }

}

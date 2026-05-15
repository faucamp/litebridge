package org.litebridgedb.orm.api.select.impl;

import org.litebridgedb.orm.api.select.OrderByClause;
import org.litebridgedb.orm.api.select.OrderByClauseChain;
import org.litebridgedb.orm.api.select.WhereClauseTerminal;
import org.litebridgedb.orm.api.select.model.SelectSpec;

public abstract class AbstractWhereClauseTerminal<DTO,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>,
        SSP extends SelectSpec>

        extends OrderByClauseTerminalImpl<DTO, SSP>
        implements WhereClauseTerminal<DTO, OBC, OBCC> {

    public AbstractWhereClauseTerminal(final AbstractSelector<DTO, SSP> delegate) {
        super(delegate);
    }

}

package org.litebridgedb.orm.api.select.impl;

import org.litebridgedb.orm.api.select.GroupByClauseTerminal;
import org.litebridgedb.orm.api.select.HavingConditionClause;
import org.litebridgedb.orm.api.select.HavingConditionClauseTerminal;
import org.litebridgedb.orm.api.select.OrderByClause;
import org.litebridgedb.orm.api.select.OrderByClauseChain;
import org.litebridgedb.orm.api.select.model.SelectSpec;

public abstract class AbstractGroupByClauseTerminal<DTO,
        HCC extends HavingConditionClause<DTO, HCC, HCCT, OBC, OBCC>,
        HCCT extends HavingConditionClauseTerminal<DTO, HCC, HCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>,
        SSP extends SelectSpec>

        extends OrderByClauseTerminalImpl<DTO, SSP>
        implements GroupByClauseTerminal<DTO, HCC, HCCT, OBC, OBCC> {

    public AbstractGroupByClauseTerminal(final AbstractSelector<DTO, SSP> delegate) {
        super(delegate);
    }

}

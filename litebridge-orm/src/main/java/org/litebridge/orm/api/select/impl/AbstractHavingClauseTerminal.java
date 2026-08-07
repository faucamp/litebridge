package org.litebridge.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.dto.DtoHavingConditionClauseTerminal;
import org.litebridge.orm.api.select.HavingClauseTerminal;
import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.api.sql.SqlHavingConditionClauseTerminal;

public sealed abstract class AbstractHavingClauseTerminal<DTO,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>,
        SSP extends SelectSpec>

        extends OrderByClauseTerminalImpl<DTO, SSP>
        implements HavingClauseTerminal<DTO, OBC, OBCC>

        permits DtoHavingConditionClauseTerminal, SqlHavingConditionClauseTerminal {

    public AbstractHavingClauseTerminal(final AbstractSelector<DTO, SSP> delegate) {
        super(delegate);
    }

    @Nullable QueryNode node() {
        return delegate.node();
    }
}

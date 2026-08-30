package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.dto.DtoHavingConditionClauseTerminal;
import org.litebridge.orm.api.select.HavingClauseTerminal;
import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.api.sql.SqlHavingConditionClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.QueryNode;

public sealed abstract class AbstractHavingClauseTerminal<DTO,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends OrderByClauseTerminalImpl<DTO>
        implements HavingClauseTerminal<DTO, OBC, OBCC>

        permits DtoHavingConditionClauseTerminal, SqlHavingConditionClauseTerminal {

    public AbstractHavingClauseTerminal(final QueryNode node, final SelectEngineTerminal selectEngineTerminal, final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
    }
}

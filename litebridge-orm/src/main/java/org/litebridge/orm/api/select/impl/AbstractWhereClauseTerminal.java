package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.GroupByClauseTerminal;
import org.litebridge.orm.api.select.HavingConditionClause;
import org.litebridge.orm.api.select.HavingConditionClauseTerminal;
import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.api.select.WhereClauseTerminal;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;

/**
 * Abstract base class for WHERE clause terminals.
 *
 * @param <DTO>  the mapped DTO/entity type or row type
 * @param <GBCT> the group by clause terminal type
 * @param <HCC>  the having condition clause type
 * @param <HCCT> the having condition clause terminal type
 * @param <OBC>  the order by clause type
 * @param <OBCC> the order by clause chain type
 */
public abstract class AbstractWhereClauseTerminal<DTO,
        GBCT extends GroupByClauseTerminal<DTO, HCC, HCCT, OBC, OBCC>,
        HCC extends HavingConditionClause<DTO, HCC, HCCT, OBC, OBCC>,
        HCCT extends HavingConditionClauseTerminal<DTO, HCC, HCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends OrderByClauseTerminalImpl<DTO>
        implements WhereClauseTerminal<DTO, GBCT, HCC, HCCT, OBC, OBCC> {

    /**
     * Creates a new {@code AbstractWhereClauseTerminal} instance.
     *
     * @param node                 the query node
     * @param selectEngineTerminal the terminal select engine
     * @param litebridgeContext    the Litebridge context
     */
    public AbstractWhereClauseTerminal(final QueryNode node, final SelectEngineTerminal selectEngineTerminal, final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
    }
}

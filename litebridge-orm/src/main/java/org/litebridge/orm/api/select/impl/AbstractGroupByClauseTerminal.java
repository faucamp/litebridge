package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.GroupByClauseTerminal;
import org.litebridge.orm.api.select.HavingConditionClause;
import org.litebridge.orm.api.select.HavingConditionClauseTerminal;
import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.QueryNode;

/**
 * Abstract base class for GROUP BY clause terminals.
 *
 * @param <DTO>  the mapped DTO/entity type or row type
 * @param <HCC>  the having condition clause type
 * @param <HCCT> the having condition clause terminal type
 * @param <OBC>  the order by clause type
 * @param <OBCC> the order by clause chain type
 */
public abstract class AbstractGroupByClauseTerminal<DTO,
        HCC extends HavingConditionClause<DTO, HCC, HCCT, QCB, OBC, OBCC>,
        HCCT extends HavingConditionClauseTerminal<DTO, HCC, HCCT, QCB, OBC, OBCC>,
        QCB extends QueryConditionBuilder<DTO, ?, ?, ?, QCB>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends OrderByClauseTerminalImpl<DTO>
        implements GroupByClauseTerminal<DTO, HCC, HCCT, QCB, OBC, OBCC> {

    /**
     * Creates a new {@code AbstractGroupByClauseTerminal} instance.
     *
     * @param node                 the query node
     * @param selectEngineTerminal the terminal select engine
     * @param litebridgeContext    the Litebridge context
     */
    protected AbstractGroupByClauseTerminal(final QueryNode node,
                                            final SelectEngineTerminal selectEngineTerminal,
                                            final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
    }
}

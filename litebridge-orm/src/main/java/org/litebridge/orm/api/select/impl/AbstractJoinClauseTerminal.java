package org.litebridge.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.GroupByClauseTerminal;
import org.litebridge.orm.api.select.HavingConditionClause;
import org.litebridge.orm.api.select.HavingConditionClauseTerminal;
import org.litebridge.orm.api.select.JoinClause;
import org.litebridge.orm.api.select.JoinClauseTerminal;
import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.JoinConditionClauseTerminal;
import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.api.select.WhereConditionClause;
import org.litebridge.orm.api.select.WhereConditionClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.QueryNode;

/**
 * Abstract base class for JOIN clause terminals.
 *
 * @param <DTO>  the mapped DTO/entity type or row type
 * @param <JC>   the join clause type
 * @param <JCC>  the join condition clause type
 * @param <SELF> the join condition clause terminal type
 * @param <WCC>  the where condition clause type
 * @param <WCCT> the where condition clause terminal type
 * @param <GBCT> the group by clause terminal type
 * @param <HCC>  the having condition clause type
 * @param <HCCT> the having condition clause terminal type
 * @param <OBC>  the order by clause type
 * @param <OBCC> the order by clause chain type
 */
public abstract class AbstractJoinClauseTerminal<DTO,
        JC extends JoinClause<DTO, JCC, SELF, QCB>,
        JCC extends JoinConditionClause<DTO, JCC, SELF, QCB>,
        SELF extends JoinConditionClauseTerminal<DTO, JCC, SELF, QCB>,
        WCC extends WhereConditionClause<DTO, WCC, WCCT, GBCT, HCC, HCCT, QCB, OBC, OBCC>,
        WCCT extends WhereConditionClauseTerminal<DTO, WCC, WCCT, GBCT, HCC, HCCT, QCB, OBC, OBCC>,
        GBCT extends GroupByClauseTerminal<DTO, HCC, HCCT, QCB, OBC, OBCC>,
        HCC extends HavingConditionClause<DTO, HCC, HCCT, QCB, OBC, OBCC>,
        HCCT extends HavingConditionClauseTerminal<DTO, HCC, HCCT, QCB, OBC, OBCC>,
        QCB extends QueryConditionBuilder<DTO, ?, ?, ?, QCB>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends AbstractWhereClauseTerminal<DTO, GBCT, HCC, HCCT, QCB, OBC, OBCC>
        implements JoinClauseTerminal<DTO, JC, JCC, SELF, WCC, WCCT, GBCT, HCC, HCCT, QCB, OBC, OBCC> {

    /**
     * Creates a new {@code AbstractJoinClauseTerminal} instance.
     *
     * @param node                 the query node
     * @param selectEngineTerminal the terminal select engine
     * @param litebridgeContext    the Litebridge context
     */
    public AbstractJoinClauseTerminal(final @Nullable QueryNode node, final SelectEngineTerminal selectEngineTerminal, final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
    }
}

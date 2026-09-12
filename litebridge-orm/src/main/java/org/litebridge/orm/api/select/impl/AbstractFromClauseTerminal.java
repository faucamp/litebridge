package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.FromClauseTerminal;
import org.litebridge.orm.api.select.GroupByClauseTerminal;
import org.litebridge.orm.api.select.HavingConditionClause;
import org.litebridge.orm.api.select.HavingConditionClauseTerminal;
import org.litebridge.orm.api.select.JoinClause;
import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.JoinConditionClauseTerminal;
import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.api.select.WhereConditionClause;
import org.litebridge.orm.api.select.WhereConditionClauseTerminal;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;

/**
 * Abstract base class for FROM clause terminals.
 *
 * @param <DTO>  the mapped DTO/entity type or row type
 * @param <JC>   the join clause type
 * @param <JCC>  the join condition clause type
 * @param <JCCT> the join condition clause terminal type
 * @param <WCC>  the where condition clause type
 * @param <WCCT> the where condition clause terminal type
 * @param <GBCT> the group by clause terminal type
 * @param <HCC>  the having condition clause type
 * @param <HCCT> the having condition clause terminal type
 * @param <OBC>  the order by clause type
 * @param <OBCC> the order by clause chain type
 */
public abstract class AbstractFromClauseTerminal<DTO,
        JC extends JoinClause<DTO, JCC, JCCT>,
        JCC extends JoinConditionClause<DTO, JCC, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, JCC, JCCT>,
        WCC extends WhereConditionClause<DTO, WCC, WCCT, GBCT, HCC, HCCT, OBC, OBCC>,
        WCCT extends WhereConditionClauseTerminal<DTO, WCC, WCCT, GBCT, HCC, HCCT, OBC, OBCC>,
        GBCT extends GroupByClauseTerminal<DTO, HCC, HCCT, OBC, OBCC>,
        HCC extends HavingConditionClause<DTO, HCC, HCCT, OBC, OBCC>,
        HCCT extends HavingConditionClauseTerminal<DTO, HCC, HCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends AbstractJoinClauseTerminal<DTO, JC, JCC, JCCT, WCC, WCCT, GBCT, HCC, HCCT, OBC, OBCC>
        implements FromClauseTerminal<DTO, JC, JCC, JCCT, WCC, WCCT, GBCT, HCC, HCCT, OBC, OBCC> {

    /**
     * Creates a new {@code AbstractFromClauseTerminal} instance.
     *
     * @param node                 the query node
     * @param selectEngineTerminal the terminal select engine
     * @param litebridgeContext    the Litebridge context
     */
    public AbstractFromClauseTerminal(final QueryNode node, final SelectEngineTerminal selectEngineTerminal, final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
    }
}

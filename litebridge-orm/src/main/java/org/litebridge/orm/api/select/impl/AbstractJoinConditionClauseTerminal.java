package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.GroupByClauseTerminal;
import org.litebridge.orm.api.select.HavingConditionClause;
import org.litebridge.orm.api.select.HavingConditionClauseTerminal;
import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.JoinConditionClauseTerminal;
import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;

/**
 * Abstract base class for JOIN condition clause terminals.
 *
 * @param <DTO>  the mapped DTO/entity type or row type
 * @param <JCC>  the join condition clause type
 * @param <SELF> the join condition clause terminal type
 * @param <GBCT> the group by clause terminal type
 * @param <HCC>  the having condition clause type
 * @param <HCCT> the having condition clause terminal type
 * @param <OBC>  the order by clause type
 * @param <OBCC> the order by clause chain type
 */
public abstract class AbstractJoinConditionClauseTerminal<DTO,
        JCC extends JoinConditionClause<DTO, JCC, SELF>,
        SELF extends JoinConditionClauseTerminal<DTO, JCC, SELF>,
        GBCT extends GroupByClauseTerminal<DTO, HCC, HCCT, OBC, OBCC>,
        HCC extends HavingConditionClause<DTO, HCC, HCCT, OBC, OBCC>,
        HCCT extends HavingConditionClauseTerminal<DTO, HCC, HCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends AbstractWhereClauseTerminal<DTO, GBCT, HCC, HCCT, OBC, OBCC>
        implements JoinConditionClauseTerminal<DTO, JCC, SELF> {

    /**
     * The join AST query node.
     */
    protected final JoinNode joinNode;

    /**
     * Creates a new {@code AbstractJoinConditionClauseTerminal} instance.
     *
     * @param joinNode             the join query node
     * @param selectEngineTerminal the terminal select engine
     * @param litebridgeContext    the Litebridge context
     */
    public AbstractJoinConditionClauseTerminal(final JoinNode joinNode, final SelectEngineTerminal selectEngineTerminal, final LitebridgeContext litebridgeContext) {
        super(joinNode, selectEngineTerminal, litebridgeContext);
        this.joinNode = joinNode;
    }
}

package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.JoinClause;
import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.JoinConditionClauseTerminal;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;

/**
 * Abstract base class for JOIN clauses.
 *
 * @param <DTO>  the mapped DTO/entity type or row type
 * @param <JCC>  the join condition clause type
 * @param <JCCT> the join condition clause terminal type
 */
public abstract class AbstractJoinClause<DTO,
        JCC extends JoinConditionClause<DTO, JCC, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, JCC, JCCT>>

        implements JoinClause<DTO, JCC, JCCT> {

    /**
     * The current query node.
     */
    protected final QueryNode node;

    /**
     * The Litebridge context.
     */
    protected final LitebridgeContext litebridgeContext;

    /**
     * Creates a new {@code AbstractJoinClause} instance.
     *
     * @param node              the current query node
     * @param litebridgeContext the Litebridge context
     */
    public AbstractJoinClause(final QueryNode node, final LitebridgeContext litebridgeContext) {
        this.node = node;
        this.litebridgeContext = litebridgeContext;
    }
}

package org.litebridge.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.select.GroupByClauseTerminal;
import org.litebridge.orm.api.select.HavingConditionClause;
import org.litebridge.orm.api.select.HavingConditionClauseTerminal;
import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;

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
        HCC extends HavingConditionClause<DTO, HCC, HCCT, OBC, OBCC>,
        HCCT extends HavingConditionClauseTerminal<DTO, HCC, HCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends OrderByClauseTerminalImpl<DTO>
        implements GroupByClauseTerminal<DTO, HCC, HCCT, OBC, OBCC> {

    /**
     * Group-by column names, or {@code null} if expressions are used.
     */
    protected final String @Nullable [] columns;

    /**
     * Group-by expressions, or {@code null} if column names are used.
     */
    protected final ExpressionSpec @Nullable [] expressions;

    /**
     * Creates a new instance using column names.
     *
     * @param columns              the column names
     * @param node                 the query node
     * @param selectEngineTerminal the terminal select engine
     * @param litebridgeContext    the Litebridge context
     */
    protected AbstractGroupByClauseTerminal(final String @Nullable [] columns,
                                            final QueryNode node,
                                            final SelectEngineTerminal selectEngineTerminal,
                                            final LitebridgeContext litebridgeContext) {
        this(columns, null, node, selectEngineTerminal, litebridgeContext);
    }

    /**
     * Creates a new instance using expressions.
     *
     * @param expressions          the expressions
     * @param node                 the query node
     * @param selectEngineTerminal the terminal select engine
     * @param litebridgeContext    the Litebridge context
     */
    protected AbstractGroupByClauseTerminal(final ExpressionSpec @Nullable [] expressions,
                                            final QueryNode node,
                                            final SelectEngineTerminal selectEngineTerminal,
                                            final LitebridgeContext litebridgeContext) {
        this(null, expressions, node, selectEngineTerminal, litebridgeContext);
    }

    private AbstractGroupByClauseTerminal(final String @Nullable [] columns,
                                          final ExpressionSpec @Nullable [] expressions,
                                          final QueryNode node,
                                          final SelectEngineTerminal selectEngineTerminal,
                                          final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
        this.columns = columns;
        this.expressions = expressions;
    }
}

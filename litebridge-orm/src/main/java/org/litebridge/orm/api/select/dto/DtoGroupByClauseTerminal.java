package org.litebridge.orm.api.select.dto;

import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.impl.AbstractGroupByClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.GroupByNode;
import org.litebridge.orm.engine.ast.HavingNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Terminal clause for DTO GROUP BY clauses.
 *
 * @param <DTO> the type of the DTO
 */
public final class DtoGroupByClauseTerminal<DTO> extends AbstractGroupByClauseTerminal<DTO,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>> {

    /**
     * Creates a new {@code DtoGroupByClauseTerminal} instance using expressions.
     *
     * @param expressions          the group-by expressions
     * @param node                 the current query node
     * @param selectEngineTerminal the terminal select engine
     * @param litebridgeContext    the Litebridge context
     */
    public DtoGroupByClauseTerminal(final ExpressionSpec[] expressions,
                                    final QueryNode node,
                                    final SelectEngineTerminal selectEngineTerminal,
                                    final LitebridgeContext litebridgeContext) {
        super(expressions, new GroupByNode(node, null, expressions), selectEngineTerminal, litebridgeContext);
    }

    /**
     * Creates a new {@code DtoGroupByClauseTerminal} instance using field names.
     *
     * @param fields               the group-by field names
     * @param node                 the current query node
     * @param selectEngineTerminal the terminal select engine
     * @param litebridgeContext    the Litebridge context
     */
    public DtoGroupByClauseTerminal(final String[] fields,
                                    final QueryNode node,
                                    final SelectEngineTerminal selectEngineTerminal,
                                    final LitebridgeContext litebridgeContext) {
        super(fields, new GroupByNode(node, fields, null), selectEngineTerminal, litebridgeContext);
    }

    @Override
    public DtoHavingConditionClause<DTO> having(final ExpressionSpec expression) {
        return new DtoHavingConditionClause<>(litebridgeContext,
                LogicOperator.NOOP,
                null,
                expression,
                null,
                conditionNode -> new DtoHavingConditionClauseTerminal<>(new HavingNode(this.node, conditionNode), selectEngineTerminal, litebridgeContext));
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        return new DtoOrderByClause<>(fields, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final ExpressionSpec... expressions) {
        return new DtoOrderByClause<>(expressions, node, selectEngineTerminal, litebridgeContext);
    }
}

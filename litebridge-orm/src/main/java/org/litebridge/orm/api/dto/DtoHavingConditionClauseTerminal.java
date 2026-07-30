package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.dto.condition.DtoConditionClauseStart;
import org.litebridge.orm.api.select.HavingConditionClauseTerminal;
import org.litebridge.orm.api.select.ast.ConditionGroupNode;
import org.litebridge.orm.api.select.ast.HavingNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.impl.AbstractHavingClauseTerminal;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.OrmTable;

/**
 * Terminal clause for DTO HAVING conditions.
 *
 * @param <DTO> the type of the DTO
 */
public final class DtoHavingConditionClauseTerminal<DTO>
        extends AbstractHavingClauseTerminal<DTO,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>,
        DtoSelectSpec>

        implements HavingConditionClauseTerminal<DTO,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>> {

    private final OrmTable ormTable;

    /**
     * Creates a new DtoHavingConditionClauseTerminal.
     *
     * @param delegate the DTO selector delegate
     */
    public DtoHavingConditionClauseTerminal(final DtoSelector<DTO> delegate) {
        super(delegate);
        ormTable = delegate.ormTable();
    }

    /**
     * Adds an "AND" condition to the current condition clause using the specified column.
     * This method is used to chain additional conditions in a SQL query in a type-safe and fluent manner.
     *
     * @param field the name of the DTO field to be used in the "AND" condition
     * @return the parent condition clause interface, allowing further chaining of conditions
     */
    @Override
    public DtoHavingConditionClause<DTO> and(final String field) {
        final Column column = ormTable.getColumnForFieldName(field).toColumn();
        return and(new SelectColumnSpec(column));
    }

    @Override
    public DtoHavingConditionClause<DTO> and(final ExpressionSpec expression) {
        return havingImpl(LogicOperator.AND, expression, (DtoSelector<DTO>) delegate);
    }

    @Override
    public DtoHavingConditionClauseTerminal<DTO> and(final QueryConditionBuilder<DTO> query) {
        return havingImpl(LogicOperator.AND, query);
    }

    @Override
    public DtoHavingConditionClause<DTO> or(final String field) {
        final Column column = ormTable.getColumnForFieldName(field).toColumn();
        return or(new SelectColumnSpec(column));
    }

    @Override
    public DtoHavingConditionClause<DTO> or(final ExpressionSpec expression) {
        return havingImpl(LogicOperator.OR, expression, (DtoSelector<DTO>) delegate);
    }

    @Override
    public DtoHavingConditionClauseTerminal<DTO> or(final QueryConditionBuilder<DTO> query) {
        return havingImpl(LogicOperator.OR, query);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        return orderBy(((DtoSelector<DTO>) delegate).createSelectFieldSpecs(fields).toArray(ExpressionSpec[]::new));
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final ExpressionSpec... fields) {
        return new DtoOrderByClause<>(fields, (DtoSelector<DTO>) delegate);
    }

    private DtoHavingConditionClause<DTO> havingImpl(final LogicOperator logicOperator, final ExpressionSpec expression, final DtoSelector<DTO> newDelegate) {
        if (delegate.node() instanceof HavingNode havingNode) {
            return new DtoHavingConditionClause<>(delegate.litebridgeContext(),
                    logicOperator,
                    expression,
                    havingNode.condition(),
                    node -> new DtoHavingConditionClauseTerminal<>((DtoSelector<DTO>) delegate.withNode(havingNode.withCondition(node))));
        }

        return new DtoHavingConditionClause<>(delegate.litebridgeContext(),
                logicOperator,
                expression,
                null,
                node -> new DtoHavingConditionClauseTerminal<>((DtoSelector<DTO>) delegate.withNode(new HavingNode(delegate.node(), node))));
    }

    private DtoHavingConditionClauseTerminal<DTO> havingImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(ormTable, delegate.litebridgeContext().fromClauseEngine(), null);
        final org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal<DTO> terminal = query.apply(conditionClauseStart);
        final QueryNode conditionNode = terminal.node();

        if (delegate.node() instanceof HavingNode havingNode) {
            final ConditionGroupNode groupNode = new ConditionGroupNode(havingNode.condition(), logicOperator, conditionNode);
            havingNode.withCondition(groupNode);
            return this;
        }

        final ConditionGroupNode groupNode = new ConditionGroupNode(null, logicOperator, conditionNode);
        delegate.withNode(new HavingNode(delegate.node(), groupNode));

        return this;
    }
}

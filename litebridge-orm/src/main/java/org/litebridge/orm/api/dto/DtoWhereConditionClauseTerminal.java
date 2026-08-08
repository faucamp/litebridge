package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.dto.condition.DtoConditionClauseStart;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.api.select.WhereConditionClauseTerminal;
import org.litebridge.orm.api.select.ast.ConditionGroupNode;
import org.litebridge.orm.api.select.ast.GroupByNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.api.select.impl.AbstractWhereClauseTerminal;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.OrmTable;

/**
 * DTO where condition clause terminal.
 *
 * @param <DTO> the DTO type.
 */
public final class DtoWhereConditionClauseTerminal<DTO>
        extends AbstractWhereClauseTerminal<DTO,
        DtoGroupByClauseTerminal<DTO>,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>,
        DtoSelectSpec>

        implements WhereConditionClauseTerminal<DTO,
        DtoWhereConditionClause<DTO>,
        DtoWhereConditionClauseTerminal<DTO>,
        DtoGroupByClauseTerminal<DTO>,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>> {

    private final OrmTable ormTable;

    /**
     * Constructs a new {@code DtoWhereConditionClauseTerminal}.
     *
     * @param delegate the selector delegate.
     */
    public DtoWhereConditionClauseTerminal(final DtoSelector<DTO> delegate) {
        super(delegate);
        ormTable = delegate.ormTable();
    }

    @Override
    public DtoWhereConditionClause<DTO> and(final String field) {
        final Column column = ormTable.getColumnForFieldName(field).toColumn();
        return and(new SelectColumnSpec(column));
    }

    @Override
    public DtoWhereConditionClause<DTO> and(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.AND, expression);
    }

    @Override
    public DtoWhereConditionClauseTerminal<DTO> and(final QueryConditionBuilder<DTO> query) {
        return whereImpl(LogicOperator.AND, query);
    }

    @Override
    public DtoWhereConditionClause<DTO> or(final String field) {
        final Column spiColumn = ormTable.getColumnForFieldName(field).toColumn();
        return or(new SelectColumnSpec(spiColumn));
    }

    @Override
    public DtoWhereConditionClause<DTO> or(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.OR, expression);
    }

    @Override
    public DtoWhereConditionClauseTerminal<DTO> or(final QueryConditionBuilder<DTO> query) {
        return whereImpl(LogicOperator.OR, query);
    }

    @Override
    public DtoGroupByClauseTerminal<DTO> groupBy(final String... fields) {
        return groupBy(((DtoSelector<DTO>) delegate).createSelectFieldSpecs(fields).toArray(ExpressionSpec[]::new));
    }

    @Override
    public DtoGroupByClauseTerminal<DTO> groupBy(final ExpressionSpec... fields) {
        final QueryNode groupByNode = new GroupByNode(delegate.node(), fields);
        return new DtoGroupByClauseTerminal<>((DtoSelector<DTO>) delegate.withNode(groupByNode));
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        return orderBy(((DtoSelector<DTO>) delegate).createSelectFieldSpecs(fields).toArray(ExpressionSpec[]::new));
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final ExpressionSpec... fields) {
        return new DtoOrderByClause<>(fields, (DtoSelector<DTO>) delegate);
    }

    private DtoWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        if (delegate.node() instanceof WhereNode whereNode) {
            return new DtoWhereConditionClause<>(delegate.litebridgeContext(),
                    logicOperator,
                    expression,
                    whereNode.condition(),
                    node -> new DtoWhereConditionClauseTerminal<>((DtoSelector<DTO>) delegate.withNode(whereNode.withCondition(node))));
        }

        return new DtoWhereConditionClause<>(delegate.litebridgeContext(),
                logicOperator,
                expression,
                null,
                node -> new DtoWhereConditionClauseTerminal<>((DtoSelector<DTO>) delegate.withNode(new WhereNode(delegate.node(), node))));
    }

    private DtoWhereConditionClauseTerminal<DTO> whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
        if (!(delegate.node() instanceof WhereNode whereNode)) {
            throw new IllegalArgumentException("AST error: Expected a WhereNode but got " + delegate.node());
        }

        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(ormTable, delegate.litebridgeContext().fromClauseEngine(), null);
        final AbstractCbConditionClauseTerminal<DTO> terminal = query.apply(conditionClauseStart);
        whereNode.withCondition(new ConditionGroupNode(whereNode.condition(), logicOperator, terminal.node()));
        return this;
    }
}

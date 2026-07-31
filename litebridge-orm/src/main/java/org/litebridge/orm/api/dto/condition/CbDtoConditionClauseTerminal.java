package org.litebridge.orm.api.dto.condition;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.api.select.ast.ConditionContext;
import org.litebridge.orm.api.select.ast.ConditionGroupNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.OrmTable;

/**
 * Implementation of a terminal condition clause for DTO-based queries.
 *
 * @param <DTO> The type of the DTO being queried.
 */
public final class CbDtoConditionClauseTerminal<DTO> extends AbstractCbConditionClauseTerminal<DTO> {

    private final OrmTable ormTable;

    /**
     * Constructs a new {@code CbDtoConditionClauseTerminal}.
     *
     * @param ormTable         The ORM table metadata.
     * @param fromClauseEngine The FROM clause engine.
     */
    public CbDtoConditionClauseTerminal(final OrmTable ormTable, final FromClauseEngine fromClauseEngine, final QueryNode node) {
        super(fromClauseEngine, node);
        this.ormTable = ormTable;
    }


    @Override
    protected CbDtoConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final String field) {
        final Column column = ormTable.getColumnForFieldName(field).toColumn();
        return whereImpl(logicOperator, new SelectColumnSpec(column));
    }

    @Override
    protected CbDtoConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        return new CbDtoConditionClause<>(ormTable,
                fromClauseEngine,
                logicOperator,
                expression,
                ConditionContext.WHERE,
                node,
                conditionNode -> new CbDtoConditionClauseTerminal<>(ormTable, fromClauseEngine, conditionNode));
    }

    @Override
    protected AbstractCbConditionClauseTerminal<DTO> whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(ormTable, fromClauseEngine, null);
        final ConditionClauseTerminal<DTO, ?, ?> terminal = query.apply(conditionClauseStart);

        if (terminal instanceof AbstractCbConditionClauseTerminal<?> act) {
            return new CbDtoConditionClauseTerminal<>(ormTable, fromClauseEngine, new ConditionGroupNode(node, logicOperator, act.node()));
        }

        return this;
    }
}

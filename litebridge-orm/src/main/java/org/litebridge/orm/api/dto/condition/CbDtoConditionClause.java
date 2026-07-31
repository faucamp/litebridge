package org.litebridge.orm.api.dto.condition;

import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClause;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.select.ast.ConditionContext;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.persistence.OrmTable;

import java.util.function.Function;

/**
 * Implementation of a condition clause for DTO-based queries.
 *
 * @param <DTO> The type of the DTO being queried.
 */
public class CbDtoConditionClause<DTO> extends AbstractCbConditionClause<DTO> {

    private final OrmTable ormTable;
    private final Function<QueryNode, AbstractCbConditionClauseTerminal<DTO>> terminalCreator;

    /**
     * Constructs a new {@code CbDtoConditionClause}.
     *
     * @param ormTable           The ORM table metadata.
     * @param fromClauseEngine   The FROM clause engine.
     */
    public CbDtoConditionClause(final OrmTable ormTable,
                                final FromClauseEngine fromClauseEngine,
                                final LogicOperator logicOperator,
                                final ExpressionSpec lhs,
                                final ConditionContext conditionContext,
                                final QueryNode node,
                                final Function<QueryNode, AbstractCbConditionClauseTerminal<DTO>> terminalCreator) {
        super(fromClauseEngine, logicOperator, lhs, conditionContext, node, terminalCreator);
        this.ormTable = ormTable;
        this.terminalCreator = terminalCreator;
    }

    @Override
    protected AbstractCbConditionClauseTerminal<DTO> createCbConditionClauseTerminal(final QueryNode conditionNode) {
        return new CbDtoConditionClauseTerminal<>(ormTable, fromClauseEngine, conditionNode);
    }
}

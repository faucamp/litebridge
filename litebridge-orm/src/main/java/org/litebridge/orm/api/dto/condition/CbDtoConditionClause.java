package org.litebridge.orm.api.dto.condition;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClause;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
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
     * @param ormTable         The ORM table metadata.
     * @param fromClauseEngine The FROM clause engine.
     * @param logicOperator    The logical operator (AND/OR).
     * @param lhs              The left-hand side expression.
     * @param node             The previous node in the chain.
     * @param terminalCreator  The function to create the terminal clause.
     */
    public CbDtoConditionClause(final OrmTable ormTable,
                                final FromClauseEngine fromClauseEngine,
                                final LogicOperator logicOperator,
                                final @Nullable String lhsColumn,
                                final @Nullable ExpressionSpec lhsExpression,
                                final @Nullable QueryNode node,
                                final Function<QueryNode, AbstractCbConditionClauseTerminal<DTO>> terminalCreator) {
        super(fromClauseEngine, logicOperator, lhsColumn, lhsExpression, node, terminalCreator);
        this.ormTable = ormTable;
        this.terminalCreator = terminalCreator;
    }

    @Override
    protected AbstractCbConditionClauseTerminal<DTO> createCbConditionClauseTerminal(final QueryNode conditionNode) {
        return new CbDtoConditionClauseTerminal<>(ormTable, fromClauseEngine, conditionNode);
    }
}

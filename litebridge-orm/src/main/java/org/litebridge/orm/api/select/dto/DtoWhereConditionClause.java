package org.litebridge.orm.api.select.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.WhereConditionClause;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

/**
 * DTO where condition clause.
 *
 * @param <DTO> the DTO type.
 */
public final class DtoWhereConditionClause<DTO>
        extends ConditionClauseImpl<DTO,
        DtoWhereConditionClause<DTO>,
        DtoWhereConditionClauseTerminal<DTO>>

        implements WhereConditionClause<DTO,
        DtoWhereConditionClause<DTO>,
        DtoWhereConditionClauseTerminal<DTO>,
        DtoGroupByClauseTerminal<DTO>,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>> {

    /**
     * Creates a new {@code DtoWhereConditionClause} instance.
     *
     * @param litebridgeContext the Litebridge context
     * @param logicOperator     the logical operator (AND/OR)
     * @param lhsField          the left-hand side field name
     * @param lhsExpression     the left-hand side expression
     * @param node              the previous query node in the chain
     * @param terminalRecreator the function to create the terminal clause
     */
    public DtoWhereConditionClause(final LitebridgeContext litebridgeContext,
                                   final LogicOperator logicOperator,
                                   final @Nullable String lhsField,
                                   final @Nullable ExpressionSpec lhsExpression,
                                   final @Nullable QueryNode node,
                                   final Function<QueryNode, DtoWhereConditionClauseTerminal<DTO>> terminalRecreator) {
        super(litebridgeContext, logicOperator, lhsField, lhsExpression, node, terminalRecreator);
    }
}

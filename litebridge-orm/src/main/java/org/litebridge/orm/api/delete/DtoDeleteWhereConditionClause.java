package org.litebridge.orm.api.delete;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

/**
 * Represents a WHERE condition clause for DTO delete operations.
 *
 * @param <DTO> the type of the DTO
 */
public final class DtoDeleteWhereConditionClause<DTO>

        extends ConditionClauseImpl<DTO,
        DtoDeleteWhereConditionClause<DTO>,
        DtoDeleteWhereConditionClauseTerminal<DTO>>

        implements DeleteWhereConditionClause<DTO,
        DtoDeleteWhereConditionClause<DTO>,
        DtoDeleteWhereConditionClauseTerminal<DTO>> {

    /**
     * Creates a new {@code DtoDeleteWhereConditionClause} instance.
     *
     * @param litebridgeContext the Litebridge context
     * @param logicOperator     the logical operator (AND/OR)
     * @param lhsColumn         the left-hand side column name
     * @param lhsExpression     the left-hand side expression
     * @param terminalRecreator the function to create the terminal clause
     */
    public DtoDeleteWhereConditionClause(final LitebridgeContext litebridgeContext,
                                         final LogicOperator logicOperator,
                                         final @Nullable String lhsColumn,
                                         final @Nullable ExpressionSpec lhsExpression,
                                         final Function<QueryNode, DtoDeleteWhereConditionClauseTerminal<DTO>> terminalRecreator) {
        super(litebridgeContext, logicOperator, lhsColumn, lhsExpression, null, terminalRecreator);
    }
}

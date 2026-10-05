package org.litebridge.orm.api.update;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

/**
 * Represents a where condition clause for a DTO update.
 *
 * @param <DTO> the DTO type
 */
public final class DtoUpdateWhereConditionClause<DTO>

        extends ConditionClauseImpl<DTO,
        DtoUpdateWhereConditionClause<DTO>,
        DtoUpdateWhereConditionClauseTerminal<DTO>>

        implements UpdateWhereConditionClause<DTO,
        DtoUpdateWhereConditionClause<DTO>,
        DtoUpdateWhereConditionClauseTerminal<DTO>> {

    /**
     * Creates a new {@code DtoUpdateWhereConditionClause} instance.
     *
     * @param litebridgeContext the Litebridge context
     * @param logicOperator     the logical operator (AND/OR)
     * @param lhsField          the left-hand side field name
     * @param lhsExpression     the left-hand side expression
     * @param terminalRecreator the function to create the terminal clause
     */
    public DtoUpdateWhereConditionClause(final LitebridgeContext litebridgeContext,
                                         final LogicOperator logicOperator,
                                         final @Nullable String lhsField,
                                         final @Nullable ExpressionSpec lhsExpression,
                                         final Function<QueryNode, DtoUpdateWhereConditionClauseTerminal<DTO>> terminalRecreator) {
        super(litebridgeContext, logicOperator, lhsField, lhsExpression, null, terminalRecreator);
    }
}

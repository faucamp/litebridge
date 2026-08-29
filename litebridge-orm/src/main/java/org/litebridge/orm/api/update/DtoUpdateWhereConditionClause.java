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

    public DtoUpdateWhereConditionClause(final LitebridgeContext litebridgeContext,
                                         final LogicOperator logicOperator,
                                         final @Nullable String lhsField,
                                         final @Nullable ExpressionSpec lhsExpression,
                                         final Function<QueryNode, DtoUpdateWhereConditionClauseTerminal<DTO>> terminalRecreator) {
        super(litebridgeContext, logicOperator, lhsField, lhsExpression, null, terminalRecreator);
    }
}

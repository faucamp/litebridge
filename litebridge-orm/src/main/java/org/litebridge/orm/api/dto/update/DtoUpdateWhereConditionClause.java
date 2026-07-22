package org.litebridge.orm.api.dto.update;

import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.update.UpdateWhereConditionClause;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Represents a where condition clause for a DTO update.
 *
 * @param <DTO> the DTO type
 */
public class DtoUpdateWhereConditionClause<DTO>

        extends ConditionClauseImpl<DTO,
        DtoUpdateWhereConditionClause<DTO>,
        DtoUpdateWhereConditionClauseTerminal<DTO>>

        implements UpdateWhereConditionClause<DTO,
        DtoUpdateWhereConditionClause<DTO>,
        DtoUpdateWhereConditionClauseTerminal<DTO>> {

    public DtoUpdateWhereConditionClause(final ConditionSpec conditionSpec,
                                         final LitebridgeContext litebridgeContext,
                                         final LogicOperator logicOperator,
                                         final ExpressionSpec lhs,
                                         final java.util.function.Function<org.litebridge.orm.api.select.ast.QueryNode, DtoUpdateWhereConditionClauseTerminal<DTO>> terminalRecreator) {
        super(conditionSpec, litebridgeContext, logicOperator, lhs, null, terminalRecreator);
    }
}

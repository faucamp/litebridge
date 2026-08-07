package org.litebridge.orm.api.dto.delete;

import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.delete.DeleteWhereConditionClause;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

/**
 * Represents a WHERE condition clause for DTO delete operations.
 *
 * @param <DTO> the type of the DTO
 */
public class DtoDeleteWhereConditionClause<DTO>

        extends ConditionClauseImpl<DTO,
        DtoDeleteWhereConditionClause<DTO>,
        DtoDeleteWhereConditionClauseTerminal<DTO>>

        implements DeleteWhereConditionClause<DTO,
        DtoDeleteWhereConditionClause<DTO>,
        DtoDeleteWhereConditionClauseTerminal<DTO>> {

    public DtoDeleteWhereConditionClause(final LitebridgeContext litebridgeContext,
                                         final LogicOperator logicOperator,
                                         final ExpressionSpec lhs,
                                         final Function<QueryNode, DtoDeleteWhereConditionClauseTerminal<DTO>> terminalRecreator) {
        super(litebridgeContext, logicOperator, lhs, null, terminalRecreator);
    }
}

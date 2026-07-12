package org.litebridge.orm.api.dto.delete;

import org.litebridge.orm.api.delete.DeleteWhereConditionClause;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.api.select.model.ConditionSpec;

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

    /**
     * Creates a new DtoDeleteWhereConditionClause.
     *
     * @param conditionSpec     the condition specification
     * @param conditionTerminal the terminal clause
     * @param litebridgeContext the Litebridge context
     */
    public DtoDeleteWhereConditionClause(final ConditionSpec conditionSpec, final DtoDeleteWhereConditionClauseTerminal<DTO> conditionTerminal, final LitebridgeContext litebridgeContext) {
        super(conditionSpec, conditionTerminal, litebridgeContext);
    }
}

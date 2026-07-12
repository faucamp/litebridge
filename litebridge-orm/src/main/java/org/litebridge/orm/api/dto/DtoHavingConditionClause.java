package org.litebridge.orm.api.dto;

import org.litebridge.orm.api.select.HavingConditionClause;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.api.select.model.ConditionSpec;

/**
 * Represents a HAVING condition clause for DTO queries.
 *
 * @param <DTO> the type of the DTO
 */
public final class DtoHavingConditionClause<DTO>
        extends ConditionClauseImpl<DTO,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>>

        implements HavingConditionClause<DTO,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>> {

    /**
     * Creates a new DtoHavingConditionClause.
     *
     * @param conditionSpec     the condition specification
     * @param conditionTerminal the terminal clause
     * @param litebridgeContext the Litebridge context
     */
    public DtoHavingConditionClause(final ConditionSpec conditionSpec, final DtoHavingConditionClauseTerminal<DTO> conditionTerminal, final LitebridgeContext litebridgeContext) {
        super(conditionSpec, conditionTerminal, litebridgeContext);
    }
}

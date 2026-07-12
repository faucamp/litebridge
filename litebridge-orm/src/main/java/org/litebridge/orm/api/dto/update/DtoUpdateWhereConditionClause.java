package org.litebridge.orm.api.dto.update;

import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.update.UpdateWhereConditionClause;

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

    /**
     * Creates a new DtoUpdateWhereConditionClause.
     *
     * @param conditionSpec     the condition specification
     * @param conditionTerminal the condition terminal
     * @param litebridgeContext the litebridge context
     */
    public DtoUpdateWhereConditionClause(final ConditionSpec conditionSpec, final DtoUpdateWhereConditionClauseTerminal<DTO> conditionTerminal, final LitebridgeContext litebridgeContext) {
        super(conditionSpec, conditionTerminal, litebridgeContext);
    }
}

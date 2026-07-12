package org.litebridge.orm.api.dto;

import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.api.select.model.ConditionSpec;

/**
 * Represents a condition within a JOIN clause in a DTO-based query.
 *
 * @param <DTO> the type of the DTO being queried
 */
public final class DtoJoinConditionClause<DTO> extends ConditionClauseImpl<DTO,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>>

        implements JoinConditionClause<DTO, DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>> {

    /**
     * Creates a new instance of {@code DtoJoinConditionClause}.
     *
     * @param condition the condition specification
     * @param conditionTerminal the terminal clause for this condition
     * @param litebridgeContext the ORM context
     */
    public DtoJoinConditionClause(final ConditionSpec condition, final DtoJoinConditionClauseTerminal<DTO> conditionTerminal, final LitebridgeContext litebridgeContext) {
        super(condition, conditionTerminal, litebridgeContext);
    }
}

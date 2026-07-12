package org.litebridge.orm.api.dto;

import org.litebridge.orm.api.select.WhereConditionClause;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.api.select.model.ConditionSpec;

/**
 * DTO where condition clause.
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
     * Constructs a new {@code DtoWhereConditionClause}.
     *
     * @param conditionSpec     the condition specification.
     * @param conditionTerminal the condition terminal.
     * @param litebridgeContext the context.
     */
    public DtoWhereConditionClause(final ConditionSpec conditionSpec, final DtoWhereConditionClauseTerminal<DTO> conditionTerminal, final LitebridgeContext litebridgeContext) {
        super(conditionSpec, conditionTerminal, litebridgeContext);
    }
}

package org.litebridgedb.orm.api.dto;

import org.litebridgedb.orm.api.select.HavingConditionClause;
import org.litebridgedb.orm.api.select.impl.ConditionClauseImpl;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.api.select.model.ConditionSpec;

public final class DtoHavingConditionClause<DTO>
        extends ConditionClauseImpl<DTO,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>>

        implements HavingConditionClause<DTO,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>> {

    public DtoHavingConditionClause(final ConditionSpec conditionSpec, final DtoHavingConditionClauseTerminal<DTO> conditionTerminal, final LitebridgeContext litebridgeContext) {
        super(conditionSpec, conditionTerminal, litebridgeContext);
    }
}

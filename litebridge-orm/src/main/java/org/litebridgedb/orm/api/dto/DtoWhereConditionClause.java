package org.litebridgedb.orm.api.dto;

import org.litebridgedb.orm.api.select.WhereConditionClause;
import org.litebridgedb.orm.api.select.impl.ConditionClauseImpl;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.api.select.model.ConditionSpec;

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

    public DtoWhereConditionClause(final ConditionSpec conditionSpec, final DtoWhereConditionClauseTerminal<DTO> conditionTerminal, final LitebridgeContext litebridgeContext) {
        super(conditionSpec, conditionTerminal, litebridgeContext);
    }
}

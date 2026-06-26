package org.litebridgedb.orm.api.dto.update;

import org.litebridgedb.orm.api.select.impl.ConditionClauseImpl;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.api.update.UpdateWhereConditionClause;

public class DtoUpdateWhereConditionClause<DTO>

        extends ConditionClauseImpl<DTO,
        DtoUpdateWhereConditionClause<DTO>,
        DtoUpdateWhereConditionClauseTerminal<DTO>>

        implements UpdateWhereConditionClause<DTO,
        DtoUpdateWhereConditionClause<DTO>,
        DtoUpdateWhereConditionClauseTerminal<DTO>> {

    public DtoUpdateWhereConditionClause(final ConditionSpec conditionSpec, final DtoUpdateWhereConditionClauseTerminal<DTO> conditionTerminal, final LitebridgeContext litebridgeContext) {
        super(conditionSpec, conditionTerminal, litebridgeContext);
    }
}

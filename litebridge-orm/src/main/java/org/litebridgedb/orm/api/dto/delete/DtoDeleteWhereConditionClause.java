package org.litebridgedb.orm.api.dto.delete;

import org.litebridgedb.orm.api.delete.DeleteWhereConditionClause;
import org.litebridgedb.orm.api.select.impl.ConditionClauseImpl;
import org.litebridgedb.orm.api.select.impl.LitebridgeContext;
import org.litebridgedb.orm.api.select.model.ConditionSpec;

public class DtoDeleteWhereConditionClause<DTO>

        extends ConditionClauseImpl<DTO,
        DtoDeleteWhereConditionClause<DTO>,
        DtoDeleteWhereConditionClauseTerminal<DTO>>

        implements DeleteWhereConditionClause<DTO,
        DtoDeleteWhereConditionClause<DTO>,
        DtoDeleteWhereConditionClauseTerminal<DTO>> {

    public DtoDeleteWhereConditionClause(final ConditionSpec conditionSpec, final DtoDeleteWhereConditionClauseTerminal<DTO> conditionTerminal, final LitebridgeContext litebridgeContext) {
        super(conditionSpec, conditionTerminal, litebridgeContext);
    }
}

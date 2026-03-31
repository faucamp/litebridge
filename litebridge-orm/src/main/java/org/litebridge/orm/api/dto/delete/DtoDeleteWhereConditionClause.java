package org.litebridge.orm.api.dto.delete;

import org.litebridge.orm.api.delete.DeleteWhereConditionClause;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.api.select.model.ConditionSpec;

public class DtoDeleteWhereConditionClause<DTO>

        extends ConditionClauseImpl<DTO,
        DtoDeleteWhereConditionClause<DTO>,
        DtoDeleteWhereConditionClauseTerminal<DTO>>

        implements DeleteWhereConditionClause<DTO,
        DtoDeleteWhereConditionClause<DTO>,
        DtoDeleteWhereConditionClauseTerminal<DTO>> {

    public DtoDeleteWhereConditionClause(final ConditionSpec conditionSpec, final DtoDeleteWhereConditionClauseTerminal<DTO> conditionTerminal) {
        super(conditionSpec, conditionTerminal);
    }
}

package org.litebridge.orm.api.dto.update;

import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.update.UpdateWhereConditionClause;

public class DtoUpdateWhereConditionClause<DTO>

        extends ConditionClauseImpl<DTO,
        DtoUpdateWhereConditionClause<DTO>,
        DtoUpdateWhereConditionClauseTerminal<DTO>>

        implements UpdateWhereConditionClause<DTO,
        DtoUpdateWhereConditionClause<DTO>,
        DtoUpdateWhereConditionClauseTerminal<DTO>> {

    public DtoUpdateWhereConditionClause(final ConditionSpec conditionSpec, final DtoUpdateWhereConditionClauseTerminal<DTO> conditionTerminal) {
        super(conditionSpec, conditionTerminal);
    }
}

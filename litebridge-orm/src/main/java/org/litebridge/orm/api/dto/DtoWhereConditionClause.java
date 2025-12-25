package org.litebridge.orm.api.dto;

import org.litebridge.orm.api.select.WhereConditionClause;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.api.select.model.ConditionSpec;

public class DtoWhereConditionClause<DTO>
        extends ConditionClauseImpl<DTO, DtoWhereConditionClause<DTO>, DtoWhereConditionClauseTerminal<DTO>>
        implements WhereConditionClause<DTO, DtoWhereConditionClause<DTO>, DtoWhereConditionClauseTerminal<DTO>> {

    public DtoWhereConditionClause(final ConditionSpec conditionSpec, final DtoWhereConditionClauseTerminal<DTO> conditionTerminal) {
        super(conditionSpec, conditionTerminal);
    }
}

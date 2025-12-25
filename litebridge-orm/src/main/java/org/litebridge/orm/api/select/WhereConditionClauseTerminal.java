package org.litebridge.orm.api.select;

public interface WhereConditionClauseTerminal<DTO>
        extends ConditionClauseTerminal<DTO, WhereConditionClause<DTO>, WhereConditionClauseTerminal<DTO>>, WhereClauseTerminal<DTO> {

}

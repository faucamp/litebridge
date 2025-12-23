package org.litebridge.orm.api.select;

public interface WhereConditionClauseTerminal<DTO> extends
        ConditionClauseTerminal<DTO, WhereConditionClauseTerminal<DTO>>,
        WhereClauseTerminal<DTO> {

}

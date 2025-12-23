package org.litebridge.orm.api.select;

public interface JoinConditionClauseTerminal<DTO> extends
        ConditionClauseTerminal<DTO, JoinConditionClauseTerminal<DTO>>,
        JoinClauseTerminal<DTO> {

}

package org.litebridge.orm.api.select;

public interface ConditionClauseTerminal<DTO, SELF extends ConditionClauseTerminal<DTO, SELF>> {

    ConditionClause<DTO, SELF> and(String column);

}

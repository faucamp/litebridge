package org.litebridge.orm.api.select;

public interface ConditionClauseTerminal<DTO,
        CC extends ConditionClause<DTO, CC, SELF>,
        SELF extends ConditionClauseTerminal<DTO, CC, SELF>> {

    CC and(String column);

}

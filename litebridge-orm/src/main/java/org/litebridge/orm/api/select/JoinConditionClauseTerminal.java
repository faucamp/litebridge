package org.litebridge.orm.api.select;

public interface JoinConditionClauseTerminal<DTO> extends
        ConditionClauseTerminal<DTO, JoinConditionClauseTerminal<DTO>>,
        ConditionClause<DTO, JoinConditionClauseTerminal<DTO>>,
        SelectTerminal<DTO> {

    WhereConditionClause<DTO> where(final String column);

    JoinClause<DTO> join(String table);

}

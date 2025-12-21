package org.litebridge.orm.api.select;

public interface FromClauseTerminal<DTO> extends SelectTerminal<DTO> {

    JoinClause<DTO> join(String table);
    WhereConditionClause<DTO> where(final String column);


}

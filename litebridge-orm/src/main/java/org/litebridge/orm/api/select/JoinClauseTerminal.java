package org.litebridge.orm.api.select;

public interface JoinClauseTerminal<DTO> extends WhereClauseTerminal<DTO> {

    WhereConditionClause<DTO> where(final String column);

    JoinClause<DTO> join(String table);

}

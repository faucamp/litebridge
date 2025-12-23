package org.litebridge.orm.api.select;

public interface WhereClauseTerminal<DTO> extends OrderByClauseTerminal<DTO> {

    OrderByClause<DTO> orderBy(String... column);
}

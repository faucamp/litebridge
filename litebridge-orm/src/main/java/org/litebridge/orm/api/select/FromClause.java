package org.litebridge.orm.api.select;

public interface FromClause<DTO> {

    FromClauseTerminal<DTO> from(final String table);
}

package org.litebridge.orm.api.select;

public interface OrderByClauseTerminal<DTO> extends LimitClauseTerminal<DTO> {

    LimitClauseTerminal<DTO> limit(final int limit);
}

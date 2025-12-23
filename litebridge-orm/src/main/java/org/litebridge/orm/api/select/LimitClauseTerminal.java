package org.litebridge.orm.api.select;

public interface LimitClauseTerminal<DTO> extends SelectTerminal<DTO> {

    SelectTerminal<DTO> offset(int offset);

}

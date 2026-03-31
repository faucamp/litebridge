package org.litebridge.orm.api.delete;

public interface DeleteFromClauseTerminal<DTO,
        DT extends DeleteTerminal<DTO>> extends DeleteTerminal<DTO> {

    DT where(final String column);
}

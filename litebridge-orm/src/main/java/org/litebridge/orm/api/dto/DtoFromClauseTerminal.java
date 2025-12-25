package org.litebridge.orm.api.dto;

import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.api.select.impl.FromClauseTerminalImpl;
import org.litebridge.orm.api.select.impl.JoinClauseImpl;
import org.litebridge.orm.api.select.impl.JoinConditionClauseImpl;
import org.litebridge.orm.api.select.impl.JoinConditionClauseTerminalImpl;

public class DtoFromClauseTerminal<DTO> extends FromClauseTerminalImpl<DTO,
        JoinClauseImpl<DTO>,
        JoinConditionClauseImpl<DTO, JoinConditionClauseTerminalImpl<DTO>>,
        JoinConditionClauseTerminalImpl<DTO>> {

    public DtoFromClauseTerminal(final AbstractSelector<DTO> delegate) {
        super(delegate);
    }
}

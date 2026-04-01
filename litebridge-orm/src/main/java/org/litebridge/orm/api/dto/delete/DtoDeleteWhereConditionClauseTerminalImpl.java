package org.litebridge.orm.api.dto.delete;

import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.delete.DeleteTerminal;

public final class DtoDeleteWhereConditionClauseTerminalImpl<DTO>
        implements DtoDeleteWhereConditionClauseTerminal<DTO>,
        DeleteTerminal {

    private final DtoDeletor<DTO> delegate;

    public DtoDeleteWhereConditionClauseTerminalImpl(final DtoDeletor<DTO> delegate) {
        this.delegate = delegate;
    }

    @Override
    public DtoDeleteWhereConditionClause<DTO> and(final String column) {
        return delegate.where(column);
    }

    @Override
    public UpdateResult execute() {
        return delegate.execute();
    }
}

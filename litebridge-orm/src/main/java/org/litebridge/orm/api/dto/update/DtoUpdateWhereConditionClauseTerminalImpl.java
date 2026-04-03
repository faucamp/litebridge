package org.litebridge.orm.api.dto.update;

import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.update.UpdateTerminal;
import org.litebridge.orm.api.update.model.UpdateSpec;

public final class DtoUpdateWhereConditionClauseTerminalImpl<DTO>
        implements DtoUpdateWhereConditionClauseTerminal<DTO>,
        UpdateTerminal {

    private final DtoUpdater<DTO> delegate;

    public DtoUpdateWhereConditionClauseTerminalImpl(final DtoUpdater<DTO> delegate) {
        this.delegate = delegate;
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> and(final String column) {
        return delegate.where(column);
    }

    @Override
    public UpdateSpec updateSpec() {
        return delegate.updateSpec();
    }

    @Override
    public UpdateResult execute() {
        return delegate.execute();
    }
}

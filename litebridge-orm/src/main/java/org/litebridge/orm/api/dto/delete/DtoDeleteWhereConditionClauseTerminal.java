package org.litebridge.orm.api.dto.delete;

import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.delete.DeleteWhereConditionClauseTerminal;

public class DtoDeleteWhereConditionClauseTerminal<DTO>

        implements DeleteWhereConditionClauseTerminal<DTO,
        DtoDeleteWhereConditionClause<DTO>,
        DtoDeleteWhereConditionClauseTerminal<DTO>> {

    private final DtoDeletor<DTO> delegate;

    public DtoDeleteWhereConditionClauseTerminal(final DtoDeletor<DTO> delegate) {
        this.delegate = delegate;
    }

    @Override
    public UpdateResult execute() {
        return delegate.execute();
    }

    @Override
    public DtoDeleteWhereConditionClause<DTO> and(final String column) {
        return delegate.where(column);
    }
}

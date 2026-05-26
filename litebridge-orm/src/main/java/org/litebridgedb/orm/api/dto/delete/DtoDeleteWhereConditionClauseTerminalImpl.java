package org.litebridgedb.orm.api.dto.delete;

import org.litebridgedb.db.spi.update.UpdateResult;
import org.litebridgedb.orm.api.delete.DeleteTerminal;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;

public final class DtoDeleteWhereConditionClauseTerminalImpl<DTO>
        implements DtoDeleteWhereConditionClauseTerminal<DTO>,
        DeleteTerminal {

    private final DtoDeletor<DTO> delegate;

    public DtoDeleteWhereConditionClauseTerminalImpl(final DtoDeletor<DTO> delegate) {
        this.delegate = delegate;
    }

    @Override
    public DtoDeleteWhereConditionClause<DTO> and(final String field) {
        return delegate.where(field);
    }

    @Override
    public DtoDeleteWhereConditionClause<DTO> and(final FieldColumnSpec field) {
        return and(field.field().name());
    }

    @Override
    public UpdateResult execute() {
        return delegate.execute();
    }
}

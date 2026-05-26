package org.litebridgedb.orm.api.dto.update;

import org.litebridgedb.db.spi.update.UpdateResult;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.api.update.UpdateTerminal;
import org.litebridgedb.orm.api.update.model.UpdateSpec;

public final class DtoUpdateWhereConditionClauseTerminalImpl<DTO>
        implements DtoUpdateWhereConditionClauseTerminal<DTO>,
        UpdateTerminal {

    private final DtoUpdater<DTO> delegate;

    public DtoUpdateWhereConditionClauseTerminalImpl(final DtoUpdater<DTO> delegate) {
        this.delegate = delegate;
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> and(final String field) {
        return delegate.where(field);
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> and(final FieldColumnSpec field) {
        return and(field.field().name());
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

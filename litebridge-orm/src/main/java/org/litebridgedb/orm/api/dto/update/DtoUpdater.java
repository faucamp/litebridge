package org.litebridgedb.orm.api.dto.update;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.api.update.UpdateSetStep;
import org.litebridgedb.orm.api.update.impl.AbstractUpdater;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;

public final class DtoUpdater<DTO> extends AbstractUpdater<DtoUpdateSpec> implements DtoUpdateStep<DTO> {

    public DtoUpdater(final Class<DTO> dtoClass,
                      final OrmTable dtoTable,
                      final TransactionalDatabaseProvider databaseProvider,
                      final LitebridgeContext litebridgeContext) {
        super(new DtoUpdateSpec(dtoClass, dtoTable, litebridgeContext.selectExpressionMapper()), databaseProvider, litebridgeContext);
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> where(final String field) {
        final Column column = updateSpec.dtoTable().getColumnForFieldName(field).toColumn();
        return new DtoUpdateWhereConditionClause<>(updateSpec.newWhereCondition(column), new DtoUpdateWhereConditionClauseTerminalImpl<>(this), litebridgeContext);
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> where(final ColumnExpressionSpec field) {
        return where(field.getColumn().name());
    }

    @Override
    public UpdateSetStep<DtoUpdateStep<DTO>> set(final String field) {
        final Column column = updateSpec.dtoTable().getColumnForFieldName(field).toColumn();
        return new UpdateSetStep<>(column, this);
    }

    @Override
    public UpdateSetStep<DtoUpdateStep<DTO>> set(final ColumnExpressionSpec field) {
        final Column column = field.getColumn();
        return new UpdateSetStep<>(column, this);
    }
}

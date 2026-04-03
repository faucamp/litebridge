package org.litebridge.orm.api.dto.update;

import org.litebridge.db.spi.Column;
import org.litebridge.orm.api.spec.FieldColumnSpec;
import org.litebridge.orm.api.update.UpdateSetStep;
import org.litebridge.orm.api.update.impl.AbstractUpdater;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.tracking.ClassFieldAccessorCache;

public final class DtoUpdater<DTO> extends AbstractUpdater<DtoUpdateSpec> implements DtoUpdateStep<DTO> {

    public DtoUpdater(final Class<DTO> dtoClass,
                      final OrmTable dtoTable,
                      final TableRegistry tableRegistry,
                      final ClassFieldAccessorCache classFieldAccessorCache,
                      final TransactionalDatabaseProvider databaseProvider) {
        super(new DtoUpdateSpec(dtoClass, dtoTable), databaseProvider);
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> where(final String field) {
        final Column column = updateSpec.dtoTable().getColumnForFieldName(field).toColumn();
        return new DtoUpdateWhereConditionClause<>(updateSpec.newWhereCondition(column), new DtoUpdateWhereConditionClauseTerminalImpl<>(this));
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> where(final FieldColumnSpec field) {
        return where(field.field().name());
    }

    @Override
    public UpdateSetStep<DtoUpdateStep<DTO>> set(final String field) {
        final Column column = updateSpec.dtoTable().getColumnForFieldName(field).toColumn();
        return new UpdateSetStep<>(column, this);
    }

    @Override
    public UpdateSetStep<DtoUpdateStep<DTO>> set(final FieldColumnSpec field) {
        final Column column = updateSpec.dtoTable().getColumnForFieldName(field.field().name()).toColumn();
        return new UpdateSetStep<>(column, this);
    }
}

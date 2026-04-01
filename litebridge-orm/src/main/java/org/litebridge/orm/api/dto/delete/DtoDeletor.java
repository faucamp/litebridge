package org.litebridge.orm.api.dto.delete;

import org.litebridge.db.spi.Column;
import org.litebridge.orm.api.delete.impl.AbstractDeletor;
import org.litebridge.orm.api.spec.FieldColumnSpec;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.tracking.ClassFieldAccessorCache;

public final class DtoDeletor<DTO> extends AbstractDeletor<DtoDeleteSpec> implements DtoDeleteWhereClause<DTO> {

    public DtoDeletor(final Class<DTO> dtoClass,
                      final OrmTable dtoTable,
                      final TableRegistry tableRegistry,
                      final ClassFieldAccessorCache classFieldAccessorCache,
                      final TransactionalDatabaseProvider databaseProvider) {
        super(new DtoDeleteSpec(dtoClass, dtoTable), databaseProvider);
    }

    @Override
    public DtoDeleteWhereConditionClause<DTO> where(final String field) {
        final Column column = deleteSpec.dtoTable().getColumnForFieldName(field).toColumn();
        return new DtoDeleteWhereConditionClause<>(deleteSpec.newWhereCondition(column), new DtoDeleteWhereConditionClauseTerminalImpl<>(this));
    }

    @Override
    public DtoDeleteWhereConditionClause<DTO> where(final FieldColumnSpec field) {
        return where(field.field().name());
    }
}

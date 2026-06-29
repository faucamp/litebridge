package org.litebridgedb.orm.api.dto.delete;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.api.delete.impl.AbstractDeletor;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;

public final class DtoDeletor<DTO> extends AbstractDeletor<DtoDeleteSpec> implements DtoDeleteWhereClause<DTO> {

    private final LitebridgeContext litebridgeContext;

    public DtoDeletor(final Class<DTO> dtoClass,
                      final OrmTable dtoTable,
                      final TransactionalDatabaseProvider databaseProvider,
                      final LitebridgeContext litebridgeContext) {
        super(new DtoDeleteSpec(dtoClass, dtoTable, litebridgeContext.selectExpressionMapper()), databaseProvider);
        this.litebridgeContext = litebridgeContext;
    }

    @Override
    public DtoDeleteWhereConditionClause<DTO> where(final String field) {
        final Column column = deleteSpec.dtoTable().getColumnForFieldName(field).toColumn();
        return new DtoDeleteWhereConditionClause<>(deleteSpec.newWhereCondition(column), new DtoDeleteWhereConditionClauseTerminalImpl<>(this), litebridgeContext);
    }

    @Override
    public DtoDeleteWhereConditionClause<DTO> where(final ColumnExpressionSpec field) {
        return where(field.getColumn().name());
    }
}

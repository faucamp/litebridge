package org.litebridgedb.orm.api.dto.delete;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.orm.api.condition.QueryConditionBuilder;
import org.litebridgedb.orm.api.delete.impl.AbstractDeletor;
import org.litebridgedb.orm.api.dto.condition.DtoConditionClauseStart;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.api.select.model.SelectExpressionMapper;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;

public final class DtoDeletor<DTO> extends AbstractDeletor<DtoDeleteSpec> implements DtoDeleteWhereClause<DTO> {

    private final LitebridgeContext litebridgeContext;

    public DtoDeletor(final Class<DTO> dtoClass,
                      final OrmTable dtoTable,
                      final TransactionalDatabaseProvider databaseProvider,
                      final SelectExpressionMapper selectExpressionMapper,
                      final LitebridgeContext litebridgeContext) {
        super(new DtoDeleteSpec(dtoClass, dtoTable, selectExpressionMapper), databaseProvider);
        this.litebridgeContext = litebridgeContext;
    }

    @Override
    public DtoDeleteWhereConditionClause<DTO> where(final String field) {
        return whereImpl(LogicOperator.NOOP, field);
    }

    @Override
    public DtoDeleteWhereConditionClause<DTO> where(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.NOOP, expression);
    }

    DtoDeleteWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final String field) {
        final Column column = deleteSpec.dtoTable().getColumnForFieldName(field).toColumn();
        return whereImpl(logicOperator, new SelectColumnSpec(column));
    }

    DtoDeleteWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final ConditionSpec conditionSpec = deleteSpec.currentConditionGroupSpec().newCondition(logicOperator, expression);
        return new DtoDeleteWhereConditionClause<>(conditionSpec, new DtoDeleteWhereConditionClauseTerminalImpl<>(this), litebridgeContext);
    }

    DtoDeleteWhereConditionClauseTerminalImpl<DTO> whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
        final ConditionGroupSpec subgroup = deleteSpec.pushConditionGroupSpec(logicOperator);
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(subgroup, deleteSpec.dtoTable(), litebridgeContext.fromClauseEngine());
        query.apply(conditionClauseStart);
        return new DtoDeleteWhereConditionClauseTerminalImpl<>(this);
    }
}

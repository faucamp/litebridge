package org.litebridge.orm.api.dto.delete;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.delete.impl.AbstractDeletor;
import org.litebridge.orm.api.dto.condition.DtoConditionClauseStart;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;

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
        deleteSpec.popConditionGroupSpec();
        return new DtoDeleteWhereConditionClauseTerminalImpl<>(this);
    }
}

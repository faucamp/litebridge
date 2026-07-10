package org.litebridgedb.orm.api.dto.update;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.orm.api.condition.QueryConditionBuilder;
import org.litebridgedb.orm.api.dto.condition.DtoConditionClauseStart;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.api.select.model.SelectExpressionMapper;
import org.litebridgedb.orm.api.update.UpdateSetStep;
import org.litebridgedb.orm.api.update.impl.AbstractUpdater;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.meta.QFInspector;
import org.litebridgedb.orm.meta.QueryField;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;

public final class DtoUpdater<DTO> extends AbstractUpdater<DtoUpdateSpec> implements DtoUpdateStep<DTO> {

    public DtoUpdater(final Class<DTO> dtoClass,
                      final OrmTable dtoTable,
                      final TransactionalDatabaseProvider databaseProvider,
                      final SelectExpressionMapper selectExpressionMapper,
                      final LitebridgeContext litebridgeContext) {
        super(new DtoUpdateSpec(dtoClass, dtoTable, selectExpressionMapper), databaseProvider, litebridgeContext);
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> where(final String field) {
        return whereImpl(LogicOperator.NOOP, field);
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> where(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.NOOP, expression);
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

    @Override
    public UpdateSetStep<DtoUpdateStep<DTO>> set(final QueryField field) {
        final Column column = updateSpec.dtoTable().getColumnForFieldName(QFInspector.getFieldName(field)).toColumn();
        return new UpdateSetStep<>(column, this);
    }

    DtoUpdateWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final String field) {
        final Column column = updateSpec.dtoTable().getColumnForFieldName(field).toColumn();
        return whereImpl(logicOperator, new SelectColumnSpec(column));
    }

    DtoUpdateWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final ConditionSpec conditionSpec = updateSpec.currentConditionGroupSpec().newCondition(logicOperator, expression);
        return new DtoUpdateWhereConditionClause<>(conditionSpec, new DtoUpdateWhereConditionClauseTerminalImpl<>(this), litebridgeContext);
    }

    DtoUpdateWhereConditionClauseTerminalImpl<DTO> whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
        final ConditionGroupSpec subgroup = updateSpec.pushConditionGroupSpec(logicOperator);
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(subgroup, updateSpec.dtoTable(), litebridgeContext.fromClauseEngine());
        query.apply(conditionClauseStart);
        updateSpec.popConditionGroupSpec();
        return new DtoUpdateWhereConditionClauseTerminalImpl<>(this);
    }
}

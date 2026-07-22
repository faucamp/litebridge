package org.litebridge.orm.api.dto.update;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.dto.condition.DtoConditionClauseStart;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.api.update.UpdateSetStep;
import org.litebridge.orm.api.update.impl.AbstractUpdater;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.meta.QFInspector;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;

/**
 * Updater for DTOs.
 *
 * @param <DTO> the DTO type
 */
public final class DtoUpdater<DTO> extends AbstractUpdater<DtoUpdateSpec> implements DtoUpdateStep<DTO> {

    /**
     * Creates a new DtoUpdater.
     *
     * @param dtoClass                 the DTO class
     * @param dtoTable                 the DTO table
     * @param databaseProvider         the database provider
     * @param selectExpressionMapper the select expression mapper
     * @param litebridgeContext        the litebridge context
     */
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

        final java.util.function.Function<org.litebridge.orm.api.select.ast.QueryNode, DtoUpdateWhereConditionClauseTerminal<DTO>> recreator = n -> new DtoUpdateWhereConditionClauseTerminalImpl<>(this);

        return new DtoUpdateWhereConditionClause<>(conditionSpec, litebridgeContext, logicOperator, expression, recreator);
    }

    DtoUpdateWhereConditionClauseTerminalImpl<DTO> whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
        final ConditionGroupSpec subgroup = updateSpec.pushConditionGroupSpec(logicOperator);
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(subgroup, updateSpec.dtoTable(), litebridgeContext.fromClauseEngine());
        query.apply(conditionClauseStart);
        updateSpec.popConditionGroupSpec();
        return new DtoUpdateWhereConditionClauseTerminalImpl<>(this);
    }
}

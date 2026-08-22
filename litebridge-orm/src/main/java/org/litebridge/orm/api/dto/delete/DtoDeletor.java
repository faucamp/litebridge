package org.litebridge.orm.api.dto.delete;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.delete.impl.AbstractDeletor;
import org.litebridge.orm.api.dto.condition.DtoConditionClauseStart;
import org.litebridge.orm.api.select.ast.ConditionGroupNode;
import org.litebridge.orm.api.select.ast.DeleteNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.OrmTable;

import java.util.function.Function;

/**
 * Executor for DTO delete operations.
 *
 * @param <DTO> the type of the DTO
 */
public final class DtoDeletor<DTO> extends AbstractDeletor<DtoDeleteSpec> implements DtoDeleteWhereClause<DTO> {

    /**
     * Creates a new DtoDeletor.
     *
     * @param dtoClass               the DTO class
     * @param dtoTable               the DTO table
     * @param databaseProvider       the database provider
     * @param selectExpressionMapper the select expression mapper
     * @param litebridgeContext      the Litebridge context
     */
    public DtoDeletor(final Class<DTO> dtoClass,
                      final OrmTable dtoTable,
                      final LitebridgeContext litebridgeContext) {
        super(new DtoDeleteSpec(dtoClass, dtoTable, litebridgeContext.selectExpressionMapper()), litebridgeContext, new DeleteNode(null, null, dtoClass));
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
        final Function<QueryNode, DtoDeleteWhereConditionClauseTerminal<DTO>> recreator = n -> {
            this.node = new WhereNode(this.node, n);
            return new DtoDeleteWhereConditionClauseTerminalImpl<>(this);
        };
        return new DtoDeleteWhereConditionClause<>(litebridgeContext, logicOperator, expression, recreator);
    }

    DtoDeleteWhereConditionClauseTerminalImpl<DTO> whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(deleteSpec.dtoTable(), litebridgeContext.fromClauseEngine(), null);
        final AbstractCbConditionClauseTerminal<DTO> terminal = query.apply(conditionClauseStart);
        this.node = new WhereNode(this.node, new ConditionGroupNode(null, logicOperator, terminal.node()));
        return new DtoDeleteWhereConditionClauseTerminalImpl<>(this);
    }
}

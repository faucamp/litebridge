package org.litebridge.orm.api.update;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.dto.condition.DtoConditionClauseStart;
import org.litebridge.orm.api.select.ast.ConditionGroupNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.OrmTable;

import java.util.function.Function;

/**
 * Implementation of the terminal interface for DTO update where condition clauses.
 *
 * @param <DTO> the DTO type
 */
public final class DtoUpdateWhereConditionClauseTerminalImpl<DTO>
        implements DtoUpdateWhereConditionClauseTerminal<DTO> {

    private final Class<DTO> dtoClass;
    private final LitebridgeContext litebridgeContext;
    private QueryNode node;

    /**
     * Creates a new DtoUpdateWhereConditionClauseTerminalImpl.
     */
    public DtoUpdateWhereConditionClauseTerminalImpl(final Class<DTO> dtoClass,
                                                     final QueryNode node,
                                                     final LitebridgeContext litebridgeContext) {
        this.dtoClass = dtoClass;
        this.node = node;
        this.litebridgeContext = litebridgeContext;
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> and(final String field) {
        return whereImpl(LogicOperator.AND, field);
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> and(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.AND, expression);
    }

    @Override
    public DtoUpdateWhereConditionClauseTerminal<DTO> and(final QueryConditionBuilder<DTO> query) {
        return whereImpl(LogicOperator.AND, query);
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> or(final String field) {
        return whereImpl(LogicOperator.OR, field);
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> or(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.OR, expression);
    }

    @Override
    public DtoUpdateWhereConditionClauseTerminal<DTO> or(final QueryConditionBuilder<DTO> query) {
        return whereImpl(LogicOperator.OR, query);
    }

    private DtoUpdateWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final String field) {
        final Column column = litebridgeContext.tableRegistry().getTableOrThrow(dtoClass).getColumnForFieldName(field).toColumn();
        return whereImpl(logicOperator, new SelectColumnSpec(column));
    }

    private DtoUpdateWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final Function<QueryNode, DtoUpdateWhereConditionClauseTerminal<DTO>> recreator = n -> {
            this.node = new WhereNode(this.node, n);
            return this;
        };

        return new DtoUpdateWhereConditionClause<>(litebridgeContext, logicOperator, expression, recreator);
    }

    private DtoUpdateWhereConditionClauseTerminalImpl<DTO> whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
        final OrmTable ormTable = litebridgeContext.tableRegistry().getTableOrThrow(dtoClass);
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(ormTable, litebridgeContext.fromClauseEngine(), null);
        final AbstractCbConditionClauseTerminal<DTO> terminal = query.apply(conditionClauseStart);
        this.node = new WhereNode(this.node, new ConditionGroupNode(null, logicOperator, terminal.node()));
        return this;
    }

    QueryNode node() {
        return node;
    }
}

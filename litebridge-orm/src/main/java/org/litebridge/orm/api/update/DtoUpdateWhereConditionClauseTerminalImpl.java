package org.litebridge.orm.api.update;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

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
        return whereImpl(LogicOperator.AND, field, null);
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> and(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.AND, null, expression);
    }

    @Override
    public DtoUpdateWhereConditionClauseTerminal<DTO> and(final QueryConditionBuilder<DTO> query) {
        return whereImpl(LogicOperator.AND, query);
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> or(final String field) {
        return whereImpl(LogicOperator.OR, field, null);
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> or(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.OR, null, expression);
    }

    @Override
    public DtoUpdateWhereConditionClauseTerminal<DTO> or(final QueryConditionBuilder<DTO> query) {
        return whereImpl(LogicOperator.OR, query);
    }

    private DtoUpdateWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final @Nullable String field, final @Nullable ExpressionSpec expression) {
        final Function<QueryNode, DtoUpdateWhereConditionClauseTerminal<DTO>> recreator = n -> {
            this.node = new WhereNode(this.node, n);
            return this;
        };

        return new DtoUpdateWhereConditionClause<>(litebridgeContext, logicOperator, field, expression, recreator);
    }

    private DtoUpdateWhereConditionClauseTerminalImpl<DTO> whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
//        final OrmTable ormTable = litebridgeContext.tableRegistry().getTableOrThrow(dtoClass);
//        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(ormTable, litebridgeContext.fromClauseEngine(), null);
//        final AbstractCbConditionClauseTerminal<DTO> terminal = query.apply(conditionClauseStart);
//        this.node = new WhereNode(this.node, new ConditionGroupNode(null, logicOperator, terminal.node()));
//        return this;
        throw new UnsupportedOperationException("Not implemented yet");
    }

    QueryNode node() {
        return node;
    }
}

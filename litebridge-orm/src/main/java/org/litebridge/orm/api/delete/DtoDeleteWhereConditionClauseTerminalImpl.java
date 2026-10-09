package org.litebridge.orm.api.delete;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.CbConditionClauseTerminalInspector;
import org.litebridge.orm.api.condition.CbDtoConditionClauseTerminal;
import org.litebridge.orm.api.condition.DtoConditionClauseStart;
import org.litebridge.orm.api.condition.DtoQueryConditionBuilder;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

/**
 * Implementation of {@link DtoDeleteWhereConditionClauseTerminal} for DTO delete operations.
 *
 * @param <DTO> the type of the DTO
 */
public final class DtoDeleteWhereConditionClauseTerminalImpl<DTO>
        implements DtoDeleteWhereConditionClauseTerminal<DTO>, DeleteTerminal {

    private final LitebridgeContext litebridgeContext;
    private QueryNode node;

    /**
     * Creates a new {@code DtoDeleteWhereConditionClauseTerminalImpl} instance.
     *
     * @param node              the current query node
     * @param litebridgeContext the Litebridge context
     */
    public DtoDeleteWhereConditionClauseTerminalImpl(final QueryNode node,
                                                     final LitebridgeContext litebridgeContext) {
        this.node = node;
        this.litebridgeContext = litebridgeContext;
    }

    @Override
    public DtoDeleteWhereConditionClause<DTO> and(final String field) {
        return whereImpl(LogicOperator.AND, field, null);
    }

    @Override
    public DtoDeleteWhereConditionClause<DTO> and(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.AND, null, expression);
    }

    @Override
    public DtoDeleteWhereConditionClauseTerminal<DTO> and(final DtoQueryConditionBuilder<DTO> query) {
        return whereImpl(LogicOperator.AND, query);
    }

    @Override
    public DtoDeleteWhereConditionClause<DTO> or(final String field) {
        return whereImpl(LogicOperator.OR, field, null);
    }

    @Override
    public DtoDeleteWhereConditionClause<DTO> or(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.OR, null, expression);
    }

    @Override
    public DtoDeleteWhereConditionClauseTerminal<DTO> or(final DtoQueryConditionBuilder<DTO> query) {
        return whereImpl(LogicOperator.OR, query);
    }

    private DtoDeleteWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final @Nullable String field, final @Nullable ExpressionSpec expression) {
        final Function<QueryNode, DtoDeleteWhereConditionClauseTerminal<DTO>> recreator = n -> {
            this.node = new WhereNode(this.node, n);
            return this;
        };

        return new DtoDeleteWhereConditionClause<>(litebridgeContext, logicOperator, field, expression, recreator);
    }

    private DtoDeleteWhereConditionClauseTerminalImpl<DTO> whereImpl(final LogicOperator logicOperator, final DtoQueryConditionBuilder<DTO> query) {
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(null, litebridgeContext);
        final CbDtoConditionClauseTerminal<DTO> terminal = query.apply(conditionClauseStart);
        this.node = new WhereNode(this.node, new ConditionGroupNode(null, logicOperator, CbConditionClauseTerminalInspector.getNode(terminal)));
        return this;
    }

    QueryNode node() {
        return node;
    }
}

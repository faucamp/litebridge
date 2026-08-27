package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.ast.OrderByNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.Objects;

/**
 * Represents an ORDER BY clause in a DTO-based query.
 *
 * @param <DTO> the type of the DTO being queried
 */
public final class DtoOrderByClause<DTO>
        implements OrderByClause<DTO, DtoOrderByClause<DTO>, DtoOrderByClauseChain<DTO>> {

    private final String @Nullable [] columns;
    private final ExpressionSpec @Nullable [] expressions;
    private QueryNode node;
    private final SelectEngineTerminal selectEngineTerminal;
    private final LitebridgeContext litebridgeContext;

    /**
     * Creates a new instance of {@code DtoOrderByClause}.
     *
     * @param expressions the expressions to order by
     */
    public DtoOrderByClause(final ExpressionSpec[] expressions,
                            final QueryNode node,
                            final SelectEngineTerminal selectEngineTerminal,
                            final LitebridgeContext litebridgeContext) {
        this(null, expressions, node, selectEngineTerminal, litebridgeContext);
    }

    public DtoOrderByClause(final String[] columns,
                            final QueryNode node,
                            final SelectEngineTerminal selectEngineTerminal,
                            final LitebridgeContext litebridgeContext) {
        this(columns, null, node, selectEngineTerminal, litebridgeContext);
    }

    private DtoOrderByClause(
            final String @Nullable [] columns,
            final ExpressionSpec @Nullable [] expressions,
            final QueryNode node,
            final SelectEngineTerminal selectEngineTerminal,
            final LitebridgeContext litebridgeContext) {
        this.columns = columns;
        this.expressions = expressions;
        this.node = node;
        this.selectEngineTerminal = selectEngineTerminal;
        this.litebridgeContext = litebridgeContext;
    }

    @Override
    public DtoOrderByClauseChain<DTO> asc() {
        return createDtoOrderByClauseChain(true);
    }

    @Override
    public DtoOrderByClauseChain<DTO> desc() {
        return createDtoOrderByClauseChain(false);
    }

    private DtoOrderByClauseChain<DTO> createDtoOrderByClauseChain(final boolean ascending) {
        if (expressions != null) {
            for (final ExpressionSpec expression : expressions) {
                node = new OrderByNode(node, null, expression, ascending);
            }
        } else {
            for (final String column : Objects.requireNonNull(columns)) {
                node = new OrderByNode(node, column, null, ascending);
            }
        }

        return new DtoOrderByClauseChain<>(node, selectEngineTerminal, litebridgeContext);
    }
}

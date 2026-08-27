package org.litebridge.orm.api.dto;

import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.impl.OrderByClauseTerminalImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Represents a chain of ORDER BY clauses in a DTO-based query.
 *
 * @param <DTO> the type of the DTO being queried
 */
public final class DtoOrderByClauseChain<DTO>
        extends OrderByClauseTerminalImpl<DTO>
        implements OrderByClauseChain<DTO, DtoOrderByClause<DTO>, DtoOrderByClauseChain<DTO>> {

    /**
     * Creates a new instance of {@code DtoOrderByClauseChain}.
     */
    public DtoOrderByClauseChain(final QueryNode node, final SelectEngineTerminal selectEngineTerminal, final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public DtoOrderByClause<DTO> then(final String... fields) {
//        return new DtoOrderByClause<>(((DtoSelector<DTO>) delegate).createSelectFieldSpecs(fields).toArray(ExpressionSpec[]::new), (DtoSelector<DTO>) delegate);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public DtoOrderByClause<DTO> then(final ExpressionSpec... fields) {
//        return new DtoOrderByClause<>(fields, (DtoSelector<DTO>) delegate);
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

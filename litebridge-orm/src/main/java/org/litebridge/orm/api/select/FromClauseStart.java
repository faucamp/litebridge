package org.litebridge.orm.api.select;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.SelectNode;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.engine.FromClauseEngine;

/**
 * Entry point for the "FROM" clause of a query.
 */
public final class FromClauseStart extends AbstractFromClauseStart {

    /**
     * Constructs a new {@code FromClauseStart}.
     *
     * @param node             the current query node.
     * @param fromClauseEngine the from clause engine.
     */
    public FromClauseStart(final SelectNode node, final FromClauseEngine fromClauseEngine) {
        super(node, fromClauseEngine);
    }

    /**
     * Starts a FROM clause for the given DTO class.
     *
     * @param dtoClass the DTO class.
     * @param <DTO>    the DTO type.
     * @return the DTO from clause terminal.
     */
    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass) {
        return fromClauseEngine.from(node, dtoClass, (RelatedDtoStrategy) null);
    }

    /**
     * Starts a FROM clause for the given DTO class within the context of another DTO class.
     *
     * @param dtoClass        the DTO class.
     * @param contextDtoClass the context DTO class.
     * @param <DTO>           the DTO type.
     * @return the DTO from clause terminal.
     */
    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass, final Class<?> contextDtoClass) {
        return fromClauseEngine.from(dtoClass, contextDtoClass);
    }
}
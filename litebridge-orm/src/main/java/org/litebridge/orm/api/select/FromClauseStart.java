package org.litebridge.orm.api.select;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.dto.DtoFromClauseTerminal;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Entry point for the "FROM" clause of a query.
 */
public final class FromClauseStart extends AbstractFromClauseStart {

    /**
     * Constructs a new {@code FromClauseStart}.
     *
     * @param fromClauseEngine the from clause engine.
     */
    public FromClauseStart(final FromClauseEngine fromClauseEngine) {
        super(fromClauseEngine);
    }

    /**
     * Constructs a new {@code FromClauseStart} with the given expression specifications.
     *
     * @param expressionSpecs  the expression specifications.
     * @param fromClauseEngine the from clause engine.
     */
    public FromClauseStart(final ExpressionSpec[] expressionSpecs, final FromClauseEngine fromClauseEngine) {
        super(expressionSpecs, fromClauseEngine);
    }

    /**
     * Starts a FROM clause for the given DTO class.
     *
     * @param dtoClass the DTO class.
     * @param <DTO>    the DTO type.
     * @return the DTO from clause terminal.
     */
    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass) {
        return fromClauseEngine.from(expressionSpecs, dtoClass, (RelatedDtoStrategy) null);
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

    /**
     * Starts a FROM clause for the given DTO class with a specific related DTO strategy.
     *
     * @param dtoClass           the DTO class.
     * @param relatedDtoStrategy the related DTO strategy.
     * @param <DTO>              the DTO type.
     * @return the DTO from clause terminal.
     */
    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass, final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        return fromClauseEngine.from(expressionSpecs, dtoClass, relatedDtoStrategy);
    }
}
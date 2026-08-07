package org.litebridge.orm.api.select;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.SelectNode;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Entry point for the "FROM" clause of a query with a type override.
 * @param <TypeOverride> the type override.
 */
public final class FromClauseStartTypeOverride<TypeOverride> extends AbstractFromClauseStart {

    private final Class<TypeOverride> typeOverride;

    /**
     * Constructs a new {@code FromClauseStartTypeOverride}.
     *
     * @param typeOverride     the type override class.
     * @param node             the current query node.
     * @param fromClauseEngine the from clause engine.
     */
    public FromClauseStartTypeOverride(final Class<TypeOverride> typeOverride,
                                       final SelectNode node,
                                       final FromClauseEngine fromClauseEngine) {
        super(node, fromClauseEngine);
        this.typeOverride = typeOverride;
    }

    /**
     * Starts a FROM clause for the given DTO class with the current type override.
     *
     * @param dtoClass the DTO class.
     * @return the DTO from clause terminal.
     */
    public DtoFromClauseTerminal<TypeOverride> from(final Class<?> dtoClass) {
        return fromClauseEngine.from(node, dtoClass, typeOverride, (RelatedDtoStrategy) null);
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
        return fromClauseEngine.from(node, dtoClass, relatedDtoStrategy);
    }
}

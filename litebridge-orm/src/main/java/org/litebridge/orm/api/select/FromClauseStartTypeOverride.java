package org.litebridge.orm.api.select;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.sql.SqlFromClauseTerminal;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.TypeOverride;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Entry point for the "FROM" clause of a query with a type override.
 *
 * @param <ReturnType> the type override.
 */
public final class FromClauseStartTypeOverride<ReturnType> {

    private final Class<ReturnType> typeOverride;
    private final ExpressionSpec[] expressionSpecs;
    private final SelectEngineTerminal selectEngineTerminal;
    private final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator;

    /**
     * Constructs a new {@code FromClauseStartTypeOverride}.
     *
     * @param typeOverride the type override class.
     */
    public FromClauseStartTypeOverride(final Class<ReturnType> typeOverride,
                                       final ExpressionSpec[] expressionSpecs,
                                       final SelectEngineTerminal selectEngineTerminal,
                                       final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator) {
        this.typeOverride = typeOverride;
        this.expressionSpecs = expressionSpecs;
        this.selectEngineTerminal = selectEngineTerminal;
        this.litebridgeContextCreator = litebridgeContextCreator;
    }

    /**
     * Starts a FROM clause for the given DTO class with the current type override.
     *
     * @param dtoClass the DTO class.
     * @return the DTO from clause terminal.
     */
    public DtoFromClauseTerminal<ReturnType> from(final Class<?> dtoClass) {
        return from(dtoClass, (RelatedDtoStrategy) null);
    }

    /**
     * Starts a FROM clause for the given DTO class within the context of another DTO class.
     *
     * @param dtoClass        the DTO class.
     * @param contextDtoClass the context DTO class.
     * @return the DTO from clause terminal.
     */
    public DtoFromClauseTerminal<ReturnType> from(final Class<?> dtoClass, final Class<?> contextDtoClass) {
        final SelectNode selectNode = new SelectNode(null, dtoClass, contextDtoClass, null, expressionSpecs, new Class<?>[]{typeOverride});
        return new DtoFromClauseTerminal<>(selectNode, selectEngineTerminal, litebridgeContextCreator.apply(LitebridgeContext.Mode.DTO));
    }

    /**
     * Starts a FROM clause for the given DTO class with a specific related DTO strategy.
     *
     * @param dtoClass           the DTO class.
     * @param relatedDtoStrategy the related DTO strategy.
     * @return the DTO from clause terminal.
     */
    public DtoFromClauseTerminal<ReturnType> from(final Class<?> dtoClass, final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        // Check for row column-level type overrides
        final Class<?>[] expressionReturnTypes = Arrays.stream(expressionSpecs)
                .filter(TypeOverride.class::isInstance)
                .map(TypeOverride.class::cast)
                .map(TypeOverride::returnType)
                .toArray(Class<?>[]::new);

        final Class<?>[] returnTypes;

        if (expressionReturnTypes.length > 0) {
            returnTypes = expressionReturnTypes;
        } else {
            returnTypes = new Class<?>[]{typeOverride};
        }

        final SelectNode selectNode = new SelectNode(null, dtoClass, null, null, expressionSpecs, returnTypes);
        final LitebridgeContext litebridgeContext = litebridgeContextCreator.apply(LitebridgeContext.Mode.DTO);

        if (relatedDtoStrategy != null) {
            litebridgeContext.setRelatedDtoStrategy(relatedDtoStrategy);
        }

        return new DtoFromClauseTerminal<>(selectNode, selectEngineTerminal, litebridgeContext);
    }

    /**
     * Starts a FROM clause for the given SQL table.
     *
     * @param table the table name.
     * @return the SQL from clause terminal.
     */
    public SqlFromClauseTerminal from(final String table) {
        final SelectNode selectNode = new SelectNode(table, null, null, null, expressionSpecs, new Class<?>[]{typeOverride});
        return new SqlFromClauseTerminal(selectNode, selectEngineTerminal, litebridgeContextCreator.apply(LitebridgeContext.Mode.SQL));
    }
}

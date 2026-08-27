package org.litebridge.orm.api.select;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.select.ast.SelectNode;
import org.litebridge.orm.api.sql.SqlFromClauseTerminal;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

/**
 * Entry point for the "FROM" clause of a query with a type override.
 *
 * @param <TypeOverride> the type override.
 */
public final class FromClauseStartTypeOverride<TypeOverride> {

    private final Class<TypeOverride> typeOverride;
    private final ExpressionSpec[] expressionSpecs;
    private final SelectEngineTerminal selectEngineTerminal;
    private final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator;

    /**
     * Constructs a new {@code FromClauseStartTypeOverride}.
     *
     * @param typeOverride the type override class.
     */
    public FromClauseStartTypeOverride(final Class<TypeOverride> typeOverride,
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
    public DtoFromClauseTerminal<TypeOverride> from(final Class<?> dtoClass) {
        final SelectNode selectNode = new SelectNode(null, dtoClass, null, expressionSpecs, typeOverride);
        return new DtoFromClauseTerminal<>(selectNode, selectEngineTerminal, litebridgeContextCreator.apply(LitebridgeContext.Mode.DTO));
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
//        return fromClauseEngine.from(dtoClass, contextDtoClass);
        throw new UnsupportedOperationException("Not implemented yet");
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
//        return fromClauseEngine.from(node, dtoClass, relatedDtoStrategy);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Starts a FROM clause for the given SQL table.
     *
     * @param table the table name.
     * @return the SQL from clause terminal.
     */
    public SqlFromClauseTerminal from(final String table) {
        final SelectNode selectNode = new SelectNode(table, null, null, expressionSpecs, typeOverride);
        return new SqlFromClauseTerminal(selectNode, selectEngineTerminal, litebridgeContextCreator.apply(LitebridgeContext.Mode.SQL));
    }
}

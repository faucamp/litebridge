package org.litebridge.orm.engine;

import org.litebridge.orm.api.select.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.select.FromClauseStart;
import org.litebridge.orm.api.select.FromClauseStartTypeOverride;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.TypeOverride;
import org.litebridge.orm.expression.TypeOverrideExpressionSpec;
import org.litebridge.orm.expression.intent.ConvertIntent;
import org.litebridge.orm.persistence.DtoConstructor;

import java.util.function.Function;

/**
 * Initiates the fluent API for creating {@code SELECT} statements.
 */
public class SelectEngine {

    private final SelectEngineTerminal selectEngineTerminal;

    /**
     * Creates a new {@code SelectEngine} instance.
     *
     * @param dtoConstructor the DTO constructor instance used for building result objects
     */
    public SelectEngine(final DtoConstructor dtoConstructor) {
        this.selectEngineTerminal = new SelectEngineTerminal(dtoConstructor);
    }

    /**
     * Starts a SELECT query for the specified DTO class.
     *
     * @param <DTO>             the DTO type
     * @param dtoClass          the DTO class to query
     * @param litebridgeContext the Litebridge context
     * @return the terminal FROM clause step
     */
    public <DTO> DtoFromClauseTerminal<DTO> select(final Class<DTO> dtoClass, final LitebridgeContext litebridgeContext) {
        final SelectNode selectNode = new SelectNode(null, dtoClass, null, null, null, null);
        return new DtoFromClauseTerminal<>(selectNode, selectEngineTerminal, litebridgeContext);
    }

    /**
     * Starts a SELECT query for the specified DTO class within a contextual DTO mapping.
     *
     * @param <DTO>             the DTO type
     * @param dtoClass          the DTO class to query
     * @param contextDtoClass   the context DTO class
     * @param litebridgeContext the Litebridge context
     * @return the terminal FROM clause step
     */
    public <DTO> DtoFromClauseTerminal<DTO> select(final Class<DTO> dtoClass, final Class<?> contextDtoClass, final LitebridgeContext litebridgeContext) {
        final SelectNode selectNode = new SelectNode(null, dtoClass, contextDtoClass, null, null, null);
        return new DtoFromClauseTerminal<>(selectNode, selectEngineTerminal, litebridgeContext);
    }

    /**
     * Starts a SELECT query selecting the specified fields or columns.
     *
     * @param fieldsOrColumns          the field or column names to select
     * @param litebridgeContextCreator the factory function for creating the Litebridge context
     * @return the initial FROM clause step
     */
    public FromClauseStart select(final String[] fieldsOrColumns, final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator) {
        return new FromClauseStart(fieldsOrColumns, selectEngineTerminal, litebridgeContextCreator);
    }

    /**
     * Starts a SELECT query selecting the specified expressions.
     *
     * @param expressions              the query expressions to select
     * @param litebridgeContextCreator the factory function for creating the Litebridge context
     * @return the initial FROM clause step
     */
    public FromClauseStart select(final ExpressionSpec[] expressions, final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator) {
        return new FromClauseStart(expressions, selectEngineTerminal, litebridgeContextCreator);
    }

    /**
     * Starts a SELECT query with a return type override expression.
     *
     * @param <T>                      the overridden return type
     * @param expression               the type override expression
     * @param litebridgeContextCreator the factory function for creating the Litebridge context
     * @return the initial FROM clause step with type override
     */
    public <T> FromClauseStartTypeOverride<T> select(final TypeOverride<T> expression, final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator) {
        final ExpressionSpec[] expressionSpecs = switch (expression) {
            case TypeOverrideExpressionSpec<T> typeOverride -> new ExpressionSpec[]{typeOverride};
            case ConvertIntent<T> convertIntent -> convertIntent.target();
        };

        return new FromClauseStartTypeOverride<>(expression.returnType(), expressionSpecs, selectEngineTerminal, litebridgeContextCreator);
    }

    /**
     * Starts a SELECT query selecting all fields/columns.
     *
     * @param litebridgeContextCreator the factory function for creating the Litebridge context
     * @return the initial FROM clause step
     */
    public FromClauseStart select(final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator) {
        return new FromClauseStart(selectEngineTerminal, litebridgeContextCreator);
    }
}

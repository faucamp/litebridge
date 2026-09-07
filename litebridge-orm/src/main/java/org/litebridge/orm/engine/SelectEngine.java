package org.litebridge.orm.engine;

import org.litebridge.orm.api.dto.DtoFromClauseTerminal;
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
 * Initiates the fluent API for creating SELECT statements.
 */
public class SelectEngine {

    private final SelectEngineTerminal selectEngineTerminal;

    public SelectEngine(final DtoConstructor dtoConstructor) {
        this.selectEngineTerminal = new SelectEngineTerminal(dtoConstructor);
    }

    public <DTO> DtoFromClauseTerminal<DTO> select(final Class<DTO> dtoClass, final LitebridgeContext litebridgeContext) {
        final SelectNode selectNode = new SelectNode(null, dtoClass, null, null, null, null);
        return new DtoFromClauseTerminal<>(selectNode, selectEngineTerminal, litebridgeContext);
    }

    public <DTO> DtoFromClauseTerminal<DTO> select(final Class<DTO> dtoClass, final Class<?> contextDtoClass, final LitebridgeContext litebridgeContext) {
        final SelectNode selectNode = new SelectNode(null, dtoClass, contextDtoClass, null, null, null);
        return new DtoFromClauseTerminal<>(selectNode, selectEngineTerminal, litebridgeContext);
    }

    public FromClauseStart select(final String[] fieldsOrColumns, final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator) {
        return new FromClauseStart(fieldsOrColumns, selectEngineTerminal, litebridgeContextCreator);
    }

    public FromClauseStart select(final ExpressionSpec[] expressions, final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator) {
        return new FromClauseStart(expressions, selectEngineTerminal, litebridgeContextCreator);
    }

    public <T> FromClauseStartTypeOverride<T> select(final TypeOverride<T> expression, final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator) {
        final ExpressionSpec[] expressionSpecs = switch (expression) {
            case TypeOverrideExpressionSpec<T> typeOverride -> new ExpressionSpec[]{typeOverride};
            case ConvertIntent<T> convertIntent -> convertIntent.target();
        };

        return new FromClauseStartTypeOverride<>(expression.returnType(), expressionSpecs, selectEngineTerminal, litebridgeContextCreator);
    }

    public FromClauseStart select(final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator) {
        return new FromClauseStart(selectEngineTerminal, litebridgeContextCreator);
    }
}

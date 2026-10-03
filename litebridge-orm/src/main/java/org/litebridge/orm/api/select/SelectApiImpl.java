package org.litebridge.orm.api.select;

import org.litebridge.orm.api.select.dto.DtoFromClauseTerminal;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngine;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.TypeOverride;

/**
 * Implementation of {@link SelectApi} providing entry points for SELECT queries.
 */
public final class SelectApiImpl implements SelectApi {

    private final SelectEngine selectEngine;
    private final LitebridgeContext litebridgeContext;

    /**
     * Creates a new {@code SelectApiImpl} instance.
     *
     * @param litebridgeContext the Litebridge context
     */
    public SelectApiImpl(final LitebridgeContext litebridgeContext) {
        this.selectEngine = litebridgeContext.selectEngine();
        this.litebridgeContext = litebridgeContext;
    }

    @Override
    public <DTO> DtoFromClauseTerminal<DTO> select(final Class<DTO> dtoClass) {
        return selectEngine.select(dtoClass, litebridgeContext);
    }

    @Override
    public <DTO> DtoFromClauseTerminal<DTO> select(final Class<DTO> dtoClass, final RelatedDtoStrategy relatedDtoStrategy) {
        RelatedDtoStrategy prevStrategy = litebridgeContext.getRelatedDtoStrategy();

        if (relatedDtoStrategy != prevStrategy) {
            // The subselect uses a different related DTO strategy; override the context
            litebridgeContext.setRelatedDtoStrategy(relatedDtoStrategy);
        } else {
            prevStrategy = null;
        }

        final DtoFromClauseTerminal<DTO> result = selectEngine.select(dtoClass, litebridgeContext);

        // Restore the original related DTO strategy
        if (prevStrategy != null) {
            litebridgeContext.setRelatedDtoStrategy(prevStrategy);
        }

        return result;
    }

    @Override
    public <DTO> DtoFromClauseTerminal<DTO> select(final Class<DTO> dtoClass, final Class<?> contextDtoClass) {
        return selectEngine.select(dtoClass, contextDtoClass, litebridgeContext);
    }

    @Override
    public FromClauseStart select(final String... fieldsOrColumns) {
        return selectEngine.select(fieldsOrColumns, mode -> litebridgeContext);
    }

    @Override
    public FromClauseStart select(final ExpressionSpec... expressions) {
        return selectEngine.select(expressions, mode -> litebridgeContext);
    }

    @Override
    public <T> FromClauseStartTypeOverride<T> select(final TypeOverride<T> expression) {
        return selectEngine.select(expression, mode -> litebridgeContext);
    }

    @Override
    public FromClauseStart select() {
        return selectEngine.select(mode -> litebridgeContext);
    }
}

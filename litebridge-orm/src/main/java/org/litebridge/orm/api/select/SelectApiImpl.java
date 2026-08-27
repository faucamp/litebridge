package org.litebridge.orm.api.select;

import org.litebridge.orm.api.dto.DtoFromClauseTerminal;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngine;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.TypeOverride;

public class SelectApiImpl implements SelectApi {

    private final SelectEngine selectEngine;
    private final LitebridgeContext litebridgeContext;

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
        //TODO: check if related DTO strategy is different
        return selectEngine.select(dtoClass, litebridgeContext);
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

package org.litebridge.orm.api.select;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.dto.DtoFromClauseTerminal;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.expression.ExpressionSpec;

public final class FromClauseStart extends AbstractFromClauseStart {

    public FromClauseStart(final FromClauseEngine fromClauseEngine) {
        super(fromClauseEngine);
    }

    public FromClauseStart(final ExpressionSpec[] expressionSpecs, final FromClauseEngine fromClauseEngine) {
        super(expressionSpecs, fromClauseEngine);
    }

    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass) {
        return fromClauseEngine.from(expressionSpecs, dtoClass, (RelatedDtoStrategy) null);
    }

    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass, final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        return fromClauseEngine.from(expressionSpecs, dtoClass, relatedDtoStrategy);
    }

    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass, final Class<?> contextDtoClass) {
        return fromClauseEngine.from(dtoClass, contextDtoClass);
    }
}
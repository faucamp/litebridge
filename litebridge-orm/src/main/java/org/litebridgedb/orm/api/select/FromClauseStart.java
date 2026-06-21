package org.litebridgedb.orm.api.select;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.orm.api.dto.DtoFromClauseTerminal;
import org.litebridgedb.orm.config.RelatedDtoStrategy;
import org.litebridgedb.orm.engine.FromClauseEngine;
import org.litebridgedb.orm.expression.ExpressionSpec;

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
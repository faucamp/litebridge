package org.litebridgedb.orm.api.select;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.orm.api.dto.DtoFromClauseTerminal;
import org.litebridgedb.orm.config.RelatedDtoStrategy;
import org.litebridgedb.orm.engine.FromClauseEngine;
import org.litebridgedb.orm.expression.ExpressionSpec;

public final class FromClauseStartTypeOverride<TypeOverride> extends AbstractFromClauseStart {

    private final Class<TypeOverride> typeOverride;

    public FromClauseStartTypeOverride(final Class<TypeOverride> typeOverride, FromClauseEngine fromClauseEngine) {
        this(typeOverride, new ExpressionSpec[0], fromClauseEngine);
    }

    public FromClauseStartTypeOverride(final Class<TypeOverride> typeOverride,
                                       final ExpressionSpec[] expressionSpecs,
                                       final FromClauseEngine fromClauseEngine) {
        super(expressionSpecs, fromClauseEngine);
        this.typeOverride = typeOverride;
    }

    public DtoFromClauseTerminal<TypeOverride> from(final Class<?> dtoClass) {
        return fromClauseEngine.from(expressionSpecs, dtoClass, typeOverride, (RelatedDtoStrategy) null);
    }

    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass, final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        return fromClauseEngine.from(expressionSpecs, dtoClass, relatedDtoStrategy);
    }

    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass, final Class<?> contextDtoClass) {
        return fromClauseEngine.from(dtoClass, contextDtoClass);
    }
}

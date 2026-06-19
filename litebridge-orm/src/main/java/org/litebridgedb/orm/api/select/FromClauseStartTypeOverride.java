package org.litebridgedb.orm.api.select;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.orm.api.dto.DtoFromClauseTerminal;
import org.litebridgedb.orm.config.RelatedDtoStrategy;
import org.litebridgedb.orm.engine.FromClauseEngine;
import org.litebridgedb.orm.expression.Expression;

public final class FromClauseStartTypeOverride<TypeOverride> extends AbstractFromClauseStart {

    private final Class<TypeOverride> typeOverride;

    public FromClauseStartTypeOverride(final Class<TypeOverride> typeOverride, FromClauseEngine fromClauseEngine) {
        this(typeOverride, new Expression[0], fromClauseEngine);
    }

    public FromClauseStartTypeOverride(final Class<TypeOverride> typeOverride,
                                       final Expression[] expressions,
                                       final FromClauseEngine fromClauseEngine) {
        super(expressions, fromClauseEngine);
        this.typeOverride = typeOverride;
    }

    public DtoFromClauseTerminal<TypeOverride> from(final Class<?> dtoClass) {
        return fromClauseEngine.from(expressions, dtoClass, typeOverride, (RelatedDtoStrategy) null);
    }

    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass, final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        return fromClauseEngine.from(expressions, dtoClass, relatedDtoStrategy);
    }

    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass, final Class<?> contextDtoClass) {
        return fromClauseEngine.from(dtoClass, contextDtoClass);
    }
}

package org.litebridgedb.orm.api.select;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.orm.api.dto.DtoFromClauseTerminal;
import org.litebridgedb.orm.api.sql.SqlFromClauseTerminal;
import org.litebridgedb.orm.config.RelatedDtoStrategy;
import org.litebridgedb.orm.engine.FromClauseEngine;
import org.litebridgedb.orm.function.Expression;

public class FromClauseStart {

    private final Expression[] expressions;
    private final FromClauseEngine fromClauseEngine;

    public FromClauseStart(FromClauseEngine fromClauseEngine) {
        this(new Expression[0], fromClauseEngine);
    }

    public FromClauseStart(final Expression[] expressions,
                           final FromClauseEngine fromClauseEngine) {
        this.expressions = expressions;
        this.fromClauseEngine = fromClauseEngine;
    }

    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass) {
        return fromClauseEngine.from(expressions, dtoClass, (RelatedDtoStrategy) null);
    }

    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass, final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        return fromClauseEngine.from(expressions, dtoClass, relatedDtoStrategy);
    }

    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass, final Class<?> contextDtoClass) {
        return fromClauseEngine.from(dtoClass, contextDtoClass);
    }

    public SqlFromClauseTerminal from(final String table) {
        return fromClauseEngine.from(expressions, table);
    }
}

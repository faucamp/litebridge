package org.litebridgedb.orm.api.select;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Aliased;
import org.litebridgedb.orm.api.dto.DtoFromClauseTerminal;
import org.litebridgedb.orm.api.sql.SqlFromClauseTerminal;
import org.litebridgedb.orm.config.RelatedDtoStrategy;
import org.litebridgedb.orm.engine.FromClauseEngine;

public class FromClauseStart {

    private final Aliased[] fieldsOrColumns;
    private final FromClauseEngine fromClauseEngine;

    public FromClauseStart(FromClauseEngine fromClauseEngine) {
        this(FromClauseEngine.ALL_COLUMNS, fromClauseEngine);
    }

    public FromClauseStart(final Aliased[] fieldsOrColumns,
                           final FromClauseEngine fromClauseEngine) {
        this.fieldsOrColumns = fieldsOrColumns;
        this.fromClauseEngine = fromClauseEngine;
    }

    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass) {
        return fromClauseEngine.from(fieldsOrColumns, dtoClass, (RelatedDtoStrategy) null);
    }

    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass, final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        return fromClauseEngine.from(fieldsOrColumns, dtoClass, relatedDtoStrategy);
    }

    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass, final Class<?> contextDtoClass) {
        return fromClauseEngine.from(dtoClass, contextDtoClass);
    }

    public SqlFromClauseTerminal from(final String table) {
        return fromClauseEngine.from(fieldsOrColumns, table);
    }
}

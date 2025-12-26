package org.litebridge.orm.api.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Aliased;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.persistence.DtoMapper;
import org.litebridge.orm.persistence.TableRegistry;

import java.util.Arrays;
import java.util.LinkedHashMap;

public final class SqlSelector extends AbstractSelector<LinkedHashMap<String, Object>> {

    private final TableRegistry tableRegistry;

    public SqlSelector(final DatabaseProvider databaseProvider,
                       final TableRegistry tableRegistry) {
        super(new SelectSpec(), databaseProvider, new NoOpDtoMapper());
        this.tableRegistry = tableRegistry;
    }

    public SqlFromClause select(final String... columns) {
        return select(Arrays.stream(columns).map(Aliased::new).toArray(Aliased[]::new));
    }

    public SqlFromClause select(final Aliased... columns) {
        return new SqlFromClause(columns, selectSpec, tableRegistry, this);
    }

    private static final class NoOpDtoMapper implements DtoMapper<LinkedHashMap<String, Object>> {
        @Override
        public @Nullable LinkedHashMap<String, Object> toDto(final @Nullable LinkedHashMap<String, Object> row) {
            return row;
        }
    }
}

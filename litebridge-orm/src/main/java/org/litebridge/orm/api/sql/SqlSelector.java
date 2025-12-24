package org.litebridge.orm.api.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.query.SelectField;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.persistence.DtoMapper;
import org.litebridge.orm.persistence.TableRegistry;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class SqlSelector extends AbstractSelector<LinkedHashMap<String, Object>> {

    private final TableRegistry tableRegistry;

    public SqlSelector(final DatabaseProvider databaseProvider,
                       final TableRegistry tableRegistry) {
        super(new SelectSpec(), databaseProvider, new NoOpDtoMapper());
        this.tableRegistry = tableRegistry;
    }

    public SqlFromClause select(final String... columns) {
        selectSpec.setColumns(Arrays.stream(columns).map(SelectField::new).toList());
        return new SqlFromClause(selectSpec, tableRegistry, this);
    }

    public SqlFromClause select(final SelectField... columns) {
        selectSpec.setColumns(List.of(columns));
        return new SqlFromClause(selectSpec, tableRegistry, this);
    }

    private static class NoOpDtoMapper implements DtoMapper<LinkedHashMap<String, Object>> {
        @Override
        public @Nullable LinkedHashMap<String, Object> toDto(final @Nullable LinkedHashMap<String, Object> row) {
            return row;
        }
    }
}

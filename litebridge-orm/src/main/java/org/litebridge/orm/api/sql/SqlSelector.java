package org.litebridge.orm.api.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.query.SelectField;
import org.litebridge.orm.api.select.DtoMappingSelectTerminal;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.persistence.DefaultDtoMapper;
import org.litebridge.orm.persistence.DtoMapper;
import org.litebridge.orm.persistence.Table;
import org.litebridge.orm.persistence.TableRegistry;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SqlSelector extends AbstractSelector<Map<String, Object>>
        implements DtoMappingSelectTerminal<Map<String, Object>> {

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

    @Override
    public <T> T toDto(final Map<String, Object> result, final Class<T> dtoClass) {
        final Table table = ObjectUtils.requireNonNull(tableRegistry.getTable(dtoClass), "DTO class not registered: " + dtoClass.getName());
        return DefaultDtoMapper.toDto(result, dtoClass, table, databaseProvider.getTypeConverter());
    }

    private static class NoOpDtoMapper implements DtoMapper<Map<String, Object>> {
        @Override
        public @Nullable Map<String, Object> toDto(final @Nullable Map<String, Object> row) {
            return row;
        }
    }
}

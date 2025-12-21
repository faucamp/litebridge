package org.litebridge.orm.api.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.query.SelectField;
import org.litebridge.orm.api.select.FromClause;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.persistence.TableRegistry;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SqlSelector extends AbstractSelector<Map<String, Object>> {

    private final TableRegistry tableRegistry;

    public SqlSelector(final DatabaseProvider databaseProvider, final TableRegistry tableRegistry) {
        super(databaseProvider);
        this.tableRegistry = tableRegistry;
    }

    public FromClause<Map<String, Object>> select(final String... columns) {
        selectSpec.setColumns(Arrays.stream(columns).map(SelectField::new).toList());
        return new SqlFromClause(selectSpec, tableRegistry, databaseProvider, this);
    }

    public FromClause<Map<String, Object>> select(final SelectField... columns) {
        selectSpec.setColumns(List.of(columns));
        return new SqlFromClause(selectSpec, tableRegistry, databaseProvider, this);
    }

    @Override
    protected final @Nullable Map<String, Object> toDto(final @Nullable Map<String, Object> row) {
        return row;
    }
}

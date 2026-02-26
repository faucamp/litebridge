package org.litebridge.orm.api.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.Aliased;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.persistence.TableRegistry;

import java.util.Arrays;
import java.util.List;

public final class SqlSelector extends AbstractSelector<Row, SqlSelectSpec> {

    private final TableRegistry tableRegistry;

    public SqlSelector(final DatabaseProvider databaseProvider,
                       final TableRegistry tableRegistry) {
        super(new SqlSelectSpec(), databaseProvider, Row.class);
        this.tableRegistry = tableRegistry;
    }

    public SqlFromClause select(final String... columns) {
        return select(Arrays.stream(columns).map(Aliased::new).toArray(Aliased[]::new));
    }

    public SqlFromClause select(final Aliased... columns) {
        return new SqlFromClause(columns, selectSpec, tableRegistry, this);
    }

    @Override
    public @Nullable Row oneOrNull() {
        return fetchOneRecord(false);
    }

    @Override
    public @Nullable Row firstOrNull() {
        return fetchOneRecord(true);
    }

    @Override
    public List<Row> list() {
        return executeQuery();
    }

    private @Nullable Row fetchOneRecord(final boolean first) {
        if (first) {
            // Set LIMIT since we are only interested in the first record
            selectSpec.ensureLimit().setLimit(1);
        }

        final List<Row> resultList = executeQuery();

        if (CollectionUtils.isEmpty(resultList)) {
            return null;
        }

        if (!first && resultList.size() > 1) {
            throw new IllegalStateException("Expected exactly one result, but got %d".formatted(resultList.size()));
        }

        return resultList.getFirst();
    }
}

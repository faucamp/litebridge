package org.litebridgedb.orm.api.sql;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.CollectionUtils;
import org.litebridgedb.db.spi.Aliased;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.orm.api.select.impl.AbstractSelector;
import org.litebridgedb.orm.config.LitebridgeConfig;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;

import java.util.Arrays;
import java.util.List;

public final class SqlSelector extends AbstractSelector<Row, SqlSelectSpec> {

    private final TableRegistry tableRegistry;

    public SqlSelector(final TransactionalDatabaseProvider databaseProvider,
                       final TableRegistry tableRegistry,
                       final LitebridgeConfig litebridgeConfig) {
        super(new SqlSelectSpec(databaseProvider.getSqlFunctionRegistry()), databaseProvider, Row.class, litebridgeConfig);
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

package org.litebridge.orm.api.select.impl;

import org.litebridge.db.api.DatabaseProvider;

import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Stream;

public abstract class AbstractSelector<DTO> extends AbstractSelectTerminal<DTO> {

    protected final DatabaseProvider databaseProvider;

    public AbstractSelector(final DatabaseProvider databaseProvider) {
        super(new SelectSpec());
        this.databaseProvider = databaseProvider;
    }

    @Override
    protected Stream<Map<String, Object>> executeQuery() {
        // Execute SQL query
        final Stream<Map<String, Object>> resultList;

        try {
            resultList = databaseProvider.select(selectSpec.toSelect());
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to execute select query", ex);
        }

        return resultList;
    }
}

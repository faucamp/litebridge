package org.litebridge.orm.persistence;

import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.orm.api.select.model.SelectSpec;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SelectFacade {

    private final DatabaseProvider databaseProvider;

    public SelectFacade(final DatabaseProvider databaseProvider) {
        this.databaseProvider = databaseProvider;
    }

    protected List<Map<String, Object>> select(final SelectSpec selectSpec) {
        // Execute SQL query
        final List<Map<String, Object>> resultList;

        try {
            resultList = databaseProvider.select(selectSpec.toSelect());
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to execute SELECT query", ex);
        }

        return resultList;
    }
}

package org.litebridge.orm.sql;

import org.litebridge.db.api.DatabaseProvider;
import org.litebridge.db.api.TableMetaData;
import org.litebridge.orm.persistence.AbstractSelector;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class SqlSelector extends AbstractSelector<Map<String, Object>> {

    public SqlSelector(final List<String> columns, final TableMetaData tableMetaData, final DatabaseProvider databaseProvider) {
        super(columns, tableMetaData, databaseProvider);
    }

    @Override
    protected Map<String, Object> get() {
        return super.getRecord();
    }

    @Override
    public List<Map<String, Object>> getAll() {
        return super.getAllRecords();
    }

    @Override
    public Stream<Map<String, Object>> stream() {
        return super.streamRecords();
    }
}
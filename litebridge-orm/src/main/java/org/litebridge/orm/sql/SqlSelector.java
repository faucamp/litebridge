package org.litebridge.orm.sql;

import jakarta.annotation.Nullable;
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
    public @Nullable Map<String, Object> oneOrNull() {
        return super.getOneRecord(false);
    }

    @Override
    public @Nullable Map<String, Object> firstOrNull() {
        return super.getOneRecord(true);
    }

    @Override
    public List<Map<String, Object>> list() {
        return super.getAllRecords();
    }

    @Override
    public Stream<Map<String, Object>> stream() {
        return super.streamRecords();
    }
}
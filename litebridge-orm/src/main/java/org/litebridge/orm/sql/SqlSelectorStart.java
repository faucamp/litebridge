package org.litebridge.orm.sql;

import org.litebridge.db.api.DatabaseProvider;
import org.litebridge.db.api.TableMetaData;
import org.litebridge.orm.Table;
import org.litebridge.orm.TableRegistry;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class SqlSelectorStart {

    private final List<String> columns;
    private final TableRegistry tableRegistry;
    private final DatabaseProvider databaseProvider;

    public SqlSelectorStart(final List<String> columns, final TableRegistry tableRegistry, final DatabaseProvider databaseProvider) {
        this.columns = columns;
        this.tableRegistry = tableRegistry;
        this.databaseProvider = databaseProvider;
    }

    public SqlSelector from(final String table) {
        return from("", table);
    }

    public SqlSelector from(final String schema, final String table) {
        final TableMetaData tableMetaData = getTableMetaData(schema, table);
        return new SqlSelector(columns, tableMetaData, tableRegistry, databaseProvider);
    }

    private TableMetaData getTableMetaData(final String schema, final String table) {
        // If the table has been registered for DTO mapping, use the corresponding Table object, else use the table name directly
        final TableMetaData tableMetaData;
        final Table tableImpl = tableRegistry.getTable(table);

        if (tableImpl != null && Objects.equals(schema, tableImpl.getMetaData().getSchema())) {
            tableMetaData = tableImpl.getMetaData();
        } else {
            tableMetaData = new TableMetaData("", schema, table, Collections.emptyList(), Collections.emptyList());
        }

        return tableMetaData;
    }
}

package org.litebridge.orm.api.sql;

import org.litebridge.db.api.DatabaseProvider;
import org.litebridge.db.api.TableMetaData;
import org.litebridge.orm.api.select.FromClause;
import org.litebridge.orm.api.select.FromClauseTerminal;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.impl.SelectSpec;
import org.litebridge.orm.persistence.Table;
import org.litebridge.orm.persistence.TableRegistry;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class SqlFromClause implements FromClause<Map<String, Object>> {

    private final SelectSpec selectSpec;
    private final TableRegistry tableRegistry;
    private final DatabaseProvider databaseProvider;
    private final SelectTerminal<Map<String, Object>> selectTerminal;

    public SqlFromClause(final SelectSpec selectSpec,
                         final TableRegistry tableRegistry,
                         final DatabaseProvider databaseProvider,
                         final SelectTerminal<Map<String, Object>> selectTerminal) {
        this.selectSpec = selectSpec;
        this.tableRegistry = tableRegistry;
        this.databaseProvider = databaseProvider;
        this.selectTerminal = selectTerminal;
    }

    public FromClauseTerminal<Map<String, Object>> from(final String schema, final String table) {
        final TableMetaData tableMetaData = getTableMetaData(schema, table);
        selectSpec.setTable(tableMetaData);
        return new SqlFromClauseTerminal(selectSpec, selectTerminal);
    }

    @Override
    public FromClauseTerminal<Map<String, Object>> from(final String table) {
        return from("", table);
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

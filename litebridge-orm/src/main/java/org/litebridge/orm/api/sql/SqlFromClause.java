package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.api.select.FromClause;
import org.litebridge.orm.api.select.impl.FromClauseTerminalImpl;
import org.litebridge.orm.api.select.impl.JoinClauseImpl;
import org.litebridge.orm.api.select.impl.JoinConditionClauseImpl;
import org.litebridge.orm.api.select.impl.JoinConditionClauseTerminalImpl;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.persistence.Table;
import org.litebridge.orm.persistence.TableRegistry;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Objects;

public class SqlFromClause implements FromClause<LinkedHashMap<String, Object>,
        JoinClauseImpl<LinkedHashMap<String, Object>>,
        JoinConditionClauseImpl<LinkedHashMap<String, Object>, JoinConditionClauseTerminalImpl<LinkedHashMap<String, Object>>>,
        JoinConditionClauseTerminalImpl<LinkedHashMap<String, Object>>,
        SqlFromClauseTerminal> {

    private final SelectSpec selectSpec;
    private final TableRegistry tableRegistry;
    private final SqlSelector delegate;

    public SqlFromClause(final SelectSpec selectSpec,
                         final TableRegistry tableRegistry,
                         final SqlSelector delegate) {
        this.selectSpec = selectSpec;
        this.tableRegistry = tableRegistry;
        this.delegate = delegate;
    }

    @Override
    public SqlFromClauseTerminal from(final String schema, final String table) {
        final TableMetaData tableMetaData = getTableMetaData(schema, table);
        selectSpec.setTable(tableMetaData);
        return new SqlFromClauseTerminal(delegate);
    }

    @Override
    public SqlFromClauseTerminal from(final String table) {
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

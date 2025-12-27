package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Aliased;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.FromClause;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.persistence.TableRegistry;

import java.util.Arrays;

public final class SqlFromClause implements FromClause<Row,
        SqlFromClauseTerminal,
        SqlJoinClause,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    private final Aliased[] columns;
    private final SelectSpec selectSpec;
    private final TableRegistry tableRegistry;
    private final SqlSelector delegate;

    public SqlFromClause(final Aliased[] columns,
                         final SelectSpec selectSpec,
                         final TableRegistry tableRegistry,
                         final SqlSelector delegate) {
        this.columns = columns;
        this.selectSpec = selectSpec;
        this.tableRegistry = tableRegistry;
        this.delegate = delegate;
    }

    @Override
    public SqlFromClauseTerminal from(final String schema, final String table) {
        final Table spiTable = tableRegistry.getOrCreateSpiTable(schema, table);
        selectSpec.setTable(spiTable);
        selectSpec.setColumns(Arrays.stream(columns)
                .map(aliased -> {
                    if (aliased instanceof Column column) {
                        return column;
                    } else {
                        return new Column(spiTable, aliased.name(), aliased.alias());
                    }
                })
                .toList());
        return new SqlFromClauseTerminal(delegate);
    }
}

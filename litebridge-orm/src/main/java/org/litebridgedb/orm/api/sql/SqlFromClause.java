package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Aliased;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.api.select.FromClause;
import org.litebridgedb.orm.persistence.TableRegistry;

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
    private final SqlSelectSpec selectSpec;
    private final TableRegistry tableRegistry;
    private final SqlSelector delegate;

    public SqlFromClause(final Aliased[] columns,
                         final SqlSelectSpec selectSpec,
                         final TableRegistry tableRegistry,
                         final SqlSelector delegate) {
        this.columns = columns;
        this.selectSpec = selectSpec;
        this.tableRegistry = tableRegistry;
        this.delegate = delegate;
    }

    @Override
    public SqlFromClauseTerminal from(final String table) {
        final Table spiTable = tableRegistry.getOrCreateSpiTable(table);
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

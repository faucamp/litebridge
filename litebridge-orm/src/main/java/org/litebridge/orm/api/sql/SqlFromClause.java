package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.FromClause;
import org.litebridge.orm.persistence.TableRegistry;

public final class SqlFromClause implements FromClause<Row,
        SqlFromClauseTerminal,
        SqlJoinClause,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal,
        SqlGroupByClauseTerminal,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    private final SqlSelectSpec selectSpec;
    private final TableRegistry tableRegistry;
    private final SqlSelector delegate;

    public SqlFromClause(final SqlSelectSpec selectSpec,
                         final TableRegistry tableRegistry,
                         final SqlSelector delegate) {
        this.selectSpec = selectSpec;
        this.tableRegistry = tableRegistry;
        this.delegate = delegate;
    }

    @Override
    public SqlFromClauseTerminal from(final String table) {
        final Table spiTable = tableRegistry.getOrCreateSpiTable(table);
        selectSpec.setTable(spiTable);
        selectSpec.setProtoExpressionResolver(new SqlProtoExpressionResolver(selectSpec));
        return new SqlFromClauseTerminal(delegate);
    }
}

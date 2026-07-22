package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.FromClause;
import org.litebridge.orm.api.select.ast.FromNode;
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

    private final TableRegistry tableRegistry;
    private final SqlSelector delegate;

    public SqlFromClause(final TableRegistry tableRegistry,
                         final SqlSelector delegate) {
        this.tableRegistry = tableRegistry;
        this.delegate = delegate;
    }

    @Override
    public SqlFromClauseTerminal from(final String table) {
        final Table spiTable = tableRegistry.getOrCreateSpiTable(table);
        final SqlSelector newDelegate = (SqlSelector) delegate.withNode(new FromNode(delegate.node(), null, null, table, null));
        newDelegate.selectSpec().setTable(spiTable);
        return new SqlFromClauseTerminal(newDelegate);
    }
}

package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.impl.AbstractFromClauseTerminal;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.api.spec.FieldColumnSpec;

import java.util.Arrays;

public final class SqlFromClauseTerminal extends AbstractFromClauseTerminal<Row,
        SqlJoinClause,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain,
        SelectSpec>

        implements SqlJoinClauseTerminal {

    public SqlFromClauseTerminal(final SqlSelector delegate) {
        super(delegate);
    }

    @Override
    public SqlJoinClause join(final String schema, final String table) {
        return new SqlJoinClause(selectSpec.newJoinSpec(schema, table), delegate);
    }

    @Override
    public SqlWhereConditionClause where(final String column) {
        final Column spiColumn = new Column(selectSpec.getTable(), column);
        return new SqlWhereConditionClause(selectSpec.newWhereCondition(spiColumn), new SqlWhereConditionClauseTerminal((SqlSelector) delegate));
    }

    @Override
    public SqlOrderByClause orderBy(final String... columns) {
        return new SqlOrderByClause(selectSpec.newOrderBy(columns), (SqlSelector) delegate);
    }

    @Override
    public SqlOrderByClause orderBy(final FieldColumnSpec... columns) {
        return new SqlOrderByClause(selectSpec.newOrderBy(Arrays.stream(columns)
                .map(fieldColumnSpec -> fieldColumnSpec.columnSpec().name())
                .toArray(String[]::new)),
                (SqlSelector) delegate);
    }
}

package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.impl.AbstractJoinConditionClauseTerminal;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.api.select.model.JoinSpec;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.api.spec.ColumnSpec;
import org.litebridge.orm.api.spec.FieldColumnSpec;

import java.util.Arrays;

public final class SqlJoinConditionClauseTerminal extends AbstractJoinConditionClauseTerminal<Row,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain,
        SelectSpec>

        implements SqlJoinClauseTerminal {

    public SqlJoinConditionClauseTerminal(final JoinSpec joinSpec, final AbstractSelector<Row, SelectSpec> delegate) {
        super(joinSpec, delegate);
    }

    @Override
    public SqlJoinConditionClause and(final String column) {
        final Column spiColumn = new Column(joinSpec.table(), column);
        return new SqlJoinConditionClause(joinSpec.newCondition(spiColumn), this);
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

    public SqlOrderByClause orderBy(final FieldColumnSpec... columns) {
        return new SqlOrderByClause(selectSpec.newOrderBy(Arrays.stream(columns)
                .map(fieldColumnSpec -> fieldColumnSpec.columnSpec().name())
                .toArray(String[]::new)),
                (SqlSelector) delegate);
    }
}

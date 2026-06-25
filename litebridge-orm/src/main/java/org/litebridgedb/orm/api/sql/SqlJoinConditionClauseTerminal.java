package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.orm.api.select.impl.AbstractJoinConditionClauseTerminal;
import org.litebridgedb.orm.api.select.impl.AbstractSelector;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;

import java.util.Arrays;

public final class SqlJoinConditionClauseTerminal extends AbstractJoinConditionClauseTerminal<Row,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal,
        SqlGroupByClauseTerminal,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain,
        SqlSelectSpec,
        SqlJoinSpec>

        implements SqlJoinClauseTerminal {

    public SqlJoinConditionClauseTerminal(final SqlJoinSpec joinSpec, final AbstractSelector<Row, SqlSelectSpec> delegate) {
        super(joinSpec, delegate);
    }

    @Override
    public SqlJoinConditionClause and(final String column) {
        final Column spiColumn = new Column(joinSpec.table(), column);
        return new SqlJoinConditionClause(joinSpec.newCondition(spiColumn), this, delegate.litebridgeContext());
    }

    @Override
    public SqlJoinConditionClause and(final FieldColumnSpec column) {
        return and(column.columnSpec().name());
    }

    @Override
    public SqlJoinClause join(final String table) {
        return new SqlJoinClause(selectSpec.newJoinSpec(table), delegate);
    }

    @Override
    public SqlWhereConditionClause where(final String column) {
        final Column spiColumn = new Column(selectSpec.getTable(), column);
        return new SqlWhereConditionClause(selectSpec.newWhereCondition(spiColumn), new SqlWhereConditionClauseTerminal((SqlSelector) delegate), delegate.litebridgeContext());
    }

    @Override
    public SqlWhereConditionClause where(final FieldColumnSpec column) {
        return where(column.columnSpec().name());
    }

    @Override
    public SqlGroupByClauseTerminal groupBy(final String... columns) {
        throw new UnsupportedOperationException("Not yet implemented");
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

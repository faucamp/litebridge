package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.orm.api.select.WhereConditionClauseTerminal;
import org.litebridgedb.orm.api.select.impl.AbstractWhereClauseTerminal;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;

import java.util.Arrays;

public final class SqlWhereConditionClauseTerminal
        extends AbstractWhereClauseTerminal<Row, SqlOrderByClause, SqlOrderByClauseChain, SqlSelectSpec>
        implements WhereConditionClauseTerminal<Row,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    public SqlWhereConditionClauseTerminal(final SqlSelector delegate) {
        super(delegate);
    }

    @Override
    public SqlWhereConditionClause and(final String column) {
        final Column spiColumn = new Column(selectSpec.getTable(), column);
        return new SqlWhereConditionClause(selectSpec.newWhereCondition(spiColumn), this, delegate.litebridgeContext());
    }

    @Override
    public SqlWhereConditionClause and(final FieldColumnSpec column) {
        return and(column.columnSpec().name());
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

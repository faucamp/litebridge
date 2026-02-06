package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.WhereConditionClauseTerminal;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.api.select.impl.AbstractWhereClauseTerminal;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.api.spec.FieldColumnSpec;

import java.util.Arrays;

public final class SqlWhereConditionClauseTerminal
        extends AbstractWhereClauseTerminal<Row, SqlOrderByClause, SqlOrderByClauseChain, SelectSpec>
        implements WhereConditionClauseTerminal<Row,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    public SqlWhereConditionClauseTerminal(final AbstractSelector<Row, SelectSpec> delegate) {
        super(delegate);
    }

    @Override
    public SqlWhereConditionClause and(final String column) {
        final Column spiColumn = new Column(selectSpec.getTable(), column);
        return new SqlWhereConditionClause(selectSpec.newWhereCondition(spiColumn), this);
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

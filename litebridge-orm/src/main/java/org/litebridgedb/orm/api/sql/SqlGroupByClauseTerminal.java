package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Row;
import org.litebridgedb.orm.api.select.impl.AbstractGroupByClauseTerminal;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.expression.ExpressionSpec;

import java.util.Arrays;

public class SqlGroupByClauseTerminal extends AbstractGroupByClauseTerminal<Row,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain,
        SqlSelectSpec> {

    public SqlGroupByClauseTerminal(final SqlSelector delegate) {
        super(delegate);
    }

    @Override
    public SqlHavingConditionClause having(final ExpressionSpec expression) {
        throw new UnsupportedOperationException("Not yet implemented");
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

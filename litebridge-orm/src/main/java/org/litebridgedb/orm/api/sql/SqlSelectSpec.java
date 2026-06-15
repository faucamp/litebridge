package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.function.SqlFunctionRegistry;
import org.litebridgedb.orm.api.select.model.SelectSpec;

import java.util.ArrayList;

/**
 * Specification for constructing a SQL SELECT statement.
 * <p>
 * This class encapsulates table, column, join, condition, order by,
 * and limit specifications for building a query.
 */
public class SqlSelectSpec extends SelectSpec {

    public SqlSelectSpec(final SqlFunctionRegistry sqlFunctionRegistry) {
        super(sqlFunctionRegistry);
    }

    public SqlJoinSpec newJoinSpec(final String table) {
        return newJoinSpec(new Table(table, null));
    }

    public SqlJoinSpec newJoinSpec(final Table table) {
        if (this.joins == null) {
            joins = new ArrayList<>();
        }

        final SqlJoinSpec joinSpec = new SqlJoinSpec(table);
        joins.add(joinSpec);
        return joinSpec;
    }
}

package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.api.select.model.SelectSpec;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.ProtoColumnExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Specification for constructing a SQL SELECT statement.
 * <p>
 * This class encapsulates table, lhs, join, condition, order by,
 * and limit specifications for building a query.
 */
public class SqlSelectSpec extends SelectSpec {

    public SqlSelectSpec(final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
    }

    public SqlJoinSpec newJoinSpec(final String table) {
        return newJoinSpec(new Table(table, null));
    }

    public SqlJoinSpec newJoinSpec(final Table table) {
        if (this.joins == null) {
            joins = new ArrayList<>();
        }

        final SqlJoinSpec joinSpec = new SqlJoinSpec(table, selectExpressionMapper);
        joins.add(joinSpec);
        return joinSpec;
    }

    public List<ExpressionSpec> createSelectColumnSpecs(final String[] columns) {
        return Arrays.stream(columns)
                .map(column -> (ExpressionSpec) new ProtoColumnExpressionSpec(SelectColumnSpec.class, column))
                .toList();
    }
}

package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.ProtoColumnExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Specification for constructing a SQL SELECT statement.
 * <p>
 * This class encapsulates table, column, join, condition, order by,
 * and limit specifications for building a query.
 */
@Deprecated(forRemoval = true)
public class SqlSelectSpec extends SelectSpec {

    public SqlSelectSpec(final LitebridgeContext litebridgeContext, final Table table) {
        super(litebridgeContext);
        this.table = table;
    }

    public SqlJoinSpec newJoinSpec(final String table) {
        return newJoinSpec(new Table(table, null));
    }

    public SqlJoinSpec newJoinSpec(final Table table) {
        if (this.joins == null) {
            joins = new ArrayList<>();
        }

        final SqlJoinSpec joinSpec = new SqlJoinSpec(table, Objects.requireNonNull(selectExpressionMapper));
        joins.add(joinSpec);
        return joinSpec;
    }

    //TODO: move to different class
    public static List<ExpressionSpec> createSelectColumnSpecs(final String[] columns) {
        return Arrays.stream(columns)
                .map(column -> (ExpressionSpec) new ProtoColumnExpressionSpec(SelectColumnSpec.class, column))
                .toList();
    }
}

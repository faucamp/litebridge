package org.litebridgedb.orm.api.sql;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.orm.api.select.impl.LitebridgeContext;
import org.litebridgedb.orm.api.select.model.SelectSpec;

import java.util.ArrayList;

/**
 * Specification for constructing a SQL SELECT statement.
 * <p>
 * This class encapsulates table, column, join, condition, order by,
 * and limit specifications for building a query.
 */
public class SqlSelectSpec extends SelectSpec {

    private @Nullable Class<?> valueTypeOverride;

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

    public @Nullable Class<?> getValueTypeOverride() {
        return valueTypeOverride;
    }

    public void setValueTypeOverride(@Nullable final Class<?> valueTypeOverride) {
        this.valueTypeOverride = valueTypeOverride;
    }
}

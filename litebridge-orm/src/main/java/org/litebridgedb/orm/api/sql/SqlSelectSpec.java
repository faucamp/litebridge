package org.litebridgedb.orm.api.sql;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.function.SqlFunctionRegistry;
import org.litebridgedb.db.spi.query.SelectExpression;
import org.litebridgedb.orm.api.select.model.SelectSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Specification for constructing a SQL SELECT statement.
 * <p>
 * This class encapsulates table, column, join, condition, order by,
 * and limit specifications for building a query.
 */
public class SqlSelectSpec extends SelectSpec {

    @Nullable
    protected List<SelectExpression> expressions;

    public SqlSelectSpec(final SqlFunctionRegistry sqlFunctionRegistry) {
        super(sqlFunctionRegistry);
    }

    public @Nullable List<SelectExpression> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<SelectExpression> expressions) {
        if (expressions instanceof ArrayList) {
            this.expressions = expressions;
        } else {
            this.expressions = new ArrayList<>(expressions);
        }
    }

    public void addExpressions(final List<SelectExpression> expressions) {
        if (this.expressions == null) {
            this.expressions = new ArrayList<>();
        } else if (!(this.expressions instanceof ArrayList)) {
            this.expressions = new ArrayList<>(this.expressions);
        }

        this.expressions.addAll(expressions);
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

    @Override
    protected List<SelectExpression> expressions() {
        return expressions != null ? expressions : Collections.emptyList();
    }

    private Column sanitise(final Column column) {
        if (table != null && column.table() != table
                && column.table().alias() == null
                && Objects.equals(column.table().schema(), table.schema())
                && Objects.equals(column.table().name(), table.name())) {
            return new Column(table, column.name(), column.alias());
        } else {
            return column;
        }
    }
}

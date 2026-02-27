package org.litebridge.orm.api.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.model.JoinSpec;
import org.litebridge.orm.api.select.model.SelectSpec;

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
    protected List<Column> columns;

    public @Nullable List<Column> getColumns() {
        return columns;
    }

    public void setColumns(final List<Column> columns) {
        this.columns = sanitise(columns);
    }

    public void addColumns(final List<Column> columns) {
        if (this.columns == null) {
            this.columns = new ArrayList<>();
        } else if (!(columns instanceof ArrayList)) {
            this.columns = new ArrayList<>(this.columns);
        }

        this.columns.addAll(sanitise((columns)));
    }

    public JoinSpec newJoinSpec(final Table table) {
        return newJoinSpec(table.schema(), table.name());
    }

    public JoinSpec newJoinSpec(final String table) {
        return newJoinSpec("", table);
    }

    public SqlJoinSpec newJoinSpec(final String schema, final String table) {
        if (this.joins == null) {
            joins = new ArrayList<>();
        }

        final SqlJoinSpec joinSpec = new SqlJoinSpec(schema, table);
        joins.add(joinSpec);
        return joinSpec;
    }

    @Override
    protected List<Column> columns() {
        return columns != null ? columns : Collections.emptyList();
    }

    private List<Column> sanitise(final List<Column> columns) {
        // Ensure just one instance of the same table is used
        return columns.stream()
                .map(this::sanitise)
                .toList();
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

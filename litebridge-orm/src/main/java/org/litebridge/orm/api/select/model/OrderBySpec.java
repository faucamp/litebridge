package org.litebridge.orm.api.select.model;

import org.litebridge.db.spi.query.OrderBy;

import java.util.Arrays;
import java.util.List;

public class OrderBySpec {

    private final String[] columns;
    private boolean asc = true;

    public OrderBySpec(final String... columns) {
        this.columns = columns;
    }

    public boolean isAsc() {
        return asc;
    }

    public void setAsc(final boolean asc) {
        this.asc = asc;
    }

    public List<OrderBy> toOrderBys() {
        return Arrays.stream(columns)
                .map(column -> new OrderBy(column, asc))
                .toList();
    }
}

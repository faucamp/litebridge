package org.litebridgedb.orm.api.select.model;

import org.litebridgedb.db.spi.query.OrderBy;

/**
 * Specification for an "ORDER BY" clause in a database query.
 * <p>
 * This class allows specifying the expressions to order by and the sort direction
 * (ascending or descending). It provides methods to retrieve the specified
 * expressions, check the ordering direction, and convert the specification into a
 * list of {@link OrderBy} objects.
 */
public class OrderBySpec {

    private final String[] columns;
    private boolean asc;

    public OrderBySpec(final String[] columns) {
        this(columns, true);
    }

    public OrderBySpec(final String[] columns, final boolean asc) {
        this.columns = columns;
        this.asc = asc;
    }

    public String[] columns() {
        return columns;
    }

    public boolean isAsc() {
        return asc;
    }

    public void setAsc(final boolean asc) {
        this.asc = asc;
    }
}

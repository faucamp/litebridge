package org.litebridgedb.orm.api.select.model;

/**
 * Specification for an "ORDER BY" clause in a database query.
 * <p>
 * This class allows specifying the expressions to order by and the sort direction
 * (ascending or descending).
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

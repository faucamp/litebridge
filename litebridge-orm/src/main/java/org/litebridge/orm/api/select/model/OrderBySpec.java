package org.litebridge.orm.api.select.model;

import org.litebridge.orm.expression.ExpressionSpec;

import java.util.List;

/**
 * Specification for an "ORDER BY" clause in a database query.
 * <p>
 * This class allows specifying the expressions to order by and the sort direction
 * (ascending or descending).
 */
public class OrderBySpec {

    private final List<? extends ExpressionSpec> expressions;
    private boolean asc;

    /**
     * Creates a new OrderBySpec with ascending order by default.
     *
     * @param expressions the expressions to order by
     */
    public OrderBySpec(final List<? extends ExpressionSpec> expressions) {
        this(expressions, true);
    }

    /**
     * Creates a new OrderBySpec.
     *
     * @param expressions the expressions to order by
     * @param asc whether to sort in ascending order
     */
    public OrderBySpec(final List<? extends ExpressionSpec> expressions, final boolean asc) {
        this.expressions = expressions;
        this.asc = asc;
    }

    /**
     * Returns the expressions to order by.
     *
     * @return the list of expression specifications
     */
    @SuppressWarnings("unchecked")
    public List<ExpressionSpec> expressions() {
        return (List<ExpressionSpec>) expressions;
    }

    /**
     * Returns whether the sort order is ascending.
     *
     * @return {@code true} if ascending, {@code false} if descending
     */
    public boolean isAsc() {
        return asc;
    }

    /**
     * Sets whether the sort order should be ascending.
     *
     * @param asc {@code true} for ascending, {@code false} for descending
     */
    public void setAsc(final boolean asc) {
        this.asc = asc;
    }
}

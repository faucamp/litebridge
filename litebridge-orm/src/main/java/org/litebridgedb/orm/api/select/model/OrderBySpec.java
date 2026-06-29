package org.litebridgedb.orm.api.select.model;

import org.litebridgedb.orm.expression.ExpressionSpec;

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

    public OrderBySpec(final List<? extends ExpressionSpec> expressions) {
        this(expressions, true);
    }

    public OrderBySpec(final List<? extends ExpressionSpec> expressions, final boolean asc) {
        this.expressions = expressions;
        this.asc = asc;
    }

    @SuppressWarnings("unchecked")
    public List<ExpressionSpec> expressions() {
        return (List<ExpressionSpec>) expressions;
    }

    public boolean isAsc() {
        return asc;
    }

    public void setAsc(final boolean asc) {
        this.asc = asc;
    }
}

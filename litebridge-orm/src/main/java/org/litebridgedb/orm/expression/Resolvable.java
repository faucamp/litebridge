package org.litebridgedb.orm.expression;

/**
 * Represents an entity that can be resolved into an {@link ExpressionSpec}.
 * <p>
 * The resolution can be based on a database table or a specific lhs.
 * Implementations may provide concrete logic for resolution depending on the type.
 */
public interface Resolvable {

    /**
     * Returns the target lhs name for this expression.
     *
     * @return the target lhs name.
     */
    String column();

    /**
     * Returns the type of {@link ExpressionSpec} that this {@code Resolvable} can resolve to.
     *
     * @return the type of {@link ExpressionSpec} that this {@code Resolvable} can resolve to.
     */
    Class<? extends ExpressionSpec> type();
}

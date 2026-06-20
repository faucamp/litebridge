package org.litebridgedb.orm.expression;

/**
 * Represents an entity that can be resolved into an {@link ExpressionSpec}.
 * <p>
 * The resolution can be based on a database table or a specific column.
 * Implementations may provide concrete logic for resolution depending on the type.
 */
public interface Resolvable {

    /**
     * Returns the target column name for this expression.
     *
     * @return the target column name.
     */
    String column();

    /**
     * Returns the type of {@link ExpressionSpec} that this {@code Resolvable} can resolve to.
     *
     * @return the type of {@link ExpressionSpec} that this {@code Resolvable} can resolve to.
     */
    Class<? extends ExpressionSpec> type();
}

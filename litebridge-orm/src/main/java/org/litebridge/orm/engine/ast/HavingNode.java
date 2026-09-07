package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * HAVING clause condition in the query AST.
 */
public final class HavingNode implements QueryNode {

    private final @Nullable QueryNode previous;
    private QueryNode condition;

    /**
     * Creates a new {@code HavingNode} instance.
     *
     * @param previous  the previous node in the chain
     * @param condition the last embedded condition node for this node
     */
    public HavingNode(final @Nullable QueryNode previous, final QueryNode condition) {
        this.previous = previous;
        this.condition = condition;
    }

    @Override
    public @Nullable QueryNode previous() {
        return previous;
    }

    /**
     * Returns the HAVING condition node.
     *
     * @return the condition node
     */
    public QueryNode condition() {
        return condition;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (HavingNode) obj;
        return java.util.Objects.equals(this.previous, that.previous) &&
                java.util.Objects.equals(this.condition, that.condition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previous, condition);
    }

    @Override
    public String toString() {
        return "HavingNode[" +
                "previous=" + previous + ", " +
                "condition=" + condition + ']';
    }

    /**
     * Sets the HAVING condition node.
     *
     * @param condition the condition node to set
     * @return this having node instance
     */
    public HavingNode withCondition(final QueryNode condition) {
        this.condition = condition;
        return this;
    }
}

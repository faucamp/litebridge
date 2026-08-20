package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a WHERE clause condition in the query AST.
 *
 */
public final class WhereNode implements QueryNode {
    private final @Nullable QueryNode previous;
    private QueryNode condition;

    /**
     * @param previous  the previous node in the chain
     * @param condition the last embedded condition node for this node
     */
    public WhereNode(@Nullable QueryNode previous, QueryNode condition) {
        this.previous = previous;
        this.condition = condition;
    }

    @Override
    public @Nullable QueryNode previous() {
        return previous;
    }

    public QueryNode condition() {
        return condition;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        final WhereNode that = (WhereNode) obj;
        return Objects.equals(this.previous, that.previous) &&
                Objects.equals(this.condition, that.condition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previous, condition);
    }

    @Override
    public String toString() {
        return "WhereNode[" +
                "previous=" + previous + ", " +
                "condition=" + condition + ']';
    }

    public WhereNode withCondition(QueryNode condition) {
        this.condition = condition;
        return this;
    }

}

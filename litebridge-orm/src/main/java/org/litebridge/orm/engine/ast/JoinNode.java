package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a JOIN clause metadata in the query AST.
 */
public final class JoinNode implements QueryNode {
    private final @Nullable QueryNode previous;
    private final String type;
    private final @Nullable Class<?> dtoClass;
    private final @Nullable String rightTable;
    private @Nullable QueryNode condition;

    /**
     * Constructs a new {@code JoinNode}.
     *
     * @param previous   the previous node in the chain
     * @param type       the type of join (e.g., INNER, LEFT)
     * @param dtoClass   the DTO class being joined
     * @param rightTable the name of the table being joined
     */
    public JoinNode(@Nullable QueryNode previous,
                    //TODO: switch to enum
                    String type,
                    @Nullable Class<?> dtoClass,
                    @Nullable String rightTable) {
        this.previous = previous;
        this.type = type;
        this.dtoClass = dtoClass;
        this.rightTable = rightTable;
    }

    @Override
    public @Nullable QueryNode previous() {
        return previous;
    }

    /**
     * Returns the type of join.
     *
     * @return the join type
     */
    public String type() {
        return type;
    }

    /**
     * Returns the DTO class being joined.
     *
     * @return the DTO class
     */
    public @Nullable Class<?> dtoClass() {
        return dtoClass;
    }

    /**
     * Returns the name of the table being joined.
     *
     * @return the table name
     */
    public @Nullable String rightTable() {
        return rightTable;
    }

    /**
     * Returns the join condition node.
     *
     * @return the condition node
     */
    public @Nullable QueryNode condition() {
        return condition;
    }

    /**
     * Sets the join condition node.
     *
     * @param condition the condition node to set
     * @return this join node instance
     */
    public JoinNode withCondition(@Nullable QueryNode condition) {
        this.condition = condition;
        return this;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (JoinNode) obj;
        return Objects.equals(this.previous, that.previous) &&
                Objects.equals(this.type, that.type) &&
                Objects.equals(this.dtoClass, that.dtoClass) &&
                Objects.equals(this.rightTable, that.rightTable) &&
                Objects.equals(this.condition, that.condition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previous, type, dtoClass, rightTable, condition);
    }

    @Override
    public String toString() {
        return "JoinNode[" +
                "previous=" + previous + ", " +
                "type=" + type + ", " +
                "dtoClass=" + dtoClass + ", " +
                "rightTable=" + rightTable + ", " +
                "condition=" + condition + ']';
    }
}

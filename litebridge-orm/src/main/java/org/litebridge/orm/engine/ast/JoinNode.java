package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.Join;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * Represents a JOIN clause metadata in the query AST.
 */
public final class JoinNode implements QueryNode {

    private final @Nullable QueryNode previous;
    private final Join.JoinType type;
    private final @Nullable Class<?> dtoClass;
    private final @Nullable Class<?> contextDtoClass;
    private final @Nullable String table;
    private final @Nullable QueryNode queryNode;
    private final @Nullable String alias;
    private @Nullable QueryNode condition;

    /**
     * Constructs a new {@code JoinNode}.
     *
     * @param previous the previous node in the chain
     * @param type     the type of join (e.g., INNER, LEFT)
     * @param dtoClass the DTO class being joined
     * @param table    the name of the table being joined
     */
    public JoinNode(@Nullable QueryNode previous,
                    Join.JoinType type,
                    @Nullable Class<?> dtoClass,
                    @Nullable Class<?> contextDtoClass,
                    @Nullable String table,
                    @Nullable QueryNode queryNode,
                    @Nullable String alias) {
        this.previous = previous;
        this.type = type;
        this.dtoClass = dtoClass;
        this.contextDtoClass = contextDtoClass;
        this.table = table;
        this.queryNode = queryNode;
        this.alias = alias;
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
    public Join.JoinType type() {
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

    public @Nullable Class<?> contextDtoClass() {
        return contextDtoClass;
    }

    /**
     * Returns the name of the table being joined.
     *
     * @return the table name
     */
    public @Nullable String table() {
        return table;
    }

    public @Nullable QueryNode queryNode() {
        return queryNode;
    }

    public @Nullable String alias() {
        return alias;
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
     */
    public void setCondition(@Nullable QueryNode condition) {
        this.condition = condition;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (JoinNode) obj;
        return Objects.equals(this.previous, that.previous)
                && Objects.equals(this.type, that.type)
                && Objects.equals(this.dtoClass, that.dtoClass)
                && Objects.equals(this.contextDtoClass, that.contextDtoClass)
                && Objects.equals(this.table, that.table)
                && Objects.equals(this.condition, that.condition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previous, type, dtoClass, contextDtoClass, table, condition);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", JoinNode.class.getSimpleName() + "[", "]")
                .add("previous=" + previous)
                .add("type='" + type + "'")
                .add("dtoClass=" + dtoClass)
                .add("contextDtoClass=" + contextDtoClass)
                .add("table='" + table + "'")
                .add("condition=" + condition)
                .toString();
    }
}

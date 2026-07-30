package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import java.util.Objects;

/**
 * Represents a JOIN clause metadata in the query AST.
 */
public final class JoinNode implements QueryNode {
    private final @Nullable QueryNode previous;
    private final String type;
    private final @Nullable Class<?> dtoClass;
    private final @Nullable Class<?> sourceDtoClass;
    private final @Nullable String tableName;
    private @Nullable QueryNode condition;

    public JoinNode(@Nullable QueryNode previous,
                    String type,
                    @Nullable Class<?> dtoClass,
                    @Nullable Class<?> sourceDtoClass,
                    @Nullable String tableName) {
        this.previous = previous;
        this.type = type;
        this.dtoClass = dtoClass;
        this.sourceDtoClass = sourceDtoClass;
        this.tableName = tableName;
    }

    public JoinNode(@Nullable QueryNode previous,
                    String type,
                    @Nullable Class<?> dtoClass,
                    @Nullable String tableName) {
        this(previous, type, dtoClass, null, tableName);
    }

    @Override
    public @Nullable QueryNode previous() {
        return previous;
    }

    public String type() {
        return type;
    }

    public @Nullable Class<?> dtoClass() {
        return dtoClass;
    }

    public @Nullable Class<?> sourceDtoClass() {
        return sourceDtoClass;
    }

    public @Nullable String tableName() {
        return tableName;
    }

    public @Nullable QueryNode condition() {
        return condition;
    }

    public JoinNode withCondition(@Nullable QueryNode condition) {
        this.condition = condition;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (JoinNode) obj;
        return Objects.equals(this.previous, that.previous) &&
                Objects.equals(this.type, that.type) &&
                Objects.equals(this.dtoClass, that.dtoClass) &&
                Objects.equals(this.sourceDtoClass, that.sourceDtoClass) &&
                Objects.equals(this.tableName, that.tableName) &&
                Objects.equals(this.condition, that.condition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previous, type, dtoClass, sourceDtoClass, tableName, condition);
    }

    @Override
    public String toString() {
        return "JoinNode[" +
                "previous=" + previous + ", " +
                "type=" + type + ", " +
                "dtoClass=" + dtoClass + ", " +
                "sourceDtoClass=" + sourceDtoClass + ", " +
                "tableName=" + tableName + ", " +
                "condition=" + condition + ']';
    }
}

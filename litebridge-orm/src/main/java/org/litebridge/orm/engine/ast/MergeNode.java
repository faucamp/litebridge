package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;

public record MergeNode(@Nullable String table, @Nullable Class<?> dtoClass) implements QueryNode {

    @Override
    public @Nullable QueryNode previous() {
        return null;
    }
}

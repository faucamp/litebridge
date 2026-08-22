package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Table;

public record MergeNode(@Nullable String table, @Nullable Class<?> dtoClass) implements QueryNode {

    @Override
    public @Nullable QueryNode previous() {
        return null;
    }
}

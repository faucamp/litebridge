package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Table;

public record MergeNode(Table table, Class<?> dtoClass) implements QueryNode {

    @Override
    public @Nullable QueryNode previous() {
        return null;
    }
}

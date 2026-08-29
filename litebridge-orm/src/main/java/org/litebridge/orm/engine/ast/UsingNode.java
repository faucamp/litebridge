package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;

public record UsingNode(MergeNode previous,
                        @Nullable String table,
                        @Nullable Class<?> dtoClass,
                        QueryNode on) implements QueryNode {
}

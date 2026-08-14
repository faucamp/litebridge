package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;

public record WhenMatchedNode(QueryNode previous,
                              @Nullable SetNode update,
                              @Nullable DeleteNode delete) implements QueryNode {
}

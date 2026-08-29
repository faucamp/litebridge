package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;

public record WhenMatchedNode(QueryNode previous,
                              @Nullable QueryNode and,
                              @Nullable SetNode update,
                              boolean delete) implements QueryNode {
}

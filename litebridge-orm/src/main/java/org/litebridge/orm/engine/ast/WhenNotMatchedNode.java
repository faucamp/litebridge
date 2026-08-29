package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;

public record WhenNotMatchedNode(QueryNode previous,
                                 @Nullable QueryNode and,
                                 InsertValuesNode insert) implements QueryNode {
}

package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;

public record WhenNotMatchedNode(QueryNode previous,
                                 @Nullable InsertValuesNode insert) implements QueryNode {
}

package org.litebridge.orm.api.select.ast;

import org.litebridge.db.spi.Table;

public record UsingNode(MergeNode previous, Table table) implements QueryNode {
}

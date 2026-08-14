package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.List;

/**
 * Represents an INSERT statement in the query AST.
 *
 * @param previous the previous node in the chain
 * @param table    the table to insert into
 */
public record InsertNode(@Nullable QueryNode previous, Table table, String[] columns) implements QueryNode {

    public InsertNode(@Nullable QueryNode previous, Table table) {
        this(previous, table, null);
    }
}

package org.litebridgedb.orm.function;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;

/**
 * Expression that selects a database column.
 *
 * @param column The column to select.
 */
public record SelectColumn(Column column) implements Expression {

    /**
     * Transforms a {@link ProtoSelectColumn} into a {@link SelectColumn}.
     *
     * @param protoSelectColumn The proto select column with column name/alias.
     * @param table             The table for the column.
     */
    public SelectColumn(ProtoSelectColumn protoSelectColumn, Table table) {
        this(new Column(table, protoSelectColumn.column(), protoSelectColumn.alias()));
    }
}

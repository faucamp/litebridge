package org.litebridgedb.orm.function;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;

public record SelectColumn(Column column) implements Expression {

    public SelectColumn(ProtoSelectColumn protoSelectColumn, Table table) {
        this(new Column(table, protoSelectColumn.column(), protoSelectColumn.alias()));
    }
}

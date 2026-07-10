package org.litebridgedb.maven.reverse;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;

public record ManyToManyMapping(TableMetaData leftTable,
                                ColumnMetaData leftColumn,
                                Table joinTable,
                                Column leftJoinColumn,
                                Column rightJoinColumn,
                                TableMetaData rightTable,
                                ColumnMetaData rightColumn) {
}

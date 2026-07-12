package org.litebridge.maven.reverse;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;

public record ManyToManyMapping(TableMetaData leftTable,
                                ColumnMetaData leftColumn,
                                Table joinTable,
                                Column leftJoinColumn,
                                Column rightJoinColumn,
                                TableMetaData rightTable,
                                ColumnMetaData rightColumn) {
}

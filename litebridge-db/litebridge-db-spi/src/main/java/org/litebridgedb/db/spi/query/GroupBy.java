package org.litebridgedb.db.spi.query;

import org.litebridgedb.db.spi.Column;

import java.util.List;

@Deprecated(forRemoval = true)
public record GroupBy(List<Column> columns) {
}

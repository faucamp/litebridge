package org.litebridgedb.db.spi.query;

import org.litebridgedb.db.spi.Column;

import java.util.List;

public record GroupBy(List<Column> columns) {
}

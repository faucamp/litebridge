package org.litebridge.db.spi.query;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;

import java.util.List;
import java.util.Optional;

public record Select(Table table,
                     List<Column> columns,
                     List<Join> joins,
                     List<OrderBy> orderBy,
                     List<Condition> where,
                     Optional<Limit> limit) {

}

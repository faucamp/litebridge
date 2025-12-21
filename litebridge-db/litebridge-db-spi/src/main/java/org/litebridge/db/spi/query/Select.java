package org.litebridge.db.spi.query;

import org.litebridge.db.spi.TableMetaData;

import java.util.List;

public record Select(TableMetaData table,
                     List<SelectField> columns,
                     List<Join> joins,
                     List<OrderBy> orderBy,
                     List<Condition> where,
                     Limit limit) {

}

package org.litebridge.db.spi.update;

import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.query.Condition;

import java.util.List;

public record Update(TableMetaData table, List<ColumnValue> columnValues, List<Condition> where)
        implements UpdateStatement {
}

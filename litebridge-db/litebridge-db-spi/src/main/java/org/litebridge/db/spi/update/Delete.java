package org.litebridge.db.spi.update;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.Condition;

import java.util.List;

public record Delete(Table table, List<Condition> where)
        implements UpdateStatement {
}

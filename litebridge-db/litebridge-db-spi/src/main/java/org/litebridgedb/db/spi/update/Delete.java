package org.litebridgedb.db.spi.update;

import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.Condition;

import java.util.List;

public record Delete(Table table, List<Condition> where)
        implements UpdateStatement {
}

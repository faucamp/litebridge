package org.litebridgedb.orm.api.select.model;

import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.Join;

public interface JoinSpec {
    Table table();

    Join toJoin();
}

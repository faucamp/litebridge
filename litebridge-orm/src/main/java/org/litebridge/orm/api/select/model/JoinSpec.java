package org.litebridge.orm.api.select.model;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.Join;

public interface JoinSpec {
    Table table();

    Join toJoin();
}

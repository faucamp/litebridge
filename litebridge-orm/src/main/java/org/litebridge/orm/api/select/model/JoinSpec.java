package org.litebridge.orm.api.select.model;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.Join;

import java.util.List;

public interface JoinSpec {
    Table table();

    List<ConditionSpec> conditions();

    Join toJoin();
}

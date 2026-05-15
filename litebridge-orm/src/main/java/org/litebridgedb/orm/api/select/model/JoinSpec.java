package org.litebridgedb.orm.api.select.model;

import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.Join;

import java.util.List;

public interface JoinSpec {
    Table table();

    List<ConditionSpec> conditions();

    Join toJoin();
}

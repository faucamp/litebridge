package org.litebridge.db.spi.alias;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.SelectTarget;

public record AliasedTable(String alias, Table target) implements Aliased<Table>, SelectTarget {
}

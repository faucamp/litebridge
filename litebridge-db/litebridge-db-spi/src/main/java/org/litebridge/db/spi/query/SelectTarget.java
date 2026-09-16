package org.litebridge.db.spi.query;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.alias.AliasedQuery;
import org.litebridge.db.spi.alias.AliasedTable;

public sealed interface SelectTarget permits AliasedQuery, AliasedTable, Select, Table {
}

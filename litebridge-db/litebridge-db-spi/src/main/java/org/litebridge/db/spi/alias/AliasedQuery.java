package org.litebridge.db.spi.alias;

import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.query.SelectTarget;

public record AliasedQuery(String alias, Select target) implements Aliased<Select>, SelectTarget {
}

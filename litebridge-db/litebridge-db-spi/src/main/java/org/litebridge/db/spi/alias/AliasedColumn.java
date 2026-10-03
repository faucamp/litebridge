package org.litebridge.db.spi.alias;

import org.litebridge.db.spi.Column;

public record AliasedColumn(String alias, Column target) implements Aliased<Column> {
}

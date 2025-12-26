package org.litebridge.db.spi.query;

import org.litebridge.db.spi.Table;

import java.util.List;

public record Join(Table table, List<Condition> conditions) {
}

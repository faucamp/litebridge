package org.litebridge.db.spi.query;

import java.util.List;

public record Join(String table, List<Condition> conditions) {
}

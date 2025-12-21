package org.litebridge.db.api.query;

import java.util.List;

public record Join(String table, List<Condition> conditions) {
}

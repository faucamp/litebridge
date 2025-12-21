package org.litebridge.db.api.query;

import org.jspecify.annotations.Nullable;

public record Limit(@Nullable Integer limit, @Nullable Integer offset) {
}

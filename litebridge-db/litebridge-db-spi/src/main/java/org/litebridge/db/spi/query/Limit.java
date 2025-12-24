package org.litebridge.db.spi.query;

import java.util.Optional;

public record Limit(Optional<Integer> limit, Optional<Integer> offset) {
}

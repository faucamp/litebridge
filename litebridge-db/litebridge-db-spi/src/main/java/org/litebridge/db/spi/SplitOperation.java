package org.litebridge.db.spi;

import java.util.List;

public record SplitOperation(List<PreparedOperation> operations) {
}

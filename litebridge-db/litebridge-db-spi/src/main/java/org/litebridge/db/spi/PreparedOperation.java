package org.litebridge.db.spi;

import org.litebridge.db.spi.sql.BindValue;

import java.util.List;


public record PreparedOperation(Operation operation, List<BindValue> bindValues) {
}

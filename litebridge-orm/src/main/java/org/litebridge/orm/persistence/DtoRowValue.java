package org.litebridge.orm.persistence;

import org.litebridge.db.spi.update.RowValue;

public record DtoRowValue(Object dto, RowValue rowValue) {
}

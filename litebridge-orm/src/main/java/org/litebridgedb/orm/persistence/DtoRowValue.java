package org.litebridgedb.orm.persistence;

import org.litebridgedb.db.spi.update.RowValue;

public record DtoRowValue(Object dto, RowValue rowValue) {
}

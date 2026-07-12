package org.litebridge.orm.persistence;

import org.litebridge.db.spi.update.RowValue;

/**
 * Represents a pair of a DTO and its corresponding row value.
 *
 * @param dto      the DTO object
 * @param rowValue the database row value
 */
public record DtoRowValue(Object dto, RowValue rowValue) {
}

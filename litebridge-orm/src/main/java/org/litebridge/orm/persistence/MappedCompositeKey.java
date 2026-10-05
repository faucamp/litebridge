package org.litebridge.orm.persistence;

import org.litebridge.db.spi.MappedFieldTarget;

import java.util.function.Supplier;

/**
 * A mapped composite key, used for mapping multiple columns to a single field containing a related DTO.
 *
 * @param columns Database columns that make up the composite key.
 */
public record MappedCompositeKey(MappedFieldTarget[] columns,

                                 Supplier<OrmTable> targetOrmTable,
                                 String[] targetColumns) implements MappedFieldTarget {
}
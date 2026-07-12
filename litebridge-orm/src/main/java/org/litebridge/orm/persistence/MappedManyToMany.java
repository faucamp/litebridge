package org.litebridge.orm.persistence;

import org.litebridge.commons.type.ConcurrentLazy;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.tracking.FieldAccessor;

public record MappedManyToMany(OrmTable joinTable,
                               String joinColumn,
                               FieldAccessor collection,

                               ConcurrentLazy<OrmTable> targetTable,
                               String inverseJoinColumn) implements MappedFieldTarget {
}

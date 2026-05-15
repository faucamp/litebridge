package org.litebridgedb.orm.persistence;

import org.litebridgedb.commons.type.ConcurrentLazy;
import org.litebridgedb.db.spi.MappedFieldTarget;
import org.litebridgedb.tracking.FieldAccessor;

public record MappedManyToMany(OrmTable joinTable,
                               String joinColumn,
                               FieldAccessor collection,

                               ConcurrentLazy<OrmTable> targetTable,
                               String inverseJoinColumn) implements MappedFieldTarget {
}

package org.litebridge.orm.persistence;

import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.tracking.FieldAccessor;

public record MappedOneToMany(FieldAccessor mappedByField, FieldAccessor collection) implements MappedFieldTarget {

}

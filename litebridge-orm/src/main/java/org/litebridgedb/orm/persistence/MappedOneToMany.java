package org.litebridgedb.orm.persistence;

import org.litebridgedb.db.spi.MappedFieldTarget;
import org.litebridgedb.tracking.FieldAccessor;

public record MappedOneToMany(FieldAccessor mappedByField, FieldAccessor collection) implements MappedFieldTarget {

}

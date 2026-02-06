package org.litebridge.orm.persistence;

import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.tracking.FieldAccessor;

public class MappedOneToMany implements MappedFieldTarget {

    private final FieldAccessor collection;
    private final FieldAccessor mappedByField;

    public MappedOneToMany(final FieldAccessor mappedByField, final FieldAccessor reverseMappingCollection) {
        this.mappedByField = mappedByField;
        this.collection = reverseMappingCollection;
    }

    public FieldAccessor mappedByField() {
        return mappedByField;
    }

    public FieldAccessor collection() {
        return collection;
    }
}

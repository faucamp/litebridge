package org.litebridge.orm.persistence;

import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.tracking.FieldAccessor;

public class MappedOneToMany implements MappedFieldTarget {

    private final FieldAccessor reverseMappingCollection;
    private final FieldAccessor mappedByField;

    public MappedOneToMany(final FieldAccessor mappedByField, final FieldAccessor reverseMappingCollection) {
        this.mappedByField = mappedByField;
        this.reverseMappingCollection = reverseMappingCollection;
    }

    public FieldAccessor mappedByField() {
        return mappedByField;
    }

    public FieldAccessor reverseMappingCollection() {
        return reverseMappingCollection;
    }
}

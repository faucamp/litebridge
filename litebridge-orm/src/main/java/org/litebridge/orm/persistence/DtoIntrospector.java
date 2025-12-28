package org.litebridge.orm.persistence;

import org.litebridge.orm.api.spec.FieldSpec;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;

public final class DtoIntrospector extends ClassFieldAccessorCache {

    private DtoIntrospector() {
    }

    public static FieldAccessor fieldAccessor(final Class<?> dtoClass, final FieldSpec fieldSpec) {
        if (fieldSpec.property()) {
            return propertyAccessor(dtoClass, fieldSpec.name());
        } else {
            return fieldAccessor(dtoClass, fieldSpec.name());
        }
    }
}

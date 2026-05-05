package org.litebridge.orm.api.spec;

import java.util.Collections;
import java.util.List;

/**
 * DTO-to-table mapping details, used for registering a DTO class as an entity with the Litebridge ORM.
 *
 * @param dtoClass      the class of the Data Transfer Object to be registered; must not be null.
 * @param tableSpec     the table specification defining the mapping of the DTO class to the database table; must not be null.
 * @param dtoInterfaces list of additional superinterfaces of the DTO class; may be empty if not needed
 */
public record DtoTableSpec(Class<?> dtoClass,
                           TableSpec tableSpec,
                           List<Class<?>> dtoInterfaces) {

    /**
     * Constructs a DtoTableSpec instance using the specified DTO class and table specification.
     * The list of DTO interfaces is initialized as an empty list.
     *
     * @param dtoClass  the class of the Data Transfer Object to be registered; must not be null.
     * @param tableSpec the table specification defining the mapping of the DTO class to the database table; must not be null.
     */
    public DtoTableSpec(Class<?> dtoClass, TableSpec tableSpec) {
        this(dtoClass, tableSpec, Collections.emptyList());
    }
}

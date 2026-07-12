package org.litebridge.orm.api.register;

import org.litebridge.orm.api.spec.DtoTableSpec;
import org.litebridge.orm.api.spec.TableSpec;

import java.util.Collections;

/**
 * Builder class for constructing instances of {@link DtoTableSpec}.
 * <p>
 * This class is part of the fluent API provided by the Litebridge ORM for registering the mapping
 * between a Data Transfer Object (DTO) class and its corresponding database table. It leverages the
 * {@link RegistrationContextTerminal} as input to extract the necessary mapping details.
 * <p>
 * The {@link DtoTableSpecBuilder} processes the provided registration context and produces an immutable
 * {@link DtoTableSpec} that encapsulates the following:
 * <ul>
 *  <li>The DTO class being registered.</li>
 *  <li>The database table specification ({@link TableSpec}), including the table name and field-to-column mappings.</li>
 *  <li>A list of optional superinterfaces for the DTO class.</li>
 * </ul>
 * Instances of this class are not reusable once the {@link #build()} method has been invoked.
 */
public class DtoTableSpecBuilder {

    private final RegistrationContextTerminal context;

    /**
     * Constructs a new instance of {@code DtoTableSpecBuilder} using the specified registration context.
     * This constructor initialises the builder with the provided {@link RegistrationContextTerminal},
     * which contains the necessary mapping details such as the target DTO class, database table name,
     * field-to-column mappings, and optional superinterfaces.
     *
     * @param context The {@link RegistrationContextTerminal} containing the configuration details
     *                for registering a data transfer object (DTO) with its corresponding
     *                database table in the ORM framework. Must not be {@code null}.
     */
    public DtoTableSpecBuilder(final RegistrationContextTerminal context) {
        this.context = context;
    }

    /**
     * Builds the {@link DtoTableSpec} instance encapsulating the DTO class, table specification, and superinterfaces.
     *
     * @return The constructed {@link DtoTableSpec} instance.
     */
    public DtoTableSpec build() {
        return new DtoTableSpec(context.dtoClass, new TableSpec(context.tableName, context.fieldColumnMap), context.dtoInterfaces != null ? context.dtoInterfaces : Collections.emptyList());
    }
}

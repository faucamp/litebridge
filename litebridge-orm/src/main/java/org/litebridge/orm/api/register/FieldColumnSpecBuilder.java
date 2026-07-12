package org.litebridge.orm.api.register;

import org.litebridge.orm.api.spec.FieldColumnSpec;
import org.litebridge.orm.api.spec.FieldSpec;

import java.util.function.Function;

/**
 * A builder for specifying mappings between fields or properties in a DTO (Data Transfer Object)
 * and database expressions. This class is part of the fluent API for creating field-to-column mapping
 * specifications in the ORM framework.
 * <p>
 * The mappings allow developers to configure how fields or properties should map to database
 * structures, including basic field-column mappings, property-based mappings, and advanced relations
 * such as one-to-many and many-to-many associations.
 * <p>
 * This builder facilitates a multi-step configuration process where each method represents a specific
 * stage in defining the field-column specification. The result of the configuration is encapsulated
 * in a {@link FieldColumnSpec}, which can later be used for persistence or ORM-related operations.
 * <p>
 * The following steps summarise how this builder operates:
 * <ul>
 *  <li>A field or property in a DTO is mapped to begin configuration using {@code mapField}
 * or {@code mapProperty}.</li>
 *  <li>Subsequent methods allow specifying database column names, or defining relationships
 * like one-to-many or many-to-many.</li>
 *  <li>The resulting configuration is finalised and returned as a complete, immutable spec.</li>
 * </ul>
 * <p>
 * This class supports the creation of specifications using a declarative and type-safe approach.
 * Any ambiguities and potential misconfigurations are resolved at runtime while ensuring a fluent,
 * readable API design.
 */
public final class FieldColumnSpecBuilder {

    /**
     * Maps a specific field in a DTO to begin the configuration process for defining its database column mapping.
     * This is the initial step of the field-to-column mapping process using the builder.
     *
     * @param fieldName The name of the field in the DTO to be mapped. This name should correspond to the
     *                  field in the DTO class and must not be {@code null}.
     * @return An instance of {@link FieldColumnSpecBuilderFieldStep} to continue the configuration of the field mapping.
     */
    public FieldColumnSpecBuilderFieldStep mapField(final String fieldName) {
        return new FieldColumnSpecBuilderFieldStep(new FieldSpec(fieldName, false));
    }

    /**
     * Maps a specific property in a DTO to begin the configuration process for defining its database
     * property-based mapping. This method assumes property-level access using getter and setter
     * conventions in the DTO class.
     *
     * @param fieldName The name of the property in the DTO to be mapped. This name should correspond
     *                  to the property in the DTO class and must not be {@code null}.
     * @return An instance of {@link FieldColumnSpecBuilderFieldStep} to continue the configuration
     * of the property mapping.
     */
    public FieldColumnSpecBuilderFieldStep mapProperty(final String fieldName) {
        return new FieldColumnSpecBuilderFieldStep(new FieldSpec(fieldName, true));
    }

    /**
     * Constructs a {@link FieldColumnSpec} by applying a function to a {@link FieldColumnSpecBuilder}.
     * The function defines the steps and configuration for mapping a field or property in a DTO
     * to its corresponding database column or table specification.
     *
     * @param rc A function that accepts a {@link FieldColumnSpecBuilder} and returns a
     *           {@link FieldColumnSpecBuilderTerminal}. This function defines the sequence
     *           of steps required to configure the field-to-column mapping.
     * @return A {@link FieldColumnSpec} representing the finalised mapping between the DTO field
     * or property and the database column or table specification.
     */
    public static FieldColumnSpec spec(Function<FieldColumnSpecBuilder, FieldColumnSpecBuilderTerminal> rc) {
        final FieldColumnSpecBuilderTerminal terminal = rc.apply(new FieldColumnSpecBuilder());

        return switch (terminal) {
            case FieldColumnSpecBuilderTerminalImpl t -> t.build();
            case FieldColumnSpecBuilderColumnStep t -> t.build();
            case FieldColumnSpecBuilderJoinStep t -> t.build();
        };
    }
}

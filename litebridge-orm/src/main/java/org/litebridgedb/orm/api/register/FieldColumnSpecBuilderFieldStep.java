package org.litebridgedb.orm.api.register;

import org.litebridgedb.orm.api.spec.FieldSpec;
import org.litebridgedb.orm.api.spec.ManyToMany;
import org.litebridgedb.orm.api.spec.OneToMany;

import java.util.function.Function;

/**
 * A builder step for configuring the mapping of a field in a data model to a database column or
 * relationship. This class is part of a fluent API that facilitates the specification of field-to-column
 * mappings, one-to-many relationships, and many-to-many relationships in a structured and readable way.
 *
 * <h2>Responsibilities:</h2>
 * - Provide a mechanism to transition to the column mapping step.
 * - Enable the specification of one-to-many relationships.
 * - Enable the specification of many-to-many relationships.
 *
 * <h2>Key Operations:</h2>
 * - `toColumn(String column)`: Advances the builder to the column mapping step for the current field.
 * - `oneToMany(Function<OneToManyBuilder, OneToMany> c)`: Defines a one-to-many relationship for the field
 * using a functional builder.
 * - `manyToMany(Function<ManyToManyBuilder, ManyToMany> c)`: Defines a many-to-many relationship for the
 * field using a functional builder.
 * <p>
 * This class operates in conjunction with other builder steps such as
 * {@link FieldColumnSpecBuilderColumnStep}, {@link OneToManyBuilder}, and {@link ManyToManyBuilder}
 * to fully define the mapping details for a given field.
 * <p>
 * Instances of this class are immutable and represent transitional steps in the builder process.
 */
public final class FieldColumnSpecBuilderFieldStep {

    private final FieldSpec fieldSpec;

    FieldColumnSpecBuilderFieldStep(final FieldSpec fieldSpec) {
        this.fieldSpec = fieldSpec;
    }

    /**
     * Proceeds to the column mapping step for the current field in the fluent API builder.
     * This method is used to define the name of the database column associated with the field
     * being configured.
     *
     * @param column The name of the database column to which the field should be mapped.
     *               Must not be null or empty.
     * @return An instance of {@link FieldColumnSpecBuilderColumnStep}, enabling further
     * configuration of the column or finalisation of the field-to-column mapping.
     */
    public FieldColumnSpecBuilderColumnStep toColumn(final String column) {
        return new FieldColumnSpecBuilderColumnStep(fieldSpec, column);
    }

    /**
     * Defines a one-to-many relationship for the current field in the fluent API builder.
     * This method applies the provided function to configure the relationship and transitions
     * to the terminal step of the builder.
     *
     * @param c A function that accepts a {@link OneToManyBuilder} to configure
     *          the one-to-many relationship and returns an instance of {@link OneToMany}.
     * @return An instance of {@link FieldColumnSpecBuilderTerminal}, representing the
     * terminal step of the builder for further operations or finalisation.
     */
    public FieldColumnSpecBuilderTerminal oneToMany(Function<OneToManyBuilder, OneToMany> c) {
        return new FieldColumnSpecBuilderTerminalImpl(fieldSpec, c.apply(new OneToManyBuilder()));
    }

    /**
     * Defines a many-to-many relationship for the current field in the fluent API builder.
     * This method applies the provided function to configure the relationship and transitions
     * to the terminal step of the builder.
     *
     * @param c A function that accepts a {@link ManyToManyBuilder} to configure the many-to-many
     *          relationship and returns an instance of {@link ManyToMany}.
     * @return An instance of {@link FieldColumnSpecBuilderTerminal}, representing the terminal
     * step of the builder for further operations or finalisation.
     */
    public FieldColumnSpecBuilderTerminal manyToMany(Function<ManyToManyBuilder, ManyToMany> c) {
        return new FieldColumnSpecBuilderTerminalImpl(fieldSpec, c.apply(new ManyToManyBuilder()));
    }
}

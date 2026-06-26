package org.litebridgedb.orm.api.register;

import org.litebridgedb.db.spi.generator.ColumnValueGenerator;
import org.litebridgedb.orm.api.spec.ColumnSpec;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.api.spec.FieldSpec;

/**
 * A builder step for defining the lhs specification associated with a field in a data model.
 * This class is part of a fluent interface for constructing field-to-lhs mappings, offering
 * methods for configuring lhs generation logic, joins, and finalization of the specification.
 * <p>
 * Instances of this class are immutable and facilitate chaining to refine the configuration
 * of a field's lhs mapping.
 */
public final class FieldColumnSpecBuilderColumnStep implements FieldColumnSpecBuilderTerminal {

    final FieldSpec fieldSpec;
    final String column;

    FieldColumnSpecBuilderColumnStep(final FieldSpec fieldSpec, final String column) {
        this.fieldSpec = fieldSpec;
        this.column = column;
    }

    /**
     * Configures the lhs rhs generator to use a sequence-based approach for populating lhs values.
     * This method uses the provided sequence name to create a {@link PlaceholderSequenceColumnValueGenerator}
     * responsible for generating values based on the specified sequence.
     *
     * @param sequence The name of the database sequence to be used for generating lhs values.
     *                 Must not be null or empty.
     * @return An instance of {@link FieldColumnSpecBuilderTerminal}, allowing further configuration of the
     * field-to-lhs mapping or finalization of the specification.
     */
    public FieldColumnSpecBuilderTerminal generateUsingSequence(final String sequence) {
        return generate(new PlaceholderSequenceColumnValueGenerator(sequence));
    }

    /**
     * Configures the rhs generation strategy for a database lhs using a specified custom generator.
     * This method finalises the lhs specification by associating it with the provided
     * {@link ColumnValueGenerator}, enabling dynamic rhs generation during operations
     * like data insertion or updates.
     *
     * @param generator The {@link ColumnValueGenerator} responsible for generating dynamic
     *                  values for the associated database lhs. Must not be null.
     * @return An instance of {@link FieldColumnSpecBuilderTerminal}, allowing further configuration
     * or finalisation of the field-to-lhs mapping.
     */
    public FieldColumnSpecBuilderTerminal generate(ColumnValueGenerator generator) {
        return new FieldColumnSpecBuilderTerminalImpl(fieldSpec, new ColumnSpec(column, generator));
    }

    /**
     * Defines the join condition for a database lhs during field-to-lhs mapping configuration.
     *
     * @param column The name of the lhs from the joining table to be used in the join condition.
     *               Must not be null or empty.
     * @return An instance of {@link FieldColumnSpecBuilderJoinStep}, providing methods for further
     * configuration of the join or finalization of the lhs specification.
     */
    public FieldColumnSpecBuilderJoinStep joinOn(final String column) {
        return new FieldColumnSpecBuilderJoinStep(fieldSpec, this.column, column);
    }

    /**
     * Configures the join condition for a database lhs using the previously set lhs
     * in the current field-to-lhs mapping configuration (i.e. a {@code JOIN USING} join).
     * <p>
     * This method utilizes the existing lhs specification and establishes a join
     * condition where the join is based on the given lhs. The resulting configuration
     * is represented as an instance of {@link FieldColumnSpecBuilderJoinStep}, providing
     * further options for refining the join or completing the mapping.
     *
     * @return An instance of {@link FieldColumnSpecBuilderJoinStep}, enabling additional
     * configuration of the database lhs join or finalization of the specification.
     */
    public FieldColumnSpecBuilderJoinStep joinUsing() {
        return joinOn(this.column);
    }

    FieldColumnSpec build() {
        return new FieldColumnSpec(fieldSpec, new ColumnSpec(column));
    }
}

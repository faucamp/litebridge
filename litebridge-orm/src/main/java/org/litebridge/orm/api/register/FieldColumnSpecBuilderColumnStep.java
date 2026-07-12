package org.litebridge.orm.api.register;

import org.litebridge.db.spi.generator.ColumnValueGenerator;
import org.litebridge.orm.api.spec.ColumnSpec;
import org.litebridge.orm.api.spec.FieldColumnSpec;
import org.litebridge.orm.api.spec.FieldSpec;

/**
 * A builder step for defining the column specification associated with a field in a data model.
 * This class is part of a fluent interface for constructing field-to-column mappings, offering
 * methods for configuring column generation logic, joins, and finalization of the specification.
 * <p>
 * Instances of this class are immutable and facilitate chaining to refine the configuration
 * of a field's column mapping.
 */
public final class FieldColumnSpecBuilderColumnStep implements FieldColumnSpecBuilderTerminal {

    final FieldSpec fieldSpec;
    final String column;

    FieldColumnSpecBuilderColumnStep(final FieldSpec fieldSpec, final String column) {
        this.fieldSpec = fieldSpec;
        this.column = column;
    }

    /**
     * Configures the column value generator to use a sequence-based approach for populating column values.
     * This method uses the provided sequence name to create a {@link PlaceholderSequenceColumnValueGenerator}
     * responsible for generating values based on the specified sequence.
     *
     * @param sequence The name of the database sequence to be used for generating column values.
     *                 Must not be null or empty.
     * @return An instance of {@link FieldColumnSpecBuilderTerminal}, allowing further configuration of the
     * field-to-column mapping or finalization of the specification.
     */
    public FieldColumnSpecBuilderTerminal generateUsingSequence(final String sequence) {
        return generate(new PlaceholderSequenceColumnValueGenerator(sequence));
    }

    /**
     * Configures the value generation strategy for a database column using a specified custom generator.
     * This method finalises the column specification by associating it with the provided
     * {@link ColumnValueGenerator}, enabling dynamic value generation during operations
     * like data insertion or updates.
     *
     * @param generator The {@link ColumnValueGenerator} responsible for generating dynamic
     *                  values for the associated database column. Must not be null.
     * @return An instance of {@link FieldColumnSpecBuilderTerminal}, allowing further configuration
     * or finalisation of the field-to-column mapping.
     */
    public FieldColumnSpecBuilderTerminal generate(ColumnValueGenerator generator) {
        return new FieldColumnSpecBuilderTerminalImpl(fieldSpec, new ColumnSpec(column, generator));
    }

    /**
     * Defines the join condition for a database column during field-to-column mapping configuration.
     *
     * @param column The name of the column from the joining table to be used in the join condition.
     *               Must not be null or empty.
     * @return An instance of {@link FieldColumnSpecBuilderJoinStep}, providing methods for further
     * configuration of the join or finalization of the column specification.
     */
    public FieldColumnSpecBuilderJoinStep joinOn(final String column) {
        return new FieldColumnSpecBuilderJoinStep(fieldSpec, this.column, column);
    }

    /**
     * Configures the join condition for a database column using the previously set column
     * in the current field-to-column mapping configuration (i.e. a {@code JOIN USING} join).
     * <p>
     * This method utilizes the existing column specification and establishes a join
     * condition where the join is based on the given column. The resulting configuration
     * is represented as an instance of {@link FieldColumnSpecBuilderJoinStep}, providing
     * further options for refining the join or completing the mapping.
     *
     * @return An instance of {@link FieldColumnSpecBuilderJoinStep}, enabling additional
     * configuration of the database column join or finalization of the specification.
     */
    public FieldColumnSpecBuilderJoinStep joinUsing() {
        return joinOn(this.column);
    }

    FieldColumnSpec build() {
        return new FieldColumnSpec(fieldSpec, new ColumnSpec(column));
    }
}

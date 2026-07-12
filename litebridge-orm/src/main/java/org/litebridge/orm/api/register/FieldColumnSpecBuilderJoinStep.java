package org.litebridge.orm.api.register;

import org.litebridge.orm.api.spec.ColumnSpec;
import org.litebridge.orm.api.spec.DtoTableSpec;
import org.litebridge.orm.api.spec.FieldColumnSpec;
import org.litebridge.orm.api.spec.FieldSpec;
import org.litebridge.orm.api.spec.TableMapping;

import java.util.function.Function;

/**
 * A builder step for defining the join column mapping in a field-to-database-column specification.
 * This class allows specifying a join column as part of the mapping configuration while building
 * field specifications for an Object-Relational Mapping (ORM) system.
 * <p>
 * This class is immutable and part of a step-by-step fluent API to configure ORM mappings, ensuring
 * that only valid sequences of method calls are made during the configuration process.
 */
public final class FieldColumnSpecBuilderJoinStep implements FieldColumnSpecBuilderTerminal {

    private final FieldSpec fieldSpec;
    private final String column;
    private final String joinColumn;

    public FieldColumnSpecBuilderJoinStep(final FieldSpec fieldSpec,
                                          final String column,
                                          final String joinColumn) {
        this.fieldSpec = fieldSpec;
        this.column = column;
        this.joinColumn = joinColumn;
    }

    /**
     * Maps the current column configuration to a database table associated with the specified Data Transfer Object (DTO) class.
     * This method resolves the table mapping for the provided DTO class and applies the necessary context transformations
     * via the given function to finalise the table specification for the column mapping.
     *
     * @param dtoClass The class of the Data Transfer Object (DTO) that will be mapped to the database table.
     * @param rc       A function that processes the registration context, allowing additional mapping configurations
     *                 or custom transformations, and returns a terminal registration context.
     * @return An instance of {@code FieldColumnSpecBuilderTerminal} representing the final step in the field-to-database-column
     * mapping configuration.
     */
    public FieldColumnSpecBuilderTerminal withMappedTable(final Class<?> dtoClass, final Function<RegistrationContext, RegistrationContextTerminal> rc) {
        final RegistrationContextTerminal context = rc.apply(new RegistrationContext(dtoClass, new PlaceHolderDatabaseProvider()));
        final DtoTableSpec dtoTableSpec = new DtoTableSpecBuilder(context).build();
        return new FieldColumnSpecBuilderTerminalImpl(fieldSpec, new ColumnSpec(column, null, joinColumn, new TableMapping(dtoClass, dtoTableSpec.tableSpec())));
    }

    FieldColumnSpec build() {
        return new FieldColumnSpec(fieldSpec, new ColumnSpec(column, null, joinColumn));
    }
}

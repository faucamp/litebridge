package org.litebridgedb.orm.api.register;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.db.spi.generator.ColumnValueGenerator;
import org.litebridgedb.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridgedb.orm.api.spec.ColumnMapping;
import org.litebridgedb.orm.api.spec.ColumnSpec;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.api.spec.FieldMapping;
import org.litebridgedb.orm.api.spec.TableMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Terminal context for registering the mapping between a data transfer object (DTO)
 * class and its corresponding database table within the ORM framework.
 * <p>
 * This class serves as a final step in constructing the mapping information for a DTO to
 * interact with a specific database table. It encapsulates details such as the target DTO class,
 * the associated database table name, field-to-column mappings, and the database provider responsible
 * for performing specific database-related operations.
 * <p>
 * Instances of this class are immutable after their internal state has been configured through the
 * builder-like pattern provided by the {@code with} method.
 */
public final class RegistrationContextTerminal {

    final Class<?> dtoClass;
    final String tableName;
    final Map<FieldMapping, ColumnMapping> fieldColumnMap;
    final DatabaseProvider databaseProvider;
    final @Nullable List<Class<?>> dtoInterfaces;

    RegistrationContextTerminal(final Class<?> dtoClass, final String tableName, final DatabaseProvider databaseProvider, @Nullable final List<Class<?>> dtoInterfaces) {
        this.dtoClass = dtoClass;
        this.tableName = tableName;
        this.databaseProvider = databaseProvider;
        this.fieldColumnMap = new LinkedHashMap<>();
        this.dtoInterfaces = dtoInterfaces;
    }

    /**
     * Configures a field-to-column mapping for a Data Transfer Object (DTO) within the current registration
     * context by applying a specification function. This method is used to define how a specific field in the DTO
     * maps to its corresponding database column, allowing for the configuration of column specifications,
     * sequence generators, and other mapping details.
     *
     * @param spec A {@link Function} taking a {@link FieldColumnSpecBuilder} to define the field-to-column mapping.
     *             The function returns a {@link FieldColumnSpecBuilderTerminal}, which represents the finalized mapping
     *             configuration.
     * @return An updated {@link RegistrationContextTerminal} instance with the specified field-to-column mapping
     * added to the context.
     */
    public RegistrationContextTerminal with(final Function<FieldColumnSpecBuilder, FieldColumnSpecBuilderTerminal> spec) {
        final FieldColumnSpec fieldColumnSpec = FieldColumnSpecBuilder.spec(spec);

        // Override the placeholder sequence generator with the real one if needed
        if (!(databaseProvider instanceof PlaceHolderDatabaseProvider)
                && fieldColumnSpec.column() instanceof ColumnSpec(
                String name,
                ColumnValueGenerator generator,
                String joinColumn,
                TableMapping mappedTable
        ) && generator instanceof PlaceholderSequenceColumnValueGenerator placeholderGenerator) {
            final SequenceColumnValueGenerator resolvedSequenceGenerator = databaseProvider.getSequenceColumnValueGenerator(placeholderGenerator.sequence());
            final ColumnSpec resolvedColumnSpec = new ColumnSpec(name, resolvedSequenceGenerator, joinColumn, mappedTable);
            this.fieldColumnMap.put(fieldColumnSpec.field(), resolvedColumnSpec);
        } else {
            this.fieldColumnMap.put(fieldColumnSpec.field(), fieldColumnSpec.column());
        }

        return this;
    }
}

package org.litebridgedb.orm.api.register;

import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.db.spi.generator.ColumnValueGenerator;
import org.litebridgedb.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridgedb.orm.api.spec.ColumnMapping;
import org.litebridgedb.orm.api.spec.ColumnSpec;
import org.litebridgedb.orm.api.spec.DtoTableSpec;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.api.spec.FieldMapping;
import org.litebridgedb.orm.api.spec.TableMapping;
import org.litebridgedb.orm.api.spec.TableSpec;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Abstract base class for defining type-safe mappings between Data Transfer Objects (DTOs)
 * and their corresponding database tables.
 * <p>
 * Subclasses are expected to provide concrete implementations for the table name and associated DTO class, along
 * with {@code public static final} constants containing {@link FieldColumnSpec} instances representing the field-to-column mappings.
 * <p>
 * The {@code TypeSafeDtoTableMapping} class facilitates the creation of mappings between fields in the DTOs
 * and columns in the database. It provides utility methods to define {@code FieldColumnSpec} and creates
 * a specification that links the DTO and the corresponding database table structure.
 */
public abstract class TypeSafeDtoTableMapping {

    protected abstract String table();

    protected abstract Class<?> dtoClass();

    /**
     * Convenience method that configures and creates a {@link FieldColumnSpec} by applying a mapping function that specifies
     * the steps for defining the association between a field in a Data Transfer Object (DTO) and a
     * corresponding database column.
     *
     * @param rc A function that receives a {@link FieldColumnSpecBuilder} as input and returns a
     *           {@link FieldColumnSpecBuilderTerminal}, representing the terminal step in the
     *           configuration process for the field-column mapping.
     * @return A {@link FieldColumnSpec} representing the finalized mapping between the specified DTO
     * field and its corresponding database column, ready for use in ORM and persistence operations.
     */
    public static FieldColumnSpec field(Function<FieldColumnSpecBuilder, FieldColumnSpecBuilderTerminal> rc) {
        return FieldColumnSpecBuilder.spec(rc);
    }

    /**
     * Creates a {@link DtoTableSpec} that defines the mapping between a Data Transfer Object (DTO) class and its
     * corresponding database table. This mapping includes field-to-column specifications and applies any necessary
     * transformations on column value generators.
     *
     * @param databaseProvider the {@link DatabaseProvider} used to resolve database-specific configurations, such as
     *                         sequence generators for placeholder columns; must not be null.
     * @return a {@link DtoTableSpec} instance containing the DTO-to-table mapping details, including resolved field
     * to column mappings ready for use in database operations.
     */
    public DtoTableSpec createDtoTableSpec(final DatabaseProvider databaseProvider) {
        final List<FieldColumnSpec> fieldColumnSpecs = getAllStaticFinalFieldsRecursive(this.getClass()).stream()
                .filter(field -> FieldColumnSpec.class.equals(field.getType()))
                .map(field -> {
                    try {
                        return (FieldColumnSpec) field.get(null);
                    } catch (IllegalAccessException ex) {
                        throw new IllegalArgumentException("Failed to load static final FieldColumnSpecs", ex);
                    }
                })
                .toList();

        final Map<FieldMapping, ColumnMapping> fieldColumnMap = fieldColumnSpecs.stream()
                .map(fieldColumnSpec -> {
                    // Override the placeholder sequence generator with the real one if needed
                    if (fieldColumnSpec.column() instanceof ColumnSpec(
                            String name,
                            ColumnValueGenerator generator,
                            String joinColumn,
                            TableMapping mappedTable
                    ) && generator instanceof PlaceholderSequenceColumnValueGenerator placeholderGenerator) {
                        final SequenceColumnValueGenerator resolvedSequenceGenerator = databaseProvider.getSequenceColumnValueGenerator(placeholderGenerator.sequence());
                        final ColumnSpec resolvedColumnSpec = new ColumnSpec(name, resolvedSequenceGenerator, joinColumn, mappedTable);
                        return new FieldColumnSpec(fieldColumnSpec.field(), resolvedColumnSpec);
                    } else {
                        return fieldColumnSpec;
                    }
                })
                .collect(Collectors.toMap(FieldColumnSpec::field, FieldColumnSpec::column));

        final TableSpec tableSpec = new TableSpec(table(), fieldColumnMap);
        return new DtoTableSpec(dtoClass(), tableSpec);
    }

    private List<Field> getAllStaticFinalFieldsRecursive(Class<?> clazz) {
        final List<Field> staticFields = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != TypeSafeDtoTableMapping.class) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())
                        && Modifier.isFinal(field.getModifiers())
                        && field.canAccess(null)) {
                    staticFields.add(field);
                }
            }

            current = current.getSuperclass();
        }

        return staticFields;
    }
}

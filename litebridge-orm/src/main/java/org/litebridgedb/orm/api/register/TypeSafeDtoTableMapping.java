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

public abstract class TypeSafeDtoTableMapping {

    protected abstract String table();

    protected abstract Class<?> dtoClass();

    public static FieldColumnSpec field(Function<FieldColumnSpecBuilder, FieldColumnSpecBuilderTerminal> rc) {
        return FieldColumnSpecBuilder.spec(rc);
    }

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

    public List<Field> getAllStaticFinalFieldsRecursive(Class<?> clazz) {
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

package org.litebridgedb.orm.persistence.register;

import org.litebridgedb.commons.ClassUtils;
import org.litebridgedb.commons.StringUtils;
import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.db.spi.generator.ColumnValueGenerator;
import org.litebridgedb.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridgedb.orm.annotation.AllowInterface;
import org.litebridgedb.orm.annotation.Column;
import org.litebridgedb.orm.annotation.ManyToMany;
import org.litebridgedb.orm.annotation.OneToMany;
import org.litebridgedb.orm.annotation.Table;
import org.litebridgedb.orm.api.register.ManyToManyBuilder;
import org.litebridgedb.orm.api.register.OneToManyBuilder;
import org.litebridgedb.orm.api.spec.ColumnMapping;
import org.litebridgedb.orm.api.spec.ColumnSpec;
import org.litebridgedb.orm.api.spec.DtoTableSpec;
import org.litebridgedb.orm.api.spec.FieldMapping;
import org.litebridgedb.orm.api.spec.FieldSpec;
import org.litebridgedb.orm.api.spec.TableSpec;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AnnotationMapper {

    private AnnotationMapper() {
    }

    public static DtoTableSpec createDtoTableSpec(final Class<?> entityClass, final DatabaseProvider databaseProvider, final MethodHandles.Lookup lookup) {
        // Map table
        final Table tableAnnotation = entityClass.getAnnotation(Table.class);

        if (tableAnnotation == null) {
            throw new IllegalArgumentException("Class " + entityClass.getName() + " is not annotated with @Table");
        }

        // Map supported superclasses/interfaces for entity when dealing with collections
        final AllowInterface allowInterface = entityClass.getAnnotation(AllowInterface.class);
        final List<Class<?>> entityInterfaces;

        if (allowInterface != null) {
            entityInterfaces = List.of(allowInterface.value());
        } else {
            entityInterfaces = Collections.emptyList();
        }

        final Map<FieldMapping, ColumnMapping> fieldColumnMap = new LinkedHashMap<>();
        final Set<String> mappedFieldNames = new HashSet<>();;

        // Maps annotated fields to column specifications
        ClassUtils.getAllFields(entityClass, false, lookup)
                .forEach(field -> {
                    final ColumnMapping columnMapping;

                    if (field.getAnnotation(Column.class) != null) {
                        final Column columnAnnotation = field.getAnnotation(Column.class);
                        columnMapping = createColumnMapping(columnAnnotation, databaseProvider);
                    } else if (field.getAnnotation(OneToMany.class) != null) {
                        final OneToMany oneToManyAnnotation = field.getAnnotation(OneToMany.class);
                        columnMapping = new OneToManyBuilder().mappedByField(oneToManyAnnotation.mappedByField());
                    } else if (field.getAnnotation(ManyToMany.class) != null) {
                        final ManyToMany manyToManyAnnotation = field.getAnnotation(ManyToMany.class);
                        columnMapping = new ManyToManyBuilder()
                                .joinTable(manyToManyAnnotation.joinTable())
                                .joinColumn(manyToManyAnnotation.joinColumn())
                                .inverseJoinColumn(manyToManyAnnotation.inverseJoinColumn());
                    } else {
                        // Skip non-annotated fields
                        return;
                    }

                    fieldColumnMap.put(new FieldSpec(field.getName(), false), columnMapping);
                    mappedFieldNames.add(field.getName());
                });

        // Maps annotated methods to column specifications for table binding
        ClassUtils.getAllMethods(entityClass, false, lookup)
                .forEach(method -> {
                    final ColumnMapping columnMapping;

                    if (method.getAnnotation(Column.class) != null) {
                        final Column columnAnnotation = method.getAnnotation(Column.class);
                        columnMapping = createColumnMapping(columnAnnotation, databaseProvider);
                    } else if (method.getAnnotation(OneToMany.class) != null) {
                        final OneToMany oneToManyAnnotation = method.getAnnotation(OneToMany.class);
                        columnMapping = new OneToManyBuilder().mappedByField(oneToManyAnnotation.mappedByField());
                    } else if (method.getAnnotation(ManyToMany.class) != null) {
                        final ManyToMany manyToManyAnnotation = method.getAnnotation(ManyToMany.class);
                        columnMapping = new ManyToManyBuilder()
                                .joinTable(manyToManyAnnotation.joinTable())
                                .joinColumn(manyToManyAnnotation.joinColumn())
                                .inverseJoinColumn(manyToManyAnnotation.inverseJoinColumn());
                    } else {
                        // Skip non-annotated methods
                        return;
                    }

                    // Find the target field for this getter method
                    if (method.getParameterCount() != 0) {
                        throw new IllegalArgumentException("Method " + method.getName() + " is not a field accessor method as it has parameters");
                    }

                    // Support both classic property get methods (getXXX) and record-style accessors
                    final String fieldName;

                    if (method.getName().startsWith("get") && method.getName().length() > 3) {
                        final String getterFieldNamePart = method.getName().substring(3);
                        fieldName = Character.toLowerCase(getterFieldNamePart.charAt(0)) + getterFieldNamePart.substring(1);
                    } else {
                        fieldName = method.getName();
                    }

                    if (!mappedFieldNames.contains(fieldName)) {
                        fieldColumnMap.put(new FieldSpec(fieldName, true), columnMapping);
                        mappedFieldNames.add(fieldName);
                    }
                });

        if (fieldColumnMap.isEmpty()) {
            throw new IllegalArgumentException("Class " + entityClass.getName() + " has no annotated fields or methods");
        }

        return new DtoTableSpec(entityClass, new TableSpec(tableAnnotation.value(), fieldColumnMap), entityInterfaces);
    }

    private static ColumnMapping createColumnMapping(final Column columnAnnotation, final DatabaseProvider databaseProvider) {
        final ColumnMapping columnMapping;

        if (columnAnnotation.joinUsing()) {
            columnMapping = new ColumnSpec(columnAnnotation.value(), null, columnAnnotation.value());
        } else if (!StringUtils.isBlank(columnAnnotation.joinOn())) {
            columnMapping = new ColumnSpec(columnAnnotation.value(), null, columnAnnotation.joinOn());
        } else if (!StringUtils.isBlank(columnAnnotation.generateUsingSequence())) {
            final SequenceColumnValueGenerator generator = databaseProvider.getSequenceColumnValueGenerator(columnAnnotation.generateUsingSequence());
            columnMapping = new ColumnSpec(columnAnnotation.value(), generator);
        } else if (columnAnnotation.generator() != ColumnValueGenerator.class) {
            try {
                // Instantiate the generator class
                final ColumnValueGenerator generatorInstance = columnAnnotation.generator().getDeclaredConstructor().newInstance();
                columnMapping = new ColumnSpec(columnAnnotation.value(), generatorInstance);
            } catch (final Exception ex) {
                throw new IllegalArgumentException("Failed to instantiate lhs value generator class: " + columnAnnotation.generator(), ex);
            }
        } else {
            columnMapping = new ColumnSpec(columnAnnotation.value());
        }

        return columnMapping;
    }
}

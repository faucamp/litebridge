package org.litebridgedb.maven.reverse;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.CollectionUtils;
import org.litebridgedb.commons.StringUtils;
import org.litebridgedb.convert.DefaultTypeConverter;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.ForeignKeyConstraint;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.convert.TypeConverter;
import org.litebridgedb.maven.config.ColumnMappingConfig;
import org.litebridgedb.maven.config.OutputConfig;
import org.litebridgedb.maven.config.SqlTypeMappingConfig;
import org.litebridgedb.maven.config.TableMappingConfig;
import org.litebridgedb.maven.util.MojoStringUtils;
import org.litebridgedb.orm.annotation.OneToMany;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entity generator utility class.
 * <p>
 * A utility class responsible for generating Java entity classes based on database table metadata.
 * The entities are created using the provided SQL type mappings and table mappings configurations.
 * Additional configurations include the output settings for the generated source files and logging.
 */
public final class EntityGenerator {

    private final @Nullable List<SqlTypeMappingConfig> sqlTypeMappings;
    private final @Nullable List<TableMappingConfig> tableMappings;
    private final OutputConfig output;
    private final Log log;
    private final TypeConverter typeConverter = new DefaultTypeConverter();

    public EntityGenerator(@Nullable final List<SqlTypeMappingConfig> sqlTypeMappings,
                           @Nullable final List<TableMappingConfig> tableMappings,
                           final OutputConfig output,
                           final Log log) {
        this.sqlTypeMappings = sqlTypeMappings;
        this.tableMappings = tableMappings;
        this.output = output;
        this.log = log;
    }

    /**
     * Creates a generated entity class for a specified database table based on its metadata.
     *
     * @param tableMetaData    the metadata of the database table for which the entity class is being created
     * @param tableMetaDataMap a map of table names to their corresponding metadata, used for resolving related tables
     * @param entities         a map of table names to generated entity classes, used to cache entities under construction and for cross-referencing
     * @return a {@code GeneratedEntity} representing the generated entity class, including its associated metadata, mappings, and structure
     * @throws MojoExecutionException if any error occurs during the generation of the entity class, such as resolving class types, column mappings, or foreign key relationships
     */
    public GeneratedEntity createEntityClassForTable(final TableMetaData tableMetaData,
                                                     final Map<String, TableMetaData> tableMetaDataMap,
                                                     final Map<String, GeneratedEntity> entities) throws MojoExecutionException {
        final TableMappingConfig tableMappingConfig;

        if (!CollectionUtils.isEmpty(tableMappings)) {
            tableMappingConfig = tableMappings.stream()
                    .filter(t -> t.getTable().equals(tableMetaData.qualifiedName()))
                    .findFirst().orElse(null);
        } else {
            tableMappingConfig = null;
        }

        final String entityClassName;

        if (tableMappingConfig != null && tableMappingConfig.getEntityName() != null) {
            entityClassName = tableMappingConfig.getEntityName();
        } else {
            entityClassName = MojoStringUtils.camelCase(tableMetaData.name(), false);
        }

        // Toggle nullability annotations
        final boolean jspecify = output.getJspecify() != null && output.getJspecify().isAnnotate();

        final CompilationUnit entity = new CompilationUnit();
        entity.setPackageDeclaration(output.getOutputPackage())
                .addImport(Objects.class)
                .addImport(org.litebridgedb.orm.annotation.Column.class)
                .addImport(org.litebridgedb.orm.annotation.Table.class);
        final ClassOrInterfaceDeclaration entityClass = entity.addClass(entityClassName)
                .setPublic(true)
                .setFinal(output.isFinalClasses());

        // Annotate class with @NullMarked/@NullUnmarked to allow nullability checks if package-info does not exist
        if (jspecify && !output.isPackageInfo()) {
            if (output.getJspecify().isNullMarked()) {
                entityClass.addMarkerAnnotation(NullMarked.class);
            } else {
                entityClass.addMarkerAnnotation(NullUnmarked.class);
            }
        }

        // Add @Table annotation
        entityClass.addSingleMemberAnnotation(org.litebridgedb.orm.annotation.Table.class.getSimpleName(),
                "\"%s\"".formatted(tableMetaData.qualifiedName()));

        if (output.isJavadoc()) {
            entityClass.setJavadocComment("Entity class for table: {@code %s}".formatted(tableMetaData.qualifiedName()));
        }

        // Create fields for columns
        final Map<Column, String> columnfieldMap = new HashMap<>(tableMetaData.columns().size());
        final List<FieldInfo> declaredFields = new ArrayList<>(tableMetaData.columns().size());
        final List<FieldInfo> appendFields = new ArrayList<>();
        final Set<Class<?>> additionalImports = new HashSet<>();

        // Cache the entity class under construction
        entities.put(tableMetaData.qualifiedName(), new GeneratedEntity(entity, tableMetaData.qualifiedName(), entityClassName, columnfieldMap));

        for (ColumnMetaData columnMetaData : tableMetaData.columns()) {
            // Determine data for the entity field
            final ColumnMappingConfig columnMappingConfig;
            final String fieldName;
            Class<?> fieldClass = null;

            if (tableMappingConfig != null && tableMappingConfig.getColumnMappings() != null) {
                columnMappingConfig = tableMappingConfig.getColumnMappings().stream()
                        .filter(c -> c.getColumn().equals(columnMetaData.name()))
                        .findFirst().orElse(null);

                if (columnMappingConfig != null) {
                    if (columnMappingConfig.getFieldType() != null) {
                        try {
                            fieldClass = PrimitiveLookup.getPrimitiveClass(columnMappingConfig.getFieldType());
                        } catch (ClassNotFoundException ex) {
                            throw new MojoExecutionException("Failed to load field type class '%s' for column mapping: %s".formatted(columnMappingConfig.getFieldType(), columnMetaData.name()));
                        }
                    }

                    if (columnMappingConfig.getFieldName() != null) {
                        fieldName = columnMappingConfig.getFieldName();
                    } else {
                        fieldName = MojoStringUtils.camelCase(columnMetaData.name(), true);
                    }
                } else {
                    fieldName = MojoStringUtils.camelCase(columnMetaData.name(), true);
                }
            } else {
                columnMappingConfig = null;
                fieldName = MojoStringUtils.camelCase(columnMetaData.name(), true);
            }

            columnfieldMap.put(columnMetaData.toColumn(), fieldName);

            // Determine field type
            String fieldClassType = null;
            String joinOn = null;
            String reverseMappingCollectionType = null;
            String oneToManyMappedByField = null;

            if (fieldClass == null) {
                final Optional<SqlTypeMappingConfig> sqlTypeMappingConfig = sqlTypeMappings == null ? Optional.empty() : sqlTypeMappings.stream()
                        .filter(m -> m.getJdbcType().getVendorTypeNumber().equals(columnMetaData.getDataType()))
                        .filter(m -> m.getPrecision() == null || m.getPrecision().equals(columnMetaData.getSize()))
                        .filter(m -> m.getNotNull() == null || m.getNotNull().equals(!columnMetaData.isNullable()))
                        .reduce((a, b) -> {
                            if (a.getPrecision() != null) {
                                if (b.getPrecision() != null) {
                                    if (a.getNotNull() != null) {
                                        return a;
                                    }

                                    return b;
                                }

                                return a;
                            }

                            if (b.getPrecision() != null) {
                                return b;
                            }

                            if (b.getNotNull() != null) {
                                if (a.getNotNull() != null) {
                                    return a;
                                }

                                return b;
                            }

                            return a;
                        });

                if (sqlTypeMappingConfig.isPresent()) {
                    final SqlTypeMappingConfig sqlTypeMapping = sqlTypeMappingConfig.get();

                    try {
                        fieldClass = PrimitiveLookup.getPrimitiveClass(sqlTypeMapping.getFieldType());
                    } catch (ClassNotFoundException ex) {
                        fieldClassType = sqlTypeMapping.getFieldType();
                        log.warn("Could not find class for specified field type '%s' for column: %s".formatted(fieldClassType, columnMetaData.name()));
                    }
                } else {
                    final Class<?> convertedType = typeConverter.getClassForSqlType(columnMetaData.getDataType());
                    fieldClass = PrimitiveLookup.getPrimitiveClass(convertedType);
                }
            }

            // Check if the field points to a related entity, and update the field type accordingly
            for (ForeignKeyConstraint foreignRef : columnMetaData.getForeignReferences()) {
                // Check if an entity for this reference exists
                final Column remoteColumn = foreignRef.foreignKey();
                final String remoteTableName = remoteColumn.table().qualifiedName();
                GeneratedEntity remoteEntity = entities.get(remoteTableName);

                if (remoteEntity == null && tableMetaDataMap.containsKey(remoteTableName)) {
                    remoteEntity = createEntityClassForTable(tableMetaDataMap.get(remoteTableName), tableMetaDataMap, entities);
                }

                if (remoteEntity != null) {
                    // Related entity; find the remote field for the joinOn attribute
                    final String remoteFieldName = remoteEntity.columnfieldMap().get(foreignRef.foreignKey());

                    if (remoteFieldName != null) {
                        // Configure reverse-mapping collection
                        reverseMappingCollectionType = remoteEntity.className();
                        oneToManyMappedByField = remoteFieldName;
                    } else {
                        throw new MojoExecutionException("Could not find 'joinOn' field for column: " + columnMetaData.toColumn());
                    }
                }
            }

            for (ForeignKeyConstraint foreignKeyConstraint : columnMetaData.getForeignKeyConstraints()) {
                // Check if an entity for this reference exists
                final Column remoteColumn = foreignKeyConstraint.foreignKey();
                final String remoteTableName = remoteColumn.table().qualifiedName();
                GeneratedEntity remoteEntity = entities.get(remoteTableName);

                if (remoteEntity == null && tableMetaDataMap.containsKey(remoteTableName)) {
                    remoteEntity = createEntityClassForTable(tableMetaDataMap.get(remoteTableName), tableMetaDataMap, entities);
                }

                if (remoteEntity != null) {
                    fieldClass = null;
                    fieldClassType = remoteEntity.className();
                    joinOn = remoteColumn.name();

                    if (log.isDebugEnabled()) {
                        log.debug("Overriding field type for column %s to: %s".formatted(columnMetaData.name(), fieldClassType));
                    }
                } else {
                    log.warn("Could not find related entity for column %s.%s for remote table: %s; skipping related field resolution".formatted(tableMetaData.name(), columnMetaData.name(), remoteTableName));
                }
            }

            // Create entity field
            final FieldDeclaration field;

            if (fieldClass != null) {
                field = entityClass.addField(
                        fieldClass,
                        fieldName,
                        Modifier.Keyword.PRIVATE);
            } else {
                field = entityClass.addField(
                        fieldClassType,
                        fieldName,
                        Modifier.Keyword.PRIVATE);
            }

            // Annotate field for nullability with JSpecify if applicable
            if (jspecify
                    && output.getJspecify().isNullMarked()
                    && (!output.getJspecify().isDatabaseNullable() || columnMetaData.isNullable())
                    && (fieldClass == null || !fieldClass.isPrimitive())) {
                field.addMarkerAnnotation(Nullable.class);
            }

            // Set @Column annotation
            field.addAnnotation(createColumnAnnotation(columnMetaData, joinOn, columnMappingConfig));

            if (output.isJavadoc()) {
                StringBuilder comment = new StringBuilder("Column: {@code ").append(columnMetaData.name()).append('}');
                final JDBCType jdbcType = JDBCType.valueOf(columnMetaData.getDataType());
                comment.append("\n\nType: {@code ").append(jdbcType.getName()).append('}');

                if (columnMetaData.getSize() > 0) {
                    comment.append(", size: ").append(columnMetaData.getSize());
                }

                if (columnMetaData.isNullable()) {
                    comment.append(", nullable");
                } else {
                    comment.append(", not nullable");
                }

                field.setJavadocComment(comment.toString());
            }

            declaredFields.add(new FieldInfo(field, columnMetaData.isNullable()));

            // Create reverse mapping collection field
            if (reverseMappingCollectionType != null) {
                final Type listType = StaticJavaParser.parseType("List<%s>".formatted(reverseMappingCollectionType));
                additionalImports.add(List.class);
                additionalImports.add(OneToMany.class);
                final String reverseMappingCollectionName = "%ss".formatted(MojoStringUtils.camelCase(reverseMappingCollectionType, true));

                final FieldDeclaration reverseMappingCollectionField = new FieldDeclaration()
                        .setModifiers(Modifier.Keyword.PRIVATE)
                        .addVariable(new VariableDeclarator(listType, reverseMappingCollectionName));

                if (jspecify && output.getJspecify().isNullMarked()) {
                    reverseMappingCollectionField.addMarkerAnnotation(Nullable.class);
                }

                reverseMappingCollectionField.addAnnotation(createOneToManyAnnotation(oneToManyMappedByField));

                if (output.isJavadoc()) {
                    reverseMappingCollectionField.setJavadocComment("Reverse mapping for {@code %s.%s}".formatted(reverseMappingCollectionType, oneToManyMappedByField));
                }

                appendFields.add(new FieldInfo(reverseMappingCollectionField, true));
            }
        }

        // Add getters/setters
        final List<String> fieldNames = new ArrayList<>(declaredFields.size());

        // Add reverse collection fields
        appendFields.stream().map(FieldInfo::field).forEach(entityClass::addMember);

        // Add getters/setters
        declaredFields.forEach(fieldInfo -> {
            fieldNames.add(fieldInfo.field().getVariable(0).getNameAsString());
            createGettersAndSetters(fieldInfo);
        });

        appendFields.forEach(EntityGenerator::createGettersAndSetters);

        // Add equals(), hashcodde() and toString()
        entityClass.addMember(createEquals(entityClassName, fieldNames));
        entityClass.addMember(createHashCode(fieldNames));
        entityClass.addMember(createToString(entityClassName, fieldNames));

        // Finalise imports
        additionalImports.forEach(entity::addImport);

        return new GeneratedEntity(entity, tableMetaData.name(), entityClassName, columnfieldMap);
    }

    private static void createGettersAndSetters(final FieldInfo fieldInfo) {
        final MethodDeclaration getter = fieldInfo.field().createGetter();
        final MethodDeclaration setter = fieldInfo.field().createSetter();
        final Parameter setterParameter = setter.getParameter(0);
        setterParameter.setModifier(Modifier.Keyword.FINAL, true);

        if (fieldInfo.field().isAnnotationPresent(Nullable.class)) {
            getter.addMarkerAnnotation(Nullable.class);

            if (fieldInfo.columnNullable()) {
                setterParameter.addMarkerAnnotation(Nullable.class);
            }
        }
    }

    /**
     * Creates a toString method for the specified entity class.
     *
     * @param className the name of the entity class
     * @param fields    the list of field names to include in the toString output
     * @return the generated toString method declaration
     */
    private static MethodDeclaration createToString(final String className, final List<String> fields) {
        // Build the string concatenation expression for the return statement
        // Example output format: "User{id=" + id + ", name='" + name + "', active=" + active + "}"
        final String returnExpression = fields.stream()
                .map(field -> "\"" + field + "=\" + " + field)
                .collect(Collectors.joining(" + \", \" + ", "\"" + className + "{\" + ", " + \"}\""));

        // Create the toString method declaration
        final MethodDeclaration toStringMethod = new MethodDeclaration()
                .setModifiers(Modifier.Keyword.PUBLIC)
                .setType(String.class)
                .setName("toString")
                .addAnnotation(new MarkerAnnotationExpr("Override"));

        // Define the method body with the generated return statement
        BlockStmt body = new BlockStmt();
        body.addStatement("return " + returnExpression + ";");
        toStringMethod.setBody(body);

        return toStringMethod;
    }

    /**
     * Creates an equals method for the specified entity class.
     *
     * @param className the name of the entity class
     * @param fields    the list of field names to include in the equals comparison
     * @return the generated equals method declaration
     */
    private static MethodDeclaration createEquals(final String className, final List<String> fields) {
        // Build the basic method signature: public boolean equals(Object obj)
        MethodDeclaration equalsMethod = new MethodDeclaration()
                .setModifiers(Modifier.Keyword.PUBLIC)
                .setType("boolean")
                .setName("equals")
                .addParameter(new Parameter(StaticJavaParser.parseType("Object"), "obj")
                        .setModifier(Modifier.Keyword.FINAL, true))
                .addAnnotation(new MarkerAnnotationExpr("Override"));

        // Build the inner logic block using template strings
        StringBuilder body = new StringBuilder("{\n");
        body.append("    if (this == obj) return true;\n");
        body.append("    if (obj == null || getClass() != obj.getClass()) return false;\n");
        body.append(String.format("    final %s other = (%s) obj;\n", className, className));

        // Use Objects.equals for safe null/primitive checks
        String comparisons = fields.stream()
                .map(field -> String.format("Objects.equals(%s, other.%s)", field, field))
                .collect(Collectors.joining("\n        && "));

        body.append("    return ").append(comparisons).append(";\n");
        body.append("}");

        equalsMethod.setBody(StaticJavaParser.parseBlock(body.toString()));
        return equalsMethod;
    }

    /**
     * Creates a hashCode method for the specified entity class.
     *
     * @param fields the list of field names to include in the hashCode calculation
     * @return the generated hashCode method declaration
     */
    private static MethodDeclaration createHashCode(List<String> fields) {
        // Build basic method signature: public int hashCode()
        MethodDeclaration hashCodeMethod = new MethodDeclaration()
                .setModifiers(Modifier.Keyword.PUBLIC)
                .setType("int")
                .setName("hashCode")
                .addAnnotation(new MarkerAnnotationExpr("Override"));

        // Use java.util.Objects.hash() for a clean builder approach
        String fieldsCsv = String.join(", ", fields);
        String body = String.format("{\n    return Objects.hash(%s);\n}", fieldsCsv);

        hashCodeMethod.setBody(StaticJavaParser.parseBlock(body));
        return hashCodeMethod;
    }

    /**
     * Creates a column annotation for the specified column metadata.
     *
     * @param columnMetaData      the metadata of the column for which the annotation is being created
     * @param joinOn              remote join column for this column, if any
     * @param columnMappingConfig the configuration for mapping the column, if any
     * @return the generated column annotation expression
     */
    private AnnotationExpr createColumnAnnotation(final ColumnMetaData columnMetaData, final @Nullable String joinOn, final @Nullable ColumnMappingConfig columnMappingConfig) {
        final NormalAnnotationExpr annotation = new NormalAnnotationExpr();
        annotation.setName(new Name(org.litebridgedb.orm.annotation.Column.class.getSimpleName()));
        annotation.addPair("value", "\"%s\"".formatted(columnMetaData.name()));

        if (columnMappingConfig == null) {
            return annotation;
        }

        if (!StringUtils.isBlank(columnMappingConfig.getGenerateUsingSequence())) {
            annotation.addPair("generateUsingSequence", "\"%s\"".formatted(columnMappingConfig.getGenerateUsingSequence()));
        }

        if (!StringUtils.isBlank(columnMappingConfig.getGeneratorClass())) {
            annotation.addPair("generator", columnMappingConfig.getGeneratorClass());
        }

        if (joinOn != null) {
            annotation.addPair("joinOn", "\"%s\"".formatted(joinOn));
        }

        return annotation;
    }

    /**
     * Creates a one-to-sssssssmany annotation for the specified mapped-by field.
     *
     * @param mappedByField the name of the field in the related entity that maps to this entity
     * @return the generated one-to-many annotation expression
     */
    private AnnotationExpr createOneToManyAnnotation(final String mappedByField) {
        final NormalAnnotationExpr annotation = new NormalAnnotationExpr();
        annotation.setName(new Name(org.litebridgedb.orm.annotation.OneToMany.class.getSimpleName()));
        annotation.addPair("mappedByField", "\"%s\"".formatted(mappedByField));
        return annotation;
    }

    private record FieldInfo(FieldDeclaration field, boolean columnNullable) {
    }
}

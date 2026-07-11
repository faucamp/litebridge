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
import org.litebridgedb.maven.config.reverse.ColumnMappingConfig;
import org.litebridgedb.maven.config.reverse.RevEngOutputConfig;
import org.litebridgedb.maven.config.reverse.SqlTypeMappingConfig;
import org.litebridgedb.maven.config.reverse.TableMappingConfig;
import org.litebridgedb.maven.util.MojoStringUtils;
import org.litebridgedb.orm.annotation.AllowInterface;
import org.litebridgedb.orm.annotation.ManyToMany;
import org.litebridgedb.orm.annotation.OneToMany;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    private final RevEngOutputConfig output;
    private final Log log;
    private final TypeConverter typeConverter = new DefaultTypeConverter();

    public EntityGenerator(@Nullable final List<SqlTypeMappingConfig> sqlTypeMappings,
                           @Nullable final List<TableMappingConfig> tableMappings,
                           final RevEngOutputConfig output,
                           final Log log) {
        this.sqlTypeMappings = sqlTypeMappings;
        this.tableMappings = tableMappings;
        this.output = output;
        this.log = log;
    }

    /**
     * Creates a generated entity class for a specified database table based on its metadata.
     *
     * @param tableMetaData      the metadata of the database table for which the entity class is being created
     * @param tableMetaDataMap   a map of table names to their corresponding metadata, used for resolving related tables
     * @param manyToManyMappings a list of many-to-many mappings, used for resolving related tables
     * @param entities           a map of table names to generated entity classes, used to cache entities under construction and for cross-referencing
     * @return a {@code GeneratedEntity} representing the generated entity class, including its associated metadata, mappings, and structure
     * @throws MojoExecutionException if any error occurs during the generation of the entity class, such as resolving class types, column mappings, or foreign key relationships
     */
    public GeneratedEntity createEntityClassForTable(final TableMetaData tableMetaData,
                                                     final Map<String, TableMetaData> tableMetaDataMap,
                                                     final List<ManyToManyMapping> manyToManyMappings,
                                                     final Map<String, GeneratedEntity> entities) throws MojoExecutionException {
        final @Nullable TableMappingConfig tableMappingConfig = getTableMappingConfig(tableMetaData);
        final String entityClassName = createEntityClassName(tableMetaData, tableMappingConfig);

        // Toggle nullability annotations
        final boolean jspecify = output.getJspecify() != null && output.getJspecify().isAnnotate();

        // Create class
        final CompilationUnitAndClass cuClass = createCompilationUnitAndClass(tableMetaData, tableMappingConfig, entityClassName, jspecify);
        final CompilationUnit entity = cuClass.entity();
        final ClassOrInterfaceDeclaration entityClass = cuClass.entityClass();

        // Create fields for columns
        final Map<Column, String> columnfieldMap = new HashMap<>(tableMetaData.columns().size());
        final List<FieldInfo> declaredFields = new ArrayList<>(tableMetaData.columns().size());
        final List<FieldInfo> appendFields = new ArrayList<>();

        // Cache the entity class under construction
        entities.put(tableMetaData.qualifiedName(), new GeneratedEntity(entity, tableMetaData.qualifiedName(), entityClassName, columnfieldMap));

        for (ColumnMetaData columnMetaData : tableMetaData.columns()) {
            // Get config and create field-column tracking link
            final ColumnMappingConfig columnMappingConfig = getColumnMappingConfig(columnMetaData, tableMappingConfig);
            final String fieldName = createFieldName(columnMetaData, columnMappingConfig);
            columnfieldMap.put(columnMetaData.toColumn(), fieldName);

            // Determine field type
            final FieldClassInfo fieldClassInfo = createFieldClassInfo(columnMetaData, columnMappingConfig);

            // Check if the field points to a related entity, and update the field type accordingly
            final JoinOnInfo joinOnInfo = createJoinOnInfo(columnMetaData, tableMetaDataMap, manyToManyMappings, entities);

            // Create entity field
            final FieldDeclaration field = createFieldDeclaration(fieldName, fieldClassInfo, joinOnInfo, entityClass, columnMetaData, columnMappingConfig, jspecify);
            declaredFields.add(new FieldInfo(field, columnMetaData.isNullable()));

            // Many-to-many relationships
            final List<ManyToManySpec> manyToManySpecs = manyToManyMappings.stream()
                    .filter(manyToManyMapping -> manyToManyMapping.leftColumn().equals(columnMetaData)
                            || manyToManyMapping.rightColumn().equals(columnMetaData))
                    .map(manyToManyMapping -> createManyToManySpec(columnMetaData, manyToManyMapping))
                    .toList();
            appendFields.addAll(createManyToManyFieldInfos(manyToManySpecs, entity, jspecify));

            // Create one-to-many reverse mapping collection fields
            final List<OneToManySpec> oneToManySpecs = createOneToManyMappings(columnMetaData, tableMetaDataMap, manyToManyMappings, entities);
            createReverseCollectionFieldInfos(oneToManySpecs, entity, jspecify).forEach(fieldInfo -> {
                final String collectionFieldName = fieldInfo.field().getVariable(0).getNameAsString();

                if (appendFields.stream().noneMatch(appendField -> collectionFieldName.equals(appendField.field().getVariable(0).getNameAsString()))) {
                    appendFields.add(fieldInfo);
                }
            });

        }

        // Add getters/setters
        final List<String> fieldNames = new ArrayList<>(declaredFields.size());

        // Add one-to-many reverse collection fields and many-to-many collections
        appendFields.stream().map(FieldInfo::field).forEach(cuClass.entityClass()::addMember);

        // Add getters/setters
        declaredFields.forEach(fieldInfo -> {
            fieldNames.add(fieldInfo.field().getVariable(0).getNameAsString());
            createGettersAndSetters(fieldInfo);
        });

        appendFields.forEach(EntityGenerator::createGettersAndSetters);

        // Add equals(), hashcodde() and toString()
        cuClass.entityClass().addMember(createEquals(entityClassName, fieldNames));
        cuClass.entityClass().addMember(createHashCode(fieldNames));
        cuClass.entityClass().addMember(createToString(entityClassName, fieldNames));

        return new GeneratedEntity(cuClass.entity(), tableMetaData.name(), entityClassName, columnfieldMap);
    }

    private FieldDeclaration createFieldDeclaration(final String fieldName,
                                                    final FieldClassInfo fieldClassInfo,
                                                    final @Nullable JoinOnInfo joinOnInfo,
                                                    final ClassOrInterfaceDeclaration entityClass,
                                                    final ColumnMetaData columnMetaData,
                                                    final @Nullable ColumnMappingConfig columnMappingConfig,
                                                    final boolean jspecify) {
        final Class<?> fieldClass = fieldClassInfo.fieldClass() != null && joinOnInfo == null ? fieldClassInfo.fieldClass() : null;
        final FieldDeclaration field;

        if (fieldClass != null) {
            field = entityClass.addField(
                    fieldClass,
                    fieldName,
                    Modifier.Keyword.PRIVATE);
        } else {
            final String fieldClassName = joinOnInfo != null ? joinOnInfo.fieldClassType() : fieldClassInfo.fieldClassName();
            field = entityClass.addField(
                    fieldClassName,
                    fieldName,
                    Modifier.Keyword.PRIVATE);
        }

        // Annotate field for nullability with JSpecify if applicable
        //noinspection DataFlowIssue
        if (jspecify
                && output.getJspecify().isNullMarked()
                && (!output.getJspecify().isDatabaseNullable() || columnMetaData.isNullable())
                && (fieldClass == null || !fieldClass.isPrimitive())) {
            field.addMarkerAnnotation(Nullable.class);
        }

        // Set @Column annotation
        field.addAnnotation(createColumnAnnotation(columnMetaData, joinOnInfo, columnMappingConfig));

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

        return field;
    }

    private FieldClassInfo createFieldClassInfo(final ColumnMetaData columnMetaData, final @Nullable ColumnMappingConfig columnMappingConfig) throws MojoExecutionException {
        Class<?> fieldClass = null;
        String fieldClassType = null;

        if (columnMappingConfig != null && columnMappingConfig.getFieldType() != null) {
            try {
                fieldClass = PrimitiveLookup.getPrimitiveClass(columnMappingConfig.getFieldType());
            } catch (ClassNotFoundException ex) {
                throw new MojoExecutionException("Failed to load field type class '%s' for column mapping: %s".formatted(columnMappingConfig.getFieldType(), columnMetaData.name()));
            }
        }


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

        return new FieldClassInfo(fieldClass, fieldClassType);
    }

    private static String createFieldName(final ColumnMetaData columnMetaData, final @Nullable ColumnMappingConfig columnMappingConfig) {
        final String fieldName;

        if (columnMappingConfig != null) {
            if (columnMappingConfig.getFieldName() != null) {
                fieldName = columnMappingConfig.getFieldName();
            } else {
                fieldName = MojoStringUtils.camelCase(columnMetaData.name(), true);
            }
        } else {
            fieldName = MojoStringUtils.camelCase(columnMetaData.name(), true);
        }

        return fieldName;
    }

    private CompilationUnitAndClass createCompilationUnitAndClass(final TableMetaData tableMetaData,
                                                                  final @Nullable TableMappingConfig tableMappingConfig,
                                                                  final String entityClassName,
                                                                  final boolean jspecify) {
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
            //noinspection DataFlowIssue
            if (output.getJspecify().isNullMarked()) {
                entityClass.addMarkerAnnotation(NullMarked.class);
            } else {
                entityClass.addMarkerAnnotation(NullUnmarked.class);
            }
        }

        // Add @Table annotation
        entityClass.addSingleMemberAnnotation(org.litebridgedb.orm.annotation.Table.class.getSimpleName(),
                "\"%s\"".formatted(tableMetaData.qualifiedName()));

        // @AllowInterface annotation
        if (tableMappingConfig != null && tableMappingConfig.getAllowInterface() != null) {
            entityClass.addSingleMemberAnnotation(AllowInterface.class, tableMappingConfig.getAllowInterface() + ".class");
        }

        if (output.isJavadoc()) {
            entityClass.setJavadocComment("Entity class for table: {@code %s}".formatted(tableMetaData.qualifiedName()));
        }

        CompilationUnitAndClass cuClass = new CompilationUnitAndClass(entity, entityClass);
        return cuClass;
    }

    private @Nullable TableMappingConfig getTableMappingConfig(final TableMetaData tableMetaData) {
        final TableMappingConfig tableMappingConfig;

        if (!CollectionUtils.isEmpty(tableMappings)) {
            tableMappingConfig = tableMappings.stream()
                    .filter(t -> t.getTable().equals(tableMetaData.qualifiedName()))
                    .findFirst().orElse(null);
        } else {
            tableMappingConfig = null;
        }

        return tableMappingConfig;
    }

    private static String createEntityClassName(final TableMetaData tableMetaData, final @Nullable TableMappingConfig tableMappingConfig) {
        final String entityClassName;

        if (tableMappingConfig != null && tableMappingConfig.getEntityName() != null) {
            entityClassName = tableMappingConfig.getEntityName();
        } else {
            entityClassName = MojoStringUtils.camelCase(tableMetaData.name(), false);
        }

        return entityClassName;
    }

    private ManyToManySpec createManyToManySpec(final ColumnMetaData columnMetaData,
                                                final ManyToManyMapping manyToManyMapping) {
        final boolean lhs = manyToManyMapping.leftColumn().equals(columnMetaData);
        final TableMetaData remoteTable;
        final LinkedHashMap<String, String> pairs = new LinkedHashMap<>();
        pairs.put("joinTable", "\"%s\"".formatted(manyToManyMapping.joinTable().qualifiedName()));

        if (lhs) {
            remoteTable = manyToManyMapping.rightTable();
            pairs.put("joinColumn", "\"%s\"".formatted(manyToManyMapping.leftJoinColumn().name()));
            pairs.put("inverseJoinColumn", "\"%s\"".formatted(manyToManyMapping.rightJoinColumn().name()));
        } else {
            remoteTable = manyToManyMapping.leftTable();
            pairs.put("joinColumn", "\"%s\"".formatted(manyToManyMapping.rightJoinColumn().name()));
            pairs.put("inverseJoinColumn", "\"%s\"".formatted(manyToManyMapping.leftJoinColumn().name()));
        }

        final String remoteEntityClassName = createEntityClassName(remoteTable, getTableMappingConfig(remoteTable));

        return new ManyToManySpec(new AnnotationSpec(ManyToMany.class, pairs), remoteEntityClassName);
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
     * @param joinOnInfo          remote join column for this column, if any
     * @param columnMappingConfig the configuration for mapping the column, if any
     * @return the generated column annotation expression
     */
    private AnnotationExpr createColumnAnnotation(final ColumnMetaData columnMetaData, final @Nullable JoinOnInfo joinOnInfo, final @Nullable ColumnMappingConfig columnMappingConfig) {
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

        if (joinOnInfo != null) {
            annotation.addPair("joinOn", "\"%s\"".formatted(joinOnInfo.joinOn()));
        }

        return annotation;
    }

    private List<OneToManySpec> createOneToManyMappings(final ColumnMetaData columnMetaData,
                                                        final Map<String, TableMetaData> tableMetaDataMap,
                                                        final List<ManyToManyMapping> manyToManyMappings,
                                                        final Map<String, GeneratedEntity> entities) throws MojoExecutionException {
        final List<OneToManySpec> oneToManySpecs = new ArrayList<>();

        for (ForeignKeyConstraint foreignRef : columnMetaData.getForeignReferences()) {
            // Check if an entity for this reference exists
            final Column remoteColumn = foreignRef.foreignKey();
            final String remoteTableName = remoteColumn.table().qualifiedName();
            GeneratedEntity remoteEntity = entities.get(remoteTableName);

            if (remoteEntity == null && tableMetaDataMap.containsKey(remoteTableName)) {
                remoteEntity = createEntityClassForTable(tableMetaDataMap.get(remoteTableName), tableMetaDataMap, manyToManyMappings, entities);
            }

            if (remoteEntity != null) {
                // Related entity; find the remote field for the joinOn attribute
                final String remoteFieldName = remoteEntity.columnfieldMap().get(foreignRef.foreignKey());

                if (remoteFieldName != null) {
                    // Configure reverse-mapping collection
                    oneToManySpecs.add(new OneToManySpec(remoteEntity.className(), remoteFieldName));
                } else {
                    throw new MojoExecutionException("Could not find 'joinOn' field for column: " + columnMetaData.toColumn());
                }
            }
        }

        return oneToManySpecs;
    }

    private List<FieldInfo> createReverseCollectionFieldInfos(final List<OneToManySpec> oneToManySpecs, final CompilationUnit entity, final boolean jspecify) {
        final List<FieldInfo> appendFields = new ArrayList<>();

        for (OneToManySpec oneToManySpec : oneToManySpecs) {
            final Type listType = StaticJavaParser.parseType("List<%s>".formatted(oneToManySpec.reverseMappingCollectionType()));
            entity.addImport(List.class);
            entity.addImport(OneToMany.class);
            final String reverseMappingCollectionName = MojoStringUtils.pluralise(MojoStringUtils.lowerFirst(oneToManySpec.reverseMappingCollectionType()));

            final FieldDeclaration reverseMappingCollectionField = new FieldDeclaration()
                    .setModifiers(Modifier.Keyword.PRIVATE)
                    .addVariable(new VariableDeclarator(listType, reverseMappingCollectionName));

            //noinspection DataFlowIssue
            if (jspecify && output.getJspecify().isNullMarked()) {
                reverseMappingCollectionField.addMarkerAnnotation(Nullable.class);
            }

            reverseMappingCollectionField.addAnnotation(createOneToManyAnnotation(oneToManySpec.mappedByField()));

            if (output.isJavadoc()) {
                reverseMappingCollectionField.setJavadocComment("Reverse mapping for {@code %s.%s}".formatted(oneToManySpec.reverseMappingCollectionType(), oneToManySpec.mappedByField()));
            }

            appendFields.add(new FieldInfo(reverseMappingCollectionField, true));
        }

        return appendFields;
    }

    private List<FieldInfo> createManyToManyFieldInfos(final List<ManyToManySpec> manyToManySpecs, final CompilationUnit entity, final boolean jspecify) {
        final List<FieldInfo> appendFields = new ArrayList<>();

        for (ManyToManySpec manyToManySpec : manyToManySpecs) {
            final Type listType = StaticJavaParser.parseType("List<%s>".formatted(manyToManySpec.remoteEntityClassName()));
            entity.addImport(List.class);
            entity.addImport(ManyToMany.class);
            final String reverseMappingCollectionName = MojoStringUtils.pluralise((MojoStringUtils.lowerFirst(manyToManySpec.remoteEntityClassName())));

            final FieldDeclaration reverseMappingCollectionField = new FieldDeclaration()
                    .setModifiers(Modifier.Keyword.PRIVATE)
                    .addVariable(new VariableDeclarator(listType, reverseMappingCollectionName));

            //noinspection DataFlowIssue
            if (jspecify && output.getJspecify().isNullMarked()) {
                reverseMappingCollectionField.addMarkerAnnotation(Nullable.class);
            }

            reverseMappingCollectionField.addAnnotation(createAnnotation(manyToManySpec.annotationSpec()));

            if (output.isJavadoc()) {
                reverseMappingCollectionField.setJavadocComment("Many-to-many collection to {@link %s}".formatted(manyToManySpec.remoteEntityClassName()));
            }

            appendFields.add(new FieldInfo(reverseMappingCollectionField, true));
        }

        return appendFields;
    }

    private @Nullable JoinOnInfo createJoinOnInfo(final ColumnMetaData columnMetaData,
                                                  final Map<String, TableMetaData> tableMetaDataMap,
                                                  final List<ManyToManyMapping> manyToManyMappings,
                                                  final Map<String, GeneratedEntity> entities) throws MojoExecutionException {
        for (ForeignKeyConstraint foreignKeyConstraint : columnMetaData.getForeignKeyConstraints()) {
            // Check if an entity for this reference exists
            final Column remoteColumn = foreignKeyConstraint.foreignKey();
            final String remoteTableName = remoteColumn.table().qualifiedName();
            GeneratedEntity remoteEntity = entities.get(remoteTableName);

            if (remoteEntity == null && tableMetaDataMap.containsKey(remoteTableName)) {
                remoteEntity = createEntityClassForTable(tableMetaDataMap.get(remoteTableName), tableMetaDataMap, manyToManyMappings, entities);
            }

            if (remoteEntity != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Overriding field type for column %s to: %s".formatted(columnMetaData.name(), remoteEntity.className()));
                }

                return new JoinOnInfo(remoteEntity.className(), remoteColumn.name());
            } else {
                log.warn("Could not find related entity for column %s.%s for remote table: %s; skipping related field resolution".formatted(columnMetaData.table().name(), columnMetaData.name(), remoteTableName));
            }
        }

        return null;
    }

    private static AnnotationExpr createAnnotation(AnnotationSpec annotationSpec) {
        final NormalAnnotationExpr annotation = new NormalAnnotationExpr();
        annotation.setName(new Name(annotationSpec.annotation().getSimpleName()));
        annotationSpec.pairs.forEach(annotation::addPair);
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

    private @Nullable ColumnMappingConfig getColumnMappingConfig(final ColumnMetaData columnMetaData, final @Nullable TableMappingConfig tableMappingConfig) {
        if (tableMappingConfig != null && tableMappingConfig.getColumnMappings() != null) {
            return tableMappingConfig.getColumnMappings().stream()
                    .filter(c -> c.getColumn().equals(columnMetaData.name()))
                    .findFirst().orElse(null);
        } else {
            return null;
        }
    }

    private record FieldInfo(FieldDeclaration field, boolean columnNullable) {
    }

    private record AnnotationSpec(Class<?> annotation, LinkedHashMap<String, String> pairs) {
    }

    private record ManyToManySpec(AnnotationSpec annotationSpec, String remoteEntityClassName) {
    }

    private record CompilationUnitAndClass(CompilationUnit entity, ClassOrInterfaceDeclaration entityClass) {
    }

    private record FieldClassInfo(@Nullable Class<?> fieldClass, @Nullable String fieldClassName) {
    }

    private record OneToManySpec(String reverseMappingCollectionType, String mappedByField) {
    }

    private record JoinOnInfo(String fieldClassType, String joinOn) {
    }
}

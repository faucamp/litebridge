package org.litebridge.maven.metamodel;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.types.ResolvedType;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.litebridge.maven.config.metamodel.MetamodelOutputConfig;
import org.litebridge.orm.annotation.Table;
import org.litebridge.orm.meta.NumericQueryField;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.meta.StringQueryField;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public final class MetamodelGenerator {

    private final boolean entitiesOnly;
    private final MetamodelOutputConfig output;
    private final Log log;

    public MetamodelGenerator(final boolean entitiesOnly, final MetamodelOutputConfig output, final Log log) {
        this.entitiesOnly = entitiesOnly;
        this.output = output;
        this.log = log;
    }

    public Optional<GeneratedMetamodel> createMetaModel(final File file) throws MojoExecutionException, IllegalArgumentException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Parsing Java source: " + file.getAbsolutePath());
            }

            final CompilationUnit source = StaticJavaParser.parse(file);

            final String sourceClassName = source.getPrimaryTypeName()
                    .orElseThrow(() -> new MojoExecutionException("No primary type name found in file: " + file.getAbsolutePath()));

            final List<ClassOrInterfaceDeclaration> classDeclarations = source.getLocalDeclarationFromClassname(sourceClassName);

            if (classDeclarations.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("No class declaration found for " + sourceClassName);
                }
                return Optional.empty();
            }

            if (entitiesOnly && source.getLocalDeclarationFromClassname(sourceClassName).getFirst().getAnnotationByClass(Table.class).isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Skipping non-entity class: " + sourceClassName);
                }

                return Optional.empty();
            }

            final String sourcePackageName = source.getPackageDeclaration()
                    .map(packageDeclaration -> packageDeclaration.getName().asString())
                    .orElse("");

            final String sourceQualifiedClassName = sourcePackageName.isBlank()
                    ? sourceClassName
                    : "%s.%s".formatted(sourcePackageName, sourceClassName);


            // Create metamodel
            final String metamodelClassName = output.getClassNamePrefix() + sourceClassName + output.getClassNameSuffix();
            final CompilationUnit metamodel = new CompilationUnit();
            metamodel.setPackageDeclaration(output.getOutputPackage());
            metamodel.addImport(sourceQualifiedClassName);

            final ClassOrInterfaceDeclaration metamodelClass = metamodel
                    .addClass(metamodelClassName)
                    .setPublic(true)
                    .setFinal(true)
                    .setJavadocComment("Litebridge metamodel for {@link %s}".formatted(sourceClassName));
            metamodelClass.addConstructor(Modifier.Keyword.PRIVATE);

            if (output.getJspecify() != null
                    && output.getJspecify().isAnnotate()
                    && !output.isPackageInfo()) {
                if (output.getJspecify().isNullMarked()) {
                    metamodelClass.addMarkerAnnotation(NullMarked.class);
                } else {
                    metamodelClass.addMarkerAnnotation(NullUnmarked.class);
                }
            }

            // Target non-static fields from the source file
            source.findAll(FieldDeclaration.class, fieldDeclaration -> !fieldDeclaration.isStatic())
                    .forEach(fieldDeclaration -> {
                        final String fieldName = fieldDeclaration.getVariable(0).getNameAsString();
                        final Type fieldType = fieldDeclaration.getVariable(0).getType();

                        Class<? extends QueryField> queryFieldClass = QueryField.class;

                        try {
                            // Use JavaParser symbol resolver to calculate runtime assignments
                            final ResolvedType resolvedType = fieldType.resolve();

                            if (resolvedType.isReferenceType()) {
                                String fqdn = resolvedType.asReferenceType().getQualifiedName();

                                if ("java.lang.String".equals(fqdn)) {
                                    queryFieldClass = StringQueryField.class;
                                } else if (resolvedType.asReferenceType().getAllAncestors().stream()
                                        .anyMatch(ancestor -> "java.lang.Number".equals(ancestor.getQualifiedName()))) {
                                    queryFieldClass = NumericQueryField.class;
                                }
                            } else if (resolvedType.isPrimitive()) {
                                // Catch primitives like int, double, float, long
                                String name = resolvedType.asPrimitive().name().toLowerCase();

                                if (!"boolean".equals(name) && !"char".equals(name)) {
                                    queryFieldClass = NumericQueryField.class;
                                }
                            }
                        } catch (Exception ex) {
                            // Fallback if type resolution fails (e.g. unresolvable custom project types)
                            if (log.isDebugEnabled()) {
                                log.debug("Could not resolve type for field %s.%s; defaulting to QueryField".formatted(sourceClassName, fieldName));
                            }
                        }

                        // Create the query field
                        final FieldDeclaration queryField = metamodelClass.addField(
                                        queryFieldClass,
                                        fieldName,
                                        Modifier.Keyword.PUBLIC,
                                        Modifier.Keyword.STATIC,
                                        Modifier.Keyword.FINAL)
                                .setJavadocComment("Query field for {@code %s.%s}".formatted(sourceClassName, fieldName));

                        queryField.getVariable(0)
                                .setInitializer("new %s(%s.class, \"%s\")".formatted(
                                        queryFieldClass.getSimpleName(),
                                        sourceClassName,
                                        fieldName));
                    });

            return Optional.of(new GeneratedMetamodel(metamodel, metamodelClassName));
        } catch (IOException ex) {
            throw new MojoExecutionException("Error reading file: " + file.getAbsolutePath(), ex);
        }
    }
}

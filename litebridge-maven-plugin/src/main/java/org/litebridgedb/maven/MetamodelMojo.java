package org.litebridgedb.maven;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.litebridgedb.commons.CollectionUtils;
import org.litebridgedb.commons.StringUtils;
import org.litebridgedb.orm.annotation.Table;
import org.litebridgedb.orm.meta.NumericQueryField;
import org.litebridgedb.orm.meta.QueryField;
import org.litebridgedb.orm.meta.StringQueryField;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Maven Mojo for generating Litebridge metamodel classes.
 * <p>
 * This Mojo generates Java source files based on the provided input packages and output directory.
 * It scans the specified input packages for (optionally {@link org.litebridgedb.db.spi.Table}-annotated classes)
 * and generates metamodel classes for each annotated class found.
 * <p>
 * This Mojo is executed during the <i>generate-sources</i> phase of the Maven build lifecycle by default.
 */
@Mojo(name = "metamodel", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE)
public final class MetamodelMojo extends AbstractMojo {

    /**
     * Maven project instance
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     * Skips plugin execution if {@code true}
     */
    @Parameter(defaultValue = "false")
    private boolean skip;

    /**
     * Input source directory. This is where root directory {@code <inputPackages>} will be searched for.
     * <p>
     * Defaults to the project's source directory.
     * <p>
     * This is useful when generating metamodels from reverse-engineered entities if the entities
     * are not generated in a location that is already part of the project's source directory.
     */
    @Parameter
    private String srcDir;

    /**
     * Packages to scan for entities/DTOs
     */
    @Parameter(property = "inputPackages", required = true)
    private List<String> inputPackages;

    /**
     * Output directory. This is where the {@code <outputPackage>} and generated metamodel classes will be created.
     * <p>
     * Defaults to {@code ${project.build.directory}/generated-sources/java}
     */
    @Parameter
    private String outputDir;

    /**
     * Output package for generated metamodel classes
     */
    @Parameter(property = "outputPackage", required = true)
    private String outputPackage;

    /**
     * Controls what types of input classes are processed during metamodel generation.
     * <p>
     * If {@code true}, only annotated entities will be included in metamodel generation.
     * If {@code false}, all classes will be included, whether they are annotated or not.
     */
    @Parameter(property = "entitiesOnly", defaultValue = "true")
    private boolean entitiesOnly;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().debug("Skipping Litebridge metamodel generation because skip == true");
            return;
        }

        getLog().info("Generating metamodel classes");
        final CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());

        if (CollectionUtils.isEmpty(inputPackages)) {
            throw new MojoExecutionException("No input package(s) specified");
        }

        final List<String> projectSrcDirs;

        if (srcDir == null) {
            projectSrcDirs = project.getCompileSourceRoots();
        } else {
            projectSrcDirs = Collections.singletonList(srcDir);
        }

        if (getLog().isDebugEnabled()) {
            getLog().debug("Searching for input packages in source directories: %s".formatted(projectSrcDirs));
        }

        final List<String> packageDirs = new ArrayList<>(inputPackages.size());

        for (String srcPackage : inputPackages) {
            final String packageDir = srcPackage.replace('.', File.separatorChar);
            boolean packageFound = false;

            for (String projectSrcDir : projectSrcDirs) {
                final File srcDir = Paths.get(projectSrcDir, packageDir).toFile();

                if (getLog().isDebugEnabled()) {
                    getLog().debug("Checking for package %s in source directory: %s".formatted(srcPackage, srcDir.getAbsolutePath()));
                }

                if (srcDir.exists() && srcDir.isDirectory()) {
                    typeSolver.add(new JavaParserTypeSolver(srcDir));
                    packageDirs.add(srcDir.getAbsolutePath());
                    packageFound = true;

                    if (getLog().isDebugEnabled()) {
                        getLog().debug("Package %s found in source directory: %s".formatted(srcPackage, srcDir.getAbsolutePath()));
                    }
                }
            }

            if (!packageFound) {
                throw new MojoExecutionException("Package not found: %s; searched dirs: %s".formatted(srcPackage, projectSrcDirs));
            }
        }

        // Apply symbol solver globally to JavaParser static engine
        final JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

        for (String dirName : packageDirs) {
            final File rootDir = new File(dirName);
            if (rootDir.exists() && rootDir.isDirectory()) {
                createMetamodelsForJavaSourceFiles(rootDir);
            }
        }
    }

    private void createMetamodelsForJavaSourceFiles(File directory) throws MojoExecutionException {
        final File[] files = directory.listFiles();

        if (files == null) {
            return;
        }

        if (outputDir == null) {
            outputDir = "%s/generated-sources/java".formatted(project.getBuild().getDirectory());
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // Recursively search folders
                createMetamodelsForJavaSourceFiles(file);
            } else if (file.getName().endsWith(".java")) {
                try {
                    if (getLog().isDebugEnabled()) {
                        getLog().debug("Parsing Java source: " + file.getAbsolutePath());
                    }

                    final CompilationUnit source = StaticJavaParser.parse(file);

                    final String sourceClassName = source.getPrimaryTypeName()
                            .orElseThrow(() -> new MojoExecutionException("No primary type name found in file: " + file.getAbsolutePath()));

                    final List<ClassOrInterfaceDeclaration> classDeclarations = source.getLocalDeclarationFromClassname(sourceClassName);

                    if (classDeclarations.isEmpty()) {
                        continue;
                    }

                    if (entitiesOnly && source.getLocalDeclarationFromClassname(sourceClassName).getFirst().getAnnotationByClass(Table.class).isEmpty()) {
                        if (getLog().isDebugEnabled()) {
                            getLog().debug("Skipping non-entity class: " + sourceClassName);
                        }
                        continue;
                    }

                    final String sourcePackageName = source.getPackageDeclaration()
                            .map(packageDeclaration -> packageDeclaration.getName().asString())
                            .orElse("");

                    final String sourceQualifiedClassName = sourcePackageName.isBlank()
                            ? sourceClassName
                            : "%s.%s".formatted(sourcePackageName, sourceClassName);

                    final String targetClassName = sourceClassName + "Meta";
                    final CompilationUnit metamodel = new CompilationUnit();
                    metamodel.setPackageDeclaration(outputPackage);
                    metamodel.addImport(sourceQualifiedClassName);

                    final ClassOrInterfaceDeclaration metamodelClass = metamodel
                            .addClass(targetClassName)
                            .setPublic(true)
                            .setFinal(true)
                            .setJavadocComment("Litebridge metamodel for {@link %s}".formatted(sourceClassName));
                    metamodelClass.addConstructor(Modifier.Keyword.PRIVATE);

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
                                    if (getLog().isDebugEnabled()) {
                                        getLog().debug("Could not resolve type for field %s.%s; defaulting to QueryField".formatted(sourceClassName, fieldName));
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

                    // Target output compilation structures safely
                    final String[] targetPackageArray = StringUtils.splitArray(outputPackage, '.', -1, false);
                    final Path packagePath = Paths.get(outputDir, targetPackageArray);
                    final File targetFile = new File(packagePath.toFile(), "%s.java".formatted(targetClassName));

                    try {
                        Files.createDirectories(packagePath);

                        try (FileWriter writer = new FileWriter(targetFile)) {
                            writer.write(metamodel.toString());
                        }

                        getLog().info("Created Litebridge metamodel class %s for %s at: %s".formatted(targetClassName, sourceClassName, targetFile.getAbsolutePath()));
                    } catch (IOException ex) {
                        throw new MojoExecutionException("Error writing generated Java class to file", ex);
                    }
                } catch (IOException ex) {
                    throw new MojoExecutionException("Error reading file: " + file.getAbsolutePath(), ex);
                }
            }
        }
    }
}

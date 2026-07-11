package org.litebridgedb.maven;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
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
import org.litebridgedb.maven.config.metamodel.MetamodelInputConfig;
import org.litebridgedb.maven.config.metamodel.MetamodelOutputConfig;
import org.litebridgedb.maven.metamodel.GeneratedMetamodel;
import org.litebridgedb.maven.metamodel.MetamodelGenerator;
import org.litebridgedb.maven.util.JavaFileWriter;
import org.litebridgedb.maven.util.PackageInfoGenerator;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
     * Input configuration.
     */
    @Parameter(required = true)
    private MetamodelInputConfig input;

    /**
     * Generated output configuration.
     */
    @Parameter(required = true)
    private MetamodelOutputConfig output;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().debug("Skipping Litebridge metamodel generation because skip == true");
            return;
        }

        getLog().info("Generating metamodel classes");
        final InputData inputData = getTypeSolverAndPackageDirs();

        // Apply symbol solver globally to JavaParser static engine
        final JavaSymbolSolver symbolSolver = new JavaSymbolSolver(inputData.typeSolver());
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);
        final MetamodelGenerator metamodelGenerator = new MetamodelGenerator(input.isEntitiesOnly(), output, getLog());
        final PackageInfoGenerator packageInfoGenerator = new PackageInfoGenerator(output);
        final JavaFileWriter javaFileWriter = new JavaFileWriter(project, output, getLog());

        // Create package-info.java
        if (output.isPackageInfo()) {
            final CompilationUnit packageInfo = packageInfoGenerator.createPackageInfo(output.getOutputPackage(), "Litebridge entities.");
            javaFileWriter.writeJavaFile(output.getOutputPackage(), "package-info.java", packageInfo);
        }

        for (String dirName : inputData.packageDirs()) {
            final File rootDir = new File(dirName);
            if (rootDir.exists() && rootDir.isDirectory()) {
                createMetamodelsForJavaSourceFiles(rootDir, metamodelGenerator, javaFileWriter);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private InputData getTypeSolverAndPackageDirs() throws MojoExecutionException {
        if (input == null) {
            throw new MojoExecutionException("No input configuration provided");
        }

        // Parse input packages and setup Java parser
        final CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());

        if (CollectionUtils.isEmpty(input.getInputPackages())) {
            throw new MojoExecutionException("No input package(s) specified");
        }

        final List<String> projectSrcDirs;

        if (input.getSrcDir() != null) {
            final String nonNullSrcDir = input.getSrcDir();
            projectSrcDirs = Collections.singletonList(nonNullSrcDir);
        } else {
            projectSrcDirs = project.getCompileSourceRoots();
        }

        if (getLog().isDebugEnabled()) {
            getLog().debug("Searching for input packages in source directories: %s".formatted(projectSrcDirs));
        }

        final List<String> packageDirs = new ArrayList<>(input.getInputPackages().size());

        for (String srcPackage : input.getInputPackages()) {
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

        return new InputData(typeSolver, packageDirs);
    }

    private void createMetamodelsForJavaSourceFiles(final File directory,
                                                    final MetamodelGenerator metamodelGenerator,
                                                    final JavaFileWriter javaFileWriter) throws MojoExecutionException {
        final File[] files = directory.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // Recursively search folders
                createMetamodelsForJavaSourceFiles(file, metamodelGenerator, javaFileWriter);
            } else if (file.getName().endsWith(".java")) {
                // Create metamodel
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Creating metamodel for file: " + file.getAbsolutePath());
                }
                final Optional<GeneratedMetamodel> generatedMetamodelOptional = metamodelGenerator.createMetaModel(file);

                if (generatedMetamodelOptional.isEmpty()) {
                    // Metamodel creation rejected
                    continue;
                }

                // Write metamodel Java output file
                final GeneratedMetamodel generatedMetamodel = generatedMetamodelOptional.get();
                final String filename = "%s.java".formatted(generatedMetamodel.className());
                javaFileWriter.writeJavaFile(output.getOutputPackage(), filename, generatedMetamodel.metamodel());
            }
        }
    }

    private record InputData(CombinedTypeSolver typeSolver, List<String> packageDirs) {
    }
}

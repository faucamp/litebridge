package org.litebridgedb.maven.reverse;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.litebridgedb.commons.StringUtils;
import org.litebridgedb.maven.config.OutputConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

public class EntityWriter {

    private final MavenProject project;
    private final OutputConfig output;
    private final Log log;
    private final String outputDir;

    public EntityWriter(final MavenProject project, final OutputConfig output, final Log log) {
        this.project = project;
        this.output = output;
        this.log = log;

        if (output.getOutputDir() == null) {
            this.outputDir = "%s/generated-sources/java".formatted(project.getBuild().getDirectory());
        } else {
            this.outputDir = output.getOutputDir();
        }
    }

    public void writeEntityJavaFile(final GeneratedEntity generatedEntity) throws MojoExecutionException {
        final CompilationUnit entity = generatedEntity.entity();
        final String entityClassName = generatedEntity.className();
        final ClassOrInterfaceDeclaration entityClass = entity.getLocalDeclarationFromClassname(entityClassName).getFirst();

        // Write generated entity class to file
        final String[] targetPackageArray = StringUtils.splitArray(output.getOutputPackage(), '.', -1, false);
        final Path packagePath = Paths.get(outputDir, targetPackageArray);
        final File targetFile = new File(packagePath.toFile(), "%s.java".formatted(entityClassName));

        try {
            Files.createDirectories(packagePath);

            try (FileWriter writer = new FileWriter(targetFile)) {
                writer.write(entity.toString());
            }

            log.info("Created Litebridge entity class %s for table %s at: %s".formatted(entityClassName, generatedEntity.tableName(), targetFile.getAbsolutePath()));
        } catch (IOException ex) {
            throw new MojoExecutionException("Error writing generated Java class to file", ex);
        }
    }
}

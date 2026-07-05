package org.litebridgedb.maven.util;

import com.github.javaparser.ast.CompilationUnit;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.litebridgedb.commons.StringUtils;
import org.litebridgedb.maven.config.OutputConfig;
import org.litebridgedb.maven.reverse.GeneratedEntity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class that writes generated Java files to a specified output directory.
 * <p>
 * It handles the file creation and I/O operations while logging the progress or issues encountered
 * during the process.
 */
public class JavaFileWriter {

    private final OutputConfig output;
    private final Log log;
    private final String outputDir;

    public JavaFileWriter(final MavenProject project, final OutputConfig output, final Log log) {
        this.output = output;
        this.log = log;
        this.outputDir = MojoDirUtils.getOutputDir(output.getOutputDir(), project);
    }

    /**
     * Writes the generated Java entity file to the designated output directory.
     *
     * @param generatedEntity The representation of the generated Java entity, including its
     *                        compilation unit and associated metadata such as class name
     *                        and table name.
     * @throws MojoExecutionException If an error occurs while writing the generated Java file,
     *                                such as issues with file creation or I/O operations.
     */
    public void writeEntityJavaFile(final GeneratedEntity generatedEntity) throws MojoExecutionException {
        final String[] targetPackageArray = StringUtils.splitArray(output.getOutputPackage(), '.', -1, false);
        final Path packagePath = Paths.get(outputDir, targetPackageArray);
        writeJavaFile(new File(packagePath.toFile(), "%s.java".formatted(generatedEntity.className())), generatedEntity.entity());
    }

    public void writeJavaFile(final String packageName, final String filename, final CompilationUnit compilationUnit) throws MojoExecutionException {
        final Path packagePath = Paths.get(outputDir, StringUtils.splitArray(packageName, '.', -1, false));
        writeJavaFile(new File(packagePath.toFile(), filename), compilationUnit);
    }

    /**
     * Writes the generated Java file to the designated output directory.
     *
     * @param file            The file to write.
     * @param compilationUnit The compilation unit representing the generated Java file.
     * @throws MojoExecutionException If an error occurs while writing the generated Java file,
     *                                such as issues with file creation or I/O operations.
     */
    public void writeJavaFile(final File file, final CompilationUnit compilationUnit) throws MojoExecutionException {
        if (log.isDebugEnabled()) {
            log.debug("Writing generated Java class to file: " + file.getAbsolutePath());
        }

        try {
            Files.createDirectories(file.toPath().getParent());

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(compilationUnit.toString());
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("Error writing generated Java class to file: " + file.getAbsolutePath(), ex);
        }
    }
}

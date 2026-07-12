package org.litebridge.maven.util;

import com.github.javaparser.ast.CompilationUnit;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.litebridge.maven.config.OutputConfig;
import org.litebridge.maven.reverse.GeneratedEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JavaFileWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void testWriteJavaFile_Success() throws MojoExecutionException, IOException {
        MavenProject project = mock(MavenProject.class);
        Build build = mock(Build.class);
        when(project.getBuild()).thenReturn(build);
        when(build.getDirectory()).thenReturn(tempDir.toString());

        Log log = mock(Log.class);
        when(log.isDebugEnabled()).thenReturn(true);

        OutputConfig config = new OutputConfig();
        config.setOutputDir(tempDir.toString());
        config.setOutputPackage("com.example");

        JavaFileWriter writer = new JavaFileWriter(project, config, log);
        
        CompilationUnit cu = new CompilationUnit("com.example");
        cu.addClass("MyClass");
        
        File targetFile = tempDir.resolve("com/example/MyClass.java").toFile();
        writer.writeJavaFile("com.example", "MyClass.java", cu);
        
        assertTrue(targetFile.exists());
        verify(log).debug(anyString());
    }

    @Test
    void testWriteEntityJavaFile() throws MojoExecutionException {
        MavenProject project = mock(MavenProject.class);
        Build build = mock(Build.class);
        when(project.getBuild()).thenReturn(build);
        when(build.getDirectory()).thenReturn(tempDir.toString());

        Log log = mock(Log.class);
        
        OutputConfig config = new OutputConfig();
        config.setOutputDir(tempDir.toString());
        config.setOutputPackage("com.example");

        JavaFileWriter writer = new JavaFileWriter(project, config, log);
        
        CompilationUnit cu = new CompilationUnit("com.example");
        cu.addClass("MyEntity");
        GeneratedEntity entity = new GeneratedEntity(cu, "my_table", "MyEntity", new HashMap<>());
        
        writer.writeEntityJavaFile(entity);
        
        assertTrue(tempDir.resolve("com/example/MyEntity.java").toFile().exists());
    }

    @Test
    void testWriteJavaFile_IOException() throws IOException {
        MavenProject project = mock(MavenProject.class);
        Build build = mock(Build.class);
        when(project.getBuild()).thenReturn(build);
        when(build.getDirectory()).thenReturn(tempDir.toString());

        Log log = mock(Log.class);
        
        OutputConfig config = new OutputConfig();
        // Use a directory that can't be created (e.g. a file exists with the same name as a directory)
        File file = tempDir.resolve("blocked").toFile();
        file.createNewFile();
        config.setOutputDir(file.getAbsolutePath());

        JavaFileWriter writer = new JavaFileWriter(project, config, log);
        CompilationUnit cu = new CompilationUnit();

        assertThrows(MojoExecutionException.class, () -> writer.writeJavaFile("pkg", "File.java", cu));
    }

    @Test
    void testWriteJavaFile_DebugDisabled() throws MojoExecutionException {
        MavenProject project = mock(MavenProject.class);
        Build build = mock(Build.class);
        when(project.getBuild()).thenReturn(build);
        when(build.getDirectory()).thenReturn(tempDir.toString());

        Log log = mock(Log.class);
        when(log.isDebugEnabled()).thenReturn(false);

        OutputConfig config = new OutputConfig();
        config.setOutputDir(tempDir.toString());
        config.setOutputPackage("com.example");

        JavaFileWriter writer = new JavaFileWriter(project, config, log);
        CompilationUnit cu = new CompilationUnit();
        
        writer.writeJavaFile("com.example", "DebugOff.java", cu);
        verify(log, never()).debug(anyString());
    }
}

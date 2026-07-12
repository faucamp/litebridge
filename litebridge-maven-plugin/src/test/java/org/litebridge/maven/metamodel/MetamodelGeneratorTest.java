package org.litebridge.maven.metamodel;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.litebridge.maven.DebugMojoLog;
import org.litebridge.maven.config.metamodel.MetamodelOutputConfig;
import org.litebridge.maven.config.reverse.RevEngJSpecifyConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MetamodelGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    void createMetaModel_fileNotFound() {
        // Given
        final Log log = new DebugMojoLog(MetamodelGenerator.class);
        final MetamodelOutputConfig output = new MetamodelOutputConfig();
        final MetamodelGenerator generator = new MetamodelGenerator(false, output, log);

        final File nonExistentFile = new File(tempDir.toFile(), "NonExistent.java");

        // When / Then
        assertThrows(MojoExecutionException.class, () -> generator.createMetaModel(nonExistentFile));
    }

    @Test
    void createMetaModel_IOException() throws IOException {
        // Given
        final Log log = new DebugMojoLog(MetamodelGenerator.class);
        final MetamodelOutputConfig output = new MetamodelOutputConfig();
        final MetamodelGenerator generator = new MetamodelGenerator(false, output, log);

        final File file = new File(tempDir.toFile(), "Unreadable.java");
        file.createNewFile();
        file.setReadable(false);

        // When / Then
        try {
            assertThrows(MojoExecutionException.class, () -> generator.createMetaModel(file));
        } finally {
            file.setReadable(true);
        }
    }

    @Test
    void createMetaModel_NoClassDeclarations() throws IOException, MojoExecutionException {
        // Given
        final Log log = new DebugMojoLog(MetamodelGenerator.class);
        final MetamodelOutputConfig output = new MetamodelOutputConfig();
        final MetamodelGenerator generator = new MetamodelGenerator(false, output, log);

        final Path javaFile = tempDir.resolve("NoClass.java");
        Files.writeString(javaFile, "package com.example; enum MyEnum {}");

        // When
        final Optional<GeneratedMetamodel> result = generator.createMetaModel(javaFile.toFile());

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void createMetaModel_fullSuccess() throws IOException, MojoExecutionException {
        // Given
        final CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        StaticJavaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));

        final Log log = new DebugMojoLog(MetamodelGenerator.class);
        final MetamodelOutputConfig output = new MetamodelOutputConfig();
        output.setOutputPackage("com.example.meta");
        final MetamodelGenerator generator = new MetamodelGenerator(false, output, log);

        final Path javaFile = tempDir.resolve("MyEntity.java");
        Files.writeString(javaFile, """
                package com.example;
                public class MyEntity {
                    private String name;
                    private int age;
                    private Long id;
                    private boolean active;
                    private char grade;
                    private Object other;
                    private String[] tags;
                }
                """);

        // When
        final Optional<GeneratedMetamodel> result = generator.createMetaModel(javaFile.toFile());

        // Then
        assertTrue(result.isPresent());
        GeneratedMetamodel metamodel = result.get();
        assertEquals("MyEntityMeta", metamodel.className());

        final String code = metamodel.metamodel().toString();
        assertTrue(code.contains("StringQueryField name"));
        assertTrue(code.contains("NumericQueryField age"));
        assertTrue(code.contains("NumericQueryField id"));
        assertTrue(code.contains("QueryField active"));
        assertTrue(code.contains("QueryField grade"));
        assertTrue(code.contains("QueryField other"));
    }

    @Test
    void createMetaModel_jspecify_nullMarked() throws IOException, MojoExecutionException {
        // Given
        final Log log = new DebugMojoLog(MetamodelGenerator.class);
        final MetamodelOutputConfig output = new MetamodelOutputConfig();
        output.setOutputPackage("com.example.meta");
        RevEngJSpecifyConfig jspecify = new RevEngJSpecifyConfig();
        jspecify.setAnnotate(true);
        jspecify.setNullMarked(true);
        output.setJspecify(jspecify);
        output.setPackageInfo(false);

        final MetamodelGenerator generator = new MetamodelGenerator(false, output, log);

        final Path javaFile = tempDir.resolve("JSpecifyEntity.java");
        Files.writeString(javaFile, "package com.example; public class JSpecifyEntity {}");

        // When
        final Optional<GeneratedMetamodel> result = generator.createMetaModel(javaFile.toFile());

        // Then
        assertTrue(result.isPresent());
        assertTrue(result.get().metamodel().toString().contains("@NullMarked"));
    }

    @Test
    void createMetaModel_jspecify_nullUnmarked() throws IOException, MojoExecutionException {
        // Given
        final Log log = new DebugMojoLog(MetamodelGenerator.class);
        final MetamodelOutputConfig output = new MetamodelOutputConfig();
        output.setOutputPackage("com.example.meta");
        final RevEngJSpecifyConfig jspecify = new RevEngJSpecifyConfig();
        jspecify.setAnnotate(true);
        jspecify.setNullMarked(false);
        output.setJspecify(jspecify);
        output.setPackageInfo(false);

        final MetamodelGenerator generator = new MetamodelGenerator(false, output, log);

        final Path javaFile = tempDir.resolve("JSpecifyUnmarked.java");
        Files.writeString(javaFile, "package com.example; public class JSpecifyUnmarked {}");

        // When
        final Optional<GeneratedMetamodel> result = generator.createMetaModel(javaFile.toFile());

        // Then
        assertTrue(result.isPresent());
        assertTrue(result.get().metamodel().toString().contains("@NullUnmarked"));
    }

    @Test
    void createMetaModel_typeResolutionFailure() throws IOException, MojoExecutionException {
        // Given
        // Reset symbol solver to one that fails
        StaticJavaParser.getParserConfiguration().setSymbolResolver(null);

        final Log log = new DebugMojoLog(MetamodelGenerator.class);
        final MetamodelOutputConfig output = new MetamodelOutputConfig();
        output.setOutputPackage("com.example.meta");
        final MetamodelGenerator generator = new MetamodelGenerator(false, output, log);

        final Path javaFile = tempDir.resolve("FailEntity.java");
        Files.writeString(javaFile, "package com.example; public class FailEntity { private UnknownType field; }");

        // When
        Optional<GeneratedMetamodel> result = generator.createMetaModel(javaFile.toFile());

        // Then
        assertTrue(result.isPresent());
        assertTrue(result.get().metamodel().toString().contains("QueryField field"));
    }

    @Test
    void createMetaModel_debugDisabled() throws IOException, MojoExecutionException {
        // Given
        final Log log = mock(Log.class);
        when(log.isDebugEnabled()).thenReturn(false);

        final MetamodelOutputConfig output = new MetamodelOutputConfig();
        output.setOutputPackage("com.example.meta");
        final MetamodelGenerator generator = new MetamodelGenerator(false, output, log);

        final Path javaFile = tempDir.resolve("DebugDisabled.java");
        Files.writeString(javaFile, "package com.example; public class DebugDisabled {}");

        // When
        Optional<GeneratedMetamodel> result = generator.createMetaModel(javaFile.toFile());

        // Then
        assertTrue(result.isPresent());
        verify(log, never()).debug(anyString());
    }

    @Test
    void createMetaModel_noPackage() throws IOException, MojoExecutionException {
        // Given
        final Log log = new DebugMojoLog(MetamodelGenerator.class);
        final MetamodelOutputConfig output = new MetamodelOutputConfig();
        output.setOutputPackage("com.example.meta");
        final MetamodelGenerator generator = new MetamodelGenerator(false, output, log);

        final Path javaFile = tempDir.resolve("NoPackage.java");
        Files.writeString(javaFile, "public class NoPackage {}");

        // When
        final Optional<GeneratedMetamodel> result = generator.createMetaModel(javaFile.toFile());

        // Then
        assertTrue(result.isPresent());
        String code = result.get().metamodel().toString();
        // Just verify it works and has the class name
        assertTrue(code.contains("class NoPackageMeta"));
    }

    @Test
    void createMetaModel_jspecify_disabled() throws IOException, MojoExecutionException {
        // Given
        final Log log = new DebugMojoLog(MetamodelGenerator.class);
        final MetamodelOutputConfig output = new MetamodelOutputConfig();
        output.setOutputPackage("com.example.meta");
        final RevEngJSpecifyConfig jspecify = new RevEngJSpecifyConfig();
        jspecify.setAnnotate(false);
        output.setJspecify(jspecify);

        final MetamodelGenerator generator = new MetamodelGenerator(false, output, log);

        final Path javaFile = tempDir.resolve("JSpecifyDisabled.java");
        Files.writeString(javaFile, "package com.example; public class JSpecifyDisabled {}");

        // When
        Optional<GeneratedMetamodel> result = generator.createMetaModel(javaFile.toFile());

        // Then
        assertTrue(result.isPresent());
        String code = result.get().metamodel().toString();
        assertFalse(code.contains("@NullMarked"));
        assertFalse(code.contains("@NullUnmarked"));
    }

    @Test
    void createMetaModel_jspecify_packageInfoTrue() throws IOException, MojoExecutionException {
        // Given
        final Log log = new DebugMojoLog(MetamodelGenerator.class);
        final MetamodelOutputConfig output = new MetamodelOutputConfig();
        output.setOutputPackage("com.example.meta");
        final RevEngJSpecifyConfig jspecify = new RevEngJSpecifyConfig();
        jspecify.setAnnotate(true);
        output.setJspecify(jspecify);
        output.setPackageInfo(true);

        final MetamodelGenerator generator = new MetamodelGenerator(false, output, log);

        final Path javaFile = tempDir.resolve("JSpecifyPkgInfo.java");
        Files.writeString(javaFile, "package com.example; public class JSpecifyPkgInfo {}");

        // When
        final Optional<GeneratedMetamodel> result = generator.createMetaModel(javaFile.toFile());

        // Then
        assertTrue(result.isPresent());
        String code = result.get().metamodel().toString();
        assertFalse(code.contains("@NullMarked"));
        assertFalse(code.contains("@NullUnmarked"));
    }

    @Test
    void createMetaModel_entitiesOnly_nonEntity() throws IOException, MojoExecutionException {
        // Given
        final Log log = new DebugMojoLog(MetamodelGenerator.class);
        final MetamodelOutputConfig output = new MetamodelOutputConfig();
        final MetamodelGenerator generator = new MetamodelGenerator(true, output, log);

        final Path javaFile = tempDir.resolve("NonEntity.java");
        Files.writeString(javaFile, "package com.example; public class NonEntity { private String id; }");

        // When
        final Optional<GeneratedMetamodel> result = generator.createMetaModel(javaFile.toFile());

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void createMetaModel_entitiesOnly_nonEntity_debugDisabled() throws IOException, MojoExecutionException {
        // Given
        final Log log = mock(Log.class);
        when(log.isDebugEnabled()).thenReturn(false);

        final MetamodelOutputConfig output = new MetamodelOutputConfig();
        final MetamodelGenerator generator = new MetamodelGenerator(true, output, log);

        final Path javaFile = tempDir.resolve("NonEntityDebugOff.java");
        Files.writeString(javaFile, "package com.example; public class NonEntityDebugOff {}");

        // When
        final Optional<GeneratedMetamodel> result = generator.createMetaModel(javaFile.toFile());

        // Then
        assertTrue(result.isEmpty());
        verify(log, never()).debug(anyString());
    }

    @Test
    void createMetaModel_typeResolutionFailure_debugDisabled() throws IOException, MojoExecutionException {
        // Given
        // Reset symbol solver to one that fails
        StaticJavaParser.getParserConfiguration().setSymbolResolver(null);

        final Log log = mock(Log.class);
        when(log.isDebugEnabled()).thenReturn(false);

        final MetamodelOutputConfig output = new MetamodelOutputConfig();
        output.setOutputPackage("com.example.meta");
        final MetamodelGenerator generator = new MetamodelGenerator(false, output, log);

        final Path javaFile = tempDir.resolve("FailEntityDebugOff.java");
        Files.writeString(javaFile, "package com.example; public class FailEntityDebugOff { private UnknownType field; }");

        // When
        final Optional<GeneratedMetamodel> result = generator.createMetaModel(javaFile.toFile());

        // Then
        assertTrue(result.isPresent());
        verify(log, never()).debug(anyString());
    }

    @Test
    void createMetaModel_jspecify_null() throws IOException, MojoExecutionException {
        // Given
        final Log log = new DebugMojoLog(MetamodelGenerator.class);
        final MetamodelOutputConfig output = new MetamodelOutputConfig();
        output.setOutputPackage("com.example.meta");
        output.setJspecify(null);

        final MetamodelGenerator generator = new MetamodelGenerator(false, output, log);

        final Path javaFile = tempDir.resolve("JSpecifyNull.java");
        Files.writeString(javaFile, "package com.example; public class JSpecifyNull {}");

        // When
        final Optional<GeneratedMetamodel> result = generator.createMetaModel(javaFile.toFile());

        // Then
        assertTrue(result.isPresent());
        String code = result.get().metamodel().toString();
        assertFalse(code.contains("@NullMarked"));
        assertFalse(code.contains("@NullUnmarked"));
    }

    @Test
    void createMetaModel_mismatchedClassName() throws IOException, MojoExecutionException {
        // Given
        final Log log = new DebugMojoLog(MetamodelGenerator.class);
        final MetamodelOutputConfig output = new MetamodelOutputConfig();
        final MetamodelGenerator generator = new MetamodelGenerator(false, output, log);

        final Path javaFile = tempDir.resolve("Mismatched.java");
        Files.writeString(javaFile, "package com.example; public class OtherName {}");

        // When
        final Optional<GeneratedMetamodel> result = generator.createMetaModel(javaFile.toFile());

        // Then
        assertTrue(result.isEmpty());
    }
}

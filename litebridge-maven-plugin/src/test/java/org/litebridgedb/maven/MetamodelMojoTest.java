package org.litebridgedb.maven;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import org.apache.maven.api.di.Provides;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoParameter;
import org.apache.maven.api.plugin.testing.MojoParameters;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridgedb.orm.meta.NumericQueryField;
import org.litebridgedb.orm.meta.QueryField;
import org.litebridgedb.orm.meta.StringQueryField;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@MojoTest
class MetamodelMojoTest {

    private MavenProject project;

    @Provides
    private MavenProject initProject() {
        project = mock(MavenProject.class);
        return project;
    }

    @Provides
    private Log initLog() {
        return new DebugMojoLog(MetamodelMojo.class);
    }

    @BeforeEach
    void beforeEach() throws IOException {
        DirUtil.deleteDirectoryRecursively(Paths.get("target/generated-sources"));
    }

    @Test
    @InjectMojo(goal = "metamodel")
    @MojoParameters({
            @MojoParameter(name = "entitiesOnly", value = "false"),
            @MojoParameter(name = "inputPackages", value = "org.litebridgedb.maven.test.dto"),
            @MojoParameter(name = "outputPackage", value = "org.litebridgedb.maven.test.meta")
    })
    void execute_entitiesOnly_false(final MetamodelMojo metamodelMojo) throws Exception {
        // Given
        when(project.getCompileSourceRoots()).thenReturn(List.of("src/test/java"));
        final Build build = mock(Build.class);
        when(build.getDirectory()).thenReturn("target");
        when(project.getBuild()).thenReturn(build);

        // When
        metamodelMojo.execute();

        // Then
        final Path expectedOutputFile1 = Paths.get("target/generated-sources/java/org/litebridgedb/maven/test/meta/TestDtoMeta.java");
        assertTrue(expectedOutputFile1.toFile().exists());
        final CompilationUnit compilationUnit1 = StaticJavaParser.parse(expectedOutputFile1);
        assertEquals("TestDtoMeta", compilationUnit1.getPrimaryTypeName().orElseThrow());
        final List<FieldDeclaration> fieldDeclarations1 = compilationUnit1.findAll(FieldDeclaration.class, fieldDeclaration ->
                fieldDeclaration.isPublic()
                        && fieldDeclaration.isStatic()
                        && fieldDeclaration.isFinal());
        assertEquals(3, fieldDeclarations1.size());

        for (FieldDeclaration fieldDeclaration : fieldDeclarations1) {
            switch (fieldDeclaration.getVariable(0).getNameAsString()) {
                case "id" ->
                        assertEquals(NumericQueryField.class.getSimpleName(), fieldDeclaration.getVariable(0).getType().toString());
                case "name" ->
                        assertEquals(StringQueryField.class.getSimpleName(), fieldDeclaration.getVariable(0).getType().toString());
                case "relatedDtos" ->
                        assertEquals(QueryField.class.getSimpleName(), fieldDeclaration.getVariable(0).getType().toString());
                default -> fail("Unknown generated field: " + fieldDeclaration);
            }
        }

        final Path expectedOutputFile2 = Paths.get("target/generated-sources/java/org/litebridgedb/maven/test/meta/TestRelatedDtoMeta.java");
        assertTrue(expectedOutputFile2.toFile().exists());
        final CompilationUnit compilationUnit2 = StaticJavaParser.parse(expectedOutputFile2);
        assertEquals("TestRelatedDtoMeta", compilationUnit2.getPrimaryTypeName().orElseThrow());
        final List<FieldDeclaration> fieldDeclarations2 = compilationUnit2.findAll(FieldDeclaration.class, fieldDeclaration ->
                fieldDeclaration.isPublic()
                        && fieldDeclaration.isStatic()
                        && fieldDeclaration.isFinal());
        assertEquals(4, fieldDeclarations2.size());

        for (FieldDeclaration fieldDeclaration : fieldDeclarations2) {
            switch (fieldDeclaration.getVariable(0).getNameAsString()) {
                case "id" ->
                        assertEquals(NumericQueryField.class.getSimpleName(), fieldDeclaration.getVariable(0).getType().toString());
                case "value" ->
                        assertEquals(NumericQueryField.class.getSimpleName(), fieldDeclaration.getVariable(0).getType().toString());
                case "active" ->
                        assertEquals(QueryField.class.getSimpleName(), fieldDeclaration.getVariable(0).getType().toString());
                case "testDto" ->
                        assertEquals(QueryField.class.getSimpleName(), fieldDeclaration.getVariable(0).getType().toString());
                default -> fail("Unknown generated field: " + fieldDeclaration);
            }
        }
    }

    @Test
    @InjectMojo(goal = "metamodel")
    @MojoParameters({
            @MojoParameter(name = "inputPackages", value = "org.litebridgedb.maven.test"),
            @MojoParameter(name = "outputPackage", value = "org.litebridgedb.maven.test.meta")
    })
    void execute_entitiesOnly_defaultTrue(final MetamodelMojo metamodelMojo) throws Exception {
        // Given
        when(project.getCompileSourceRoots()).thenReturn(List.of("src/test/java"));
        final Build build = mock(Build.class);
        when(build.getDirectory()).thenReturn("target");
        when(project.getBuild()).thenReturn(build);

        // When
        metamodelMojo.execute();

        // Then
        final Path expectedOutputFile1 = Paths.get("target/generated-sources/java/org/litebridgedb/maven/test/meta/TestEntityMeta.java");
        assertTrue(expectedOutputFile1.toFile().exists());
        final CompilationUnit compilationUnit1 = StaticJavaParser.parse(expectedOutputFile1);
        assertEquals("TestEntityMeta", compilationUnit1.getPrimaryTypeName().orElseThrow());
        final List<FieldDeclaration> fieldDeclarations1 = compilationUnit1.findAll(FieldDeclaration.class, fieldDeclaration ->
                fieldDeclaration.isPublic()
                        && fieldDeclaration.isStatic()
                        && fieldDeclaration.isFinal());
        assertEquals(3, fieldDeclarations1.size());

        for (FieldDeclaration fieldDeclaration : fieldDeclarations1) {
            switch (fieldDeclaration.getVariable(0).getNameAsString()) {
                case "id" ->
                        assertEquals(NumericQueryField.class.getSimpleName(), fieldDeclaration.getVariable(0).getType().toString());
                case "name" ->
                        assertEquals(StringQueryField.class.getSimpleName(), fieldDeclaration.getVariable(0).getType().toString());
                case "relatedEntities" ->
                        assertEquals(QueryField.class.getSimpleName(), fieldDeclaration.getVariable(0).getType().toString());
                default -> fail("Unknown generated field: " + fieldDeclaration);
            }
        }

        final Path expectedOutputFile2 = Paths.get("target/generated-sources/java/org/litebridgedb/maven/test/meta/TestRelatedEntityMeta.java");
        assertTrue(expectedOutputFile2.toFile().exists());
        final CompilationUnit compilationUnit2 = StaticJavaParser.parse(expectedOutputFile2);
        assertEquals("TestRelatedEntityMeta", compilationUnit2.getPrimaryTypeName().orElseThrow());
        final List<FieldDeclaration> fieldDeclarations2 = compilationUnit2.findAll(FieldDeclaration.class, fieldDeclaration ->
                fieldDeclaration.isPublic()
                        && fieldDeclaration.isStatic()
                        && fieldDeclaration.isFinal());
        assertEquals(4, fieldDeclarations2.size());

        for (FieldDeclaration fieldDeclaration : fieldDeclarations2) {
            switch (fieldDeclaration.getVariable(0).getNameAsString()) {
                case "id" ->
                        assertEquals(NumericQueryField.class.getSimpleName(), fieldDeclaration.getVariable(0).getType().toString());
                case "value" ->
                        assertEquals(NumericQueryField.class.getSimpleName(), fieldDeclaration.getVariable(0).getType().toString());
                case "active" ->
                        assertEquals(QueryField.class.getSimpleName(), fieldDeclaration.getVariable(0).getType().toString());
                case "testEntity" ->
                        assertEquals(QueryField.class.getSimpleName(), fieldDeclaration.getVariable(0).getType().toString());
                default -> fail("Unknown generated field: " + fieldDeclaration);
            }
        }
    }

    @Test
    @InjectMojo(goal = "metamodel")
    @MojoParameters({
            @MojoParameter(name = "inputPackages", value = "org.nonexistent"),
            @MojoParameter(name = "outputPackage", value = "org.litebridgedb.maven.test.meta")
    })
    void execute_inputPackageNotFound(final MetamodelMojo metamodelMojo) throws Exception {
        // When
        final MojoExecutionException result = assertThrows(MojoExecutionException.class, () -> metamodelMojo.execute());

        // Then
        assertEquals("Package not found: org.nonexistent", result.getMessage());
    }

    @Test
    @InjectMojo(goal = "metamodel")
    @MojoParameters({
            @MojoParameter(name = "outputPackage", value = "org.litebridgedb.maven.test.meta")
    })
    void execute_inputPackageNotSet(final MetamodelMojo metamodelMojo) throws Exception {
        // When
        final MojoExecutionException result = assertThrows(MojoExecutionException.class, () -> metamodelMojo.execute());

        // Then
        assertEquals("No input package(s) specified", result.getMessage());
    }

    @Test
    @InjectMojo(goal = "metamodel")
    @MojoParameters({
            @MojoParameter(name = "skip", value = "true")
    })
    void execute_skip(final MetamodelMojo metamodelMojo) throws Exception {
        // When
        metamodelMojo.execute();

        // Then
        verifyNoInteractions(project);
    }
}
package org.litebridgedb.maven;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import org.apache.maven.api.di.Provides;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoParameter;
import org.apache.maven.api.plugin.testing.MojoParameters;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.litebridgedb.orm.meta.NumericQueryField;
import org.litebridgedb.orm.meta.QueryField;
import org.litebridgedb.orm.meta.StringQueryField;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@MojoTest
class MetamodelMojoTest {

    @Provides
    private Log initLog() {
        return new DebugMojoLog(MetamodelMojo.class);
    }

    @BeforeEach
    void beforeEach() throws IOException {
        DirUtil.deleteDirectoryRecursively(Paths.get("target/generated-sources"));
    }

    @Test
    @DisplayName("Default: Entity metamodels")
    @InjectMojo(goal = "metamodel", pom = "classpath:/metamodel/pom.xml")
    void execute_defaults(final MetamodelMojo metamodelMojo) throws Exception {
        // When
        metamodelMojo.execute();

        // Then
        final Path packageInfoPath = Paths.get("target/generated-sources/java/org/litebridgedb/maven/test/meta/package-info.java");
        assertTrue(packageInfoPath.toFile().exists());
        final CompilationUnit packageInfoCu = StaticJavaParser.parse(packageInfoPath);
        assertFalse(packageInfoCu.getPackageDeclaration().orElseThrow()
                .isAnnotationPresent("NullMarked"));

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

        assertFalse(Paths.get("target/generated-sources/java/org/litebridgedb/maven/test/meta/TestDtoMeta.java")
                .toFile().exists());
        assertFalse(Paths.get("target/generated-sources/java/org/litebridgedb/maven/test/meta/TestRelatedDtoMeta.java")
                .toFile().exists());
    }

    @Test
    @DisplayName("JSpecify: defaults")
    @InjectMojo(goal = "metamodel", pom = "classpath:/metamodel/pom-jspecify.xml")
    void execute_jSpecify(final MetamodelMojo metamodelMojo) throws Exception {
        // When
        metamodelMojo.execute();

        // Then
        final Path packageInfoPath = Paths.get("target/generated-sources/java/org/litebridgedb/maven/test/meta/package-info.java");
        assertTrue(packageInfoPath.toFile().exists());
        final CompilationUnit packageInfoCu = StaticJavaParser.parse(packageInfoPath);
        assertTrue(packageInfoCu.getPackageDeclaration().orElseThrow()
                .isAnnotationPresent("NullMarked"));

        final Path expectedOutputFile1 = Paths.get("target/generated-sources/java/org/litebridgedb/maven/test/meta/TestEntityMeta.java");
        assertTrue(expectedOutputFile1.toFile().exists());
        final CompilationUnit compilationUnit1 = StaticJavaParser.parse(expectedOutputFile1);
        assertEquals("TestEntityMeta", compilationUnit1.getPrimaryTypeName().orElseThrow());
        final ClassOrInterfaceDeclaration testEntityMeta = compilationUnit1.findAll(ClassOrInterfaceDeclaration.class).getFirst();
        assertFalse(testEntityMeta.isAnnotationPresent("NullMarked"));
        assertFalse(testEntityMeta.isAnnotationPresent("NullUnmarked"));

        final Path expectedOutputFile2 = Paths.get("target/generated-sources/java/org/litebridgedb/maven/test/meta/TestRelatedEntityMeta.java");
        assertTrue(expectedOutputFile2.toFile().exists());
        final CompilationUnit compilationUnit2 = StaticJavaParser.parse(expectedOutputFile2);
        assertEquals("TestRelatedEntityMeta", compilationUnit2.getPrimaryTypeName().orElseThrow());
        final ClassOrInterfaceDeclaration TestRelatedEntityMeta = compilationUnit1.findAll(ClassOrInterfaceDeclaration.class).getFirst();
        assertFalse(TestRelatedEntityMeta.isAnnotationPresent("NullMarked"));
        assertFalse(TestRelatedEntityMeta.isAnnotationPresent("NullUnmarked"));
    }

    @Test
    @DisplayName("JSpecify: NullUnmarked")
    @InjectMojo(goal = "metamodel", pom = "classpath:/metamodel/pom-jspecify-nullUnmarked.xml")
    void execute_jSpecify_nullUnMarked(final MetamodelMojo metamodelMojo) throws Exception {
        // When
        metamodelMojo.execute();

        // Then
        final Path packageInfoPath = Paths.get("target/generated-sources/java/org/litebridgedb/maven/test/meta/package-info.java");
        assertTrue(packageInfoPath.toFile().exists());
        final CompilationUnit packageInfoCu = StaticJavaParser.parse(packageInfoPath);
        assertTrue(packageInfoCu.getPackageDeclaration().orElseThrow()
                .isAnnotationPresent("NullUnmarked"));

        final Path expectedOutputFile1 = Paths.get("target/generated-sources/java/org/litebridgedb/maven/test/meta/TestEntityMeta.java");
        assertTrue(expectedOutputFile1.toFile().exists());
        final CompilationUnit compilationUnit1 = StaticJavaParser.parse(expectedOutputFile1);
        assertEquals("TestEntityMeta", compilationUnit1.getPrimaryTypeName().orElseThrow());
        final ClassOrInterfaceDeclaration testEntityMeta = compilationUnit1.findAll(ClassOrInterfaceDeclaration.class).getFirst();
        assertFalse(testEntityMeta.isAnnotationPresent("NullMarked"));
        assertFalse(testEntityMeta.isAnnotationPresent("NullUnmarked"));

        final Path expectedOutputFile2 = Paths.get("target/generated-sources/java/org/litebridgedb/maven/test/meta/TestRelatedEntityMeta.java");
        assertTrue(expectedOutputFile2.toFile().exists());
        final CompilationUnit compilationUnit2 = StaticJavaParser.parse(expectedOutputFile2);
        assertEquals("TestRelatedEntityMeta", compilationUnit2.getPrimaryTypeName().orElseThrow());
        final ClassOrInterfaceDeclaration TestRelatedEntityMeta = compilationUnit1.findAll(ClassOrInterfaceDeclaration.class).getFirst();
        assertFalse(TestRelatedEntityMeta.isAnnotationPresent("NullMarked"));
        assertFalse(TestRelatedEntityMeta.isAnnotationPresent("NullUnmarked"));
    }

    @Test
    @DisplayName("Output classname prefix/suffix")
    @InjectMojo(goal = "metamodel", pom = "classpath:/metamodel/pom-prefixSuffix.xml")
    void execute_classNamePrefixSuffix(final MetamodelMojo metamodelMojo) throws Exception {
        // When
        metamodelMojo.execute();

        // Then
        final Path expectedOutputFile1 = Paths.get("target/generated-sources/java/org/litebridgedb/maven/test/meta/MTestEntity.java");
        assertTrue(expectedOutputFile1.toFile().exists());
        final CompilationUnit compilationUnit1 = StaticJavaParser.parse(expectedOutputFile1);
        assertEquals("MTestEntity", compilationUnit1.getPrimaryTypeName().orElseThrow());


        final Path expectedOutputFile2 = Paths.get("target/generated-sources/java/org/litebridgedb/maven/test/meta/MTestRelatedEntity.java");
        assertTrue(expectedOutputFile2.toFile().exists());
        final CompilationUnit compilationUnit2 = StaticJavaParser.parse(expectedOutputFile2);
        assertEquals("MTestRelatedEntity", compilationUnit2.getPrimaryTypeName().orElseThrow());
    }

    @Test
    @DisplayName("DTO metamodels")
    @InjectMojo(goal = "metamodel", pom = "classpath:/metamodel/pom-dtos.xml")
    void execute_dtos(final MetamodelMojo metamodelMojo) throws Exception {
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
    @DisplayName("Input package not found")
    @InjectMojo(goal = "metamodel", pom = "classpath:/metamodel/pom-packageNotFound.xml")
    void execute_inputPackageNotFound(final MetamodelMojo metamodelMojo) throws Exception {
        // When
        final MojoExecutionException result = assertThrows(MojoExecutionException.class, () -> metamodelMojo.execute());

        // Then
        assertTrue(result.getMessage().startsWith("Package not found: org.nonexistent; searched dirs: "));
    }

    @Test
    @DisplayName("Input configuration not set")
    @InjectMojo(goal = "metamodel")
    @MojoParameters({
            @MojoParameter(name = "outputPackage", value = "org.litebridgedb.maven.test.meta")
    })
    void execute_inputConfigNotSet(final MetamodelMojo metamodelMojo) throws Exception {
        // When
        final MojoExecutionException result = assertThrows(MojoExecutionException.class, () -> metamodelMojo.execute());

        // Then
        assertEquals("No input configuration provided", result.getMessage());
    }

    @Test
    @DisplayName("Skip enabled")
    @InjectMojo(goal = "metamodel")
    @MojoParameters({
            @MojoParameter(name = "skip", value = "true")
    })
    void execute_skip(final MetamodelMojo metamodelMojo) throws Exception {
        // When
        metamodelMojo.execute();
    }
}
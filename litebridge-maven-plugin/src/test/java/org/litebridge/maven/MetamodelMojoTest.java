package org.litebridge.maven;

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
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.litebridge.orm.meta.NumericQueryField;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.meta.StringQueryField;

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
        final Path packageInfoPath = Paths.get("target/generated-sources/java/org/litebridge/maven/test/meta/package-info.java");
        assertTrue(packageInfoPath.toFile().exists());
        final CompilationUnit packageInfoCu = StaticJavaParser.parse(packageInfoPath);
        assertFalse(packageInfoCu.getPackageDeclaration().orElseThrow()
                .isAnnotationPresent("NullMarked"));

        final CompilationUnit compilationUnit1 = getMetaModel("TestEntityMeta");
        final List<FieldDeclaration> fieldDeclarations1 = getMetaModelFields(compilationUnit1);
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

        final CompilationUnit compilationUnit2 = getMetaModel("TestRelatedEntityMeta");
        final List<FieldDeclaration> fieldDeclarations2 = getMetaModelFields(compilationUnit2);
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

        assertFalse(Paths.get("target/generated-sources/java/org/litebridge/maven/test/meta/TestDtoMeta.java")
                .toFile().exists());
        assertFalse(Paths.get("target/generated-sources/java/org/litebridge/maven/test/meta/TestRelatedDtoMeta.java")
                .toFile().exists());
    }

    @Test
    @DisplayName("JSpecify: defaults")
    @InjectMojo(goal = "metamodel", pom = "classpath:/metamodel/pom-jspecify.xml")
    void execute_jSpecify(final MetamodelMojo metamodelMojo) throws Exception {
        // When
        metamodelMojo.execute();

        // Then
        final Path packageInfoPath = Paths.get("target/generated-sources/java/org/litebridge/maven/test/meta/package-info.java");
        assertTrue(packageInfoPath.toFile().exists());
        final CompilationUnit packageInfoCu = StaticJavaParser.parse(packageInfoPath);
        assertTrue(packageInfoCu.getPackageDeclaration().orElseThrow()
                .isAnnotationPresent("NullMarked"));

        final CompilationUnit compilationUnit1 = getMetaModel("TestEntityMeta");
        final ClassOrInterfaceDeclaration testEntityMeta = compilationUnit1.findAll(ClassOrInterfaceDeclaration.class).getFirst();
        assertFalse(testEntityMeta.isAnnotationPresent("NullMarked"));
        assertFalse(testEntityMeta.isAnnotationPresent("NullUnmarked"));

        getMetaModel("TestRelatedEntityMeta");
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
        final Path packageInfoPath = Paths.get("target/generated-sources/java/org/litebridge/maven/test/meta/package-info.java");
        assertTrue(packageInfoPath.toFile().exists());
        final CompilationUnit packageInfoCu = StaticJavaParser.parse(packageInfoPath);
        assertTrue(packageInfoCu.getPackageDeclaration().orElseThrow()
                .isAnnotationPresent("NullUnmarked"));

        final CompilationUnit compilationUnit1 = getMetaModel("TestEntityMeta");
        final ClassOrInterfaceDeclaration testEntityMeta = compilationUnit1.findAll(ClassOrInterfaceDeclaration.class).getFirst();
        assertFalse(testEntityMeta.isAnnotationPresent("NullMarked"));
        assertFalse(testEntityMeta.isAnnotationPresent("NullUnmarked"));

        getMetaModel("TestRelatedEntityMeta");
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
        getMetaModel("MTestEntity");
        getMetaModel("MTestRelatedEntity");
    }

    @Test
    @DisplayName("DTO metamodels")
    @InjectMojo(goal = "metamodel", pom = "classpath:/metamodel/pom-dtos.xml")
    void execute_dtos(final MetamodelMojo metamodelMojo) throws Exception {
        // When
        metamodelMojo.execute();

        // Then
        final CompilationUnit compilationUnit1 = getMetaModel("TestDtoMeta");
        final List<FieldDeclaration> fieldDeclarations1 = getMetaModelFields(compilationUnit1);
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

        final CompilationUnit compilationUnit2 = getMetaModel("TestRelatedDtoMeta");
        final List<FieldDeclaration> fieldDeclarations2 = getMetaModelFields(compilationUnit2);
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
            @MojoParameter(name = "outputPackage", value = "org.litebridge.maven.test.meta")
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

    private static @NonNull CompilationUnit getMetaModel(final String className) throws IOException {
        final Path metamodelFile = Paths.get("target/generated-sources/java/org/litebridge/maven/test/meta/%s.java".formatted(className));
        assertTrue(metamodelFile.toFile().exists());
        final CompilationUnit compilationUnit = StaticJavaParser.parse(metamodelFile);
        assertEquals(className, compilationUnit.getPrimaryTypeName().orElseThrow());
        return compilationUnit;
    }

    private static List<FieldDeclaration> getMetaModelFields(final CompilationUnit compilationUnit) {
        return compilationUnit.findAll(FieldDeclaration.class, fieldDeclaration ->
                fieldDeclaration.isPublic()
                        && fieldDeclaration.isStatic()
                        && fieldDeclaration.isFinal());
    }
}
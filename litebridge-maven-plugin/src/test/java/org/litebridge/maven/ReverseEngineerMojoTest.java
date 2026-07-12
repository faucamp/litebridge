package org.litebridge.maven;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import org.apache.maven.api.di.Provides;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.flywaydb.core.Flyway;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.litebridge.orm.annotation.AllowInterface;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MojoTest
class ReverseEngineerMojoTest {

    private Flyway flyway;

    @Provides
    private Log initLog() {
        return new DebugMojoLog(ReverseEngineerMojo.class);
    }

    @BeforeEach
    void beforeEach() throws IOException {
        DirUtil.deleteDirectoryRecursively(Paths.get("target/generated-sources"));
    }

    @Test
    @DisplayName("Reverse engineer")
    @InjectMojo(goal = "reverse-engineer", pom = "classpath:/reverse/pom.xml")
    void execute(final ReverseEngineerMojo reverseEngineerMojo) throws Exception {
        final ExecuteResult result = executeImpl(reverseEngineerMojo);

        assertTrue(result.person.getImports().stream().noneMatch(importDeclaration ->
                importDeclaration.getNameAsString().equals("org.jspecify.annotations.Nullable")));
        assertTrue(result.person.getImports().stream().noneMatch(importDeclaration ->
                importDeclaration.getNameAsString().equals("org.jspecify.annotations.NullMarked")));
        final ClassOrInterfaceDeclaration person = result.person.getClassByName("PersonEntity").orElseThrow();
        assertFalse(person.isAnnotationPresent(NullMarked.class));
        person.getFields().forEach(field -> assertFalse(field.isAnnotationPresent(Nullable.class)));
        person.getMethods().forEach(method -> assertFalse(method.isAnnotationPresent(Nullable.class)));

        assertTrue(result.account.getImports().stream().noneMatch(importDeclaration ->
                importDeclaration.getNameAsString().equals("org.jspecify.annotations.Nullable")));
        assertTrue(result.account.getImports().stream().noneMatch(importDeclaration ->
                importDeclaration.getNameAsString().equals("org.jspecify.annotations.NullMarked")));
        final ClassOrInterfaceDeclaration account = result.account.getClassByName("Account").orElseThrow();
        assertFalse(account.isAnnotationPresent(NullMarked.class));
        account.getFields().forEach(field -> assertFalse(field.isAnnotationPresent(Nullable.class)));
        account.getMethods().forEach(method -> assertFalse(method.isAnnotationPresent(Nullable.class)));

        assertTrue(result.address.getImports().stream().noneMatch(importDeclaration ->
                importDeclaration.getNameAsString().equals("org.jspecify.annotations.Nullable")));
        assertTrue(result.address.getImports().stream().noneMatch(importDeclaration ->
                importDeclaration.getNameAsString().equals("org.jspecify.annotations.NullMarked")));
        final ClassOrInterfaceDeclaration address = result.address.getClassByName("Address").orElseThrow();
        assertFalse(address.isAnnotationPresent(NullMarked.class));
        address.getFields().forEach(field -> assertFalse(field.isAnnotationPresent(Nullable.class)));
        address.getMethods().forEach(method -> assertFalse(method.isAnnotationPresent(Nullable.class)));
    }

    @Test
    @DisplayName("Reverse engineer")
    @InjectMojo(goal = "reverse-engineer", pom = "classpath:/reverse/pom-allowInterface.xml")
    void execute_allowInterface(final ReverseEngineerMojo reverseEngineerMojo) throws Exception {
        final ExecuteResult result = executeImpl(reverseEngineerMojo);

        assertTrue(result.person.getImports().stream().noneMatch(importDeclaration ->
                importDeclaration.getNameAsString().equals("org.jspecify.annotations.AllowInterface")));
        final ClassOrInterfaceDeclaration person = result.person.getClassByName("PersonEntity").orElseThrow();
        assertTrue(person.isAnnotationPresent(AllowInterface.class));
        assertEquals("org.litebridge.maven.test.TestInterface.class", person.getAnnotationByClass(AllowInterface.class).orElseThrow().getChildNodes().get(1).toString());
    }

    @Test
    @DisplayName("JSpecify defaults: Strict Java nullability")
    @InjectMojo(goal = "reverse-engineer", pom = "classpath:/reverse/pom-jspecify.xml")
    void execute_jspecify(final ReverseEngineerMojo reverseEngineerMojo) throws Exception {
        final ExecuteResult result = executeImpl(reverseEngineerMojo);

        assertTrue(result.person.getImports().stream().noneMatch(importDeclaration ->
                importDeclaration.getNameAsString().equals("org.jspecify.annotations.NullMarked")));
        final ClassOrInterfaceDeclaration person = result.person.getClassByName("PersonEntity").orElseThrow();
        assertFalse(person.isAnnotationPresent(NullMarked.class));

        person.getFields().forEach(field -> {
            final String fieldName = field.getVariable(0).getNameAsString();

            if (fieldName.equals("age")) {
                // Note that "age" is nullable on a database level, but the SQL type mapping for the entity specifies a primitive int=
                assertFalse(field.isAnnotationPresent(Nullable.class), "Field must not be @Nullable: " + field);
            } else {
                assertTrue(field.isAnnotationPresent(Nullable.class), "Field must be @Nullable: " + field);
            }
        });

        person.getMethods().stream()
                .filter(method -> method.getNameAsString().startsWith("get"))
                .forEach(getter -> {
                    switch (getter.getNameAsString()) {
                        case "getAge" -> assertFalse(getter.isAnnotationPresent(Nullable.class),
                                "Getter must not be @Nullable: " + getter.getNameAsString());
                        default -> assertTrue(getter.isAnnotationPresent(Nullable.class),
                                "Getter must be @Nullable: " + getter.getNameAsString());
                    }
                });

        person.getMethods().stream()
                .filter(method -> method.getNameAsString().startsWith("set"))
                .forEach(setter -> {
                    switch (setter.getNameAsString()) {
                        case "setSurname", "setEyeColour", "setAccounts", "setAddresses" ->
                                assertTrue(setter.getParameter(0).isAnnotationPresent(Nullable.class),
                                        "Setter parameter must be @Nullable: " + setter.getNameAsString());
                        default -> assertFalse(setter.getParameter(0).isAnnotationPresent(Nullable.class),
                                "Setter parameter must not be @Nullable: " + setter.getNameAsString());
                    }
                });

        assertTrue(result.account.getImports().stream().noneMatch(importDeclaration ->
                importDeclaration.getNameAsString().equals("org.jspecify.annotations.NullMarked")));
        final ClassOrInterfaceDeclaration account = result.account.getClassByName("Account").orElseThrow();
        assertFalse(account.isAnnotationPresent(NullMarked.class));

        account.getFields().forEach(field -> {
            final String fieldName = field.getVariable(0).getNameAsString();

            if (fieldName.equals("active")) {
                assertFalse(field.isAnnotationPresent(Nullable.class), "Field must not be @Nullable: " + field);
            } else {
                assertTrue(field.isAnnotationPresent(Nullable.class), "Field must be @Nullable: " + field);
            }
        });

        account.getMethods().stream()
                .filter(method -> method.getNameAsString().startsWith("get"))
                .forEach(getter -> {
                    if (getter.getNameAsString().equals("getActive")) {
                        assertFalse(getter.isAnnotationPresent(Nullable.class),
                                "Getter must not be @Nullable: " + getter);
                    } else {
                        assertTrue(getter.isAnnotationPresent(Nullable.class),
                                "Getter must be @Nullable: " + getter);
                    }
                });

        account.getMethods().stream()
                .filter(method -> method.getNameAsString().startsWith("set"))
                .forEach(setter -> {
                    if (setter.getNameAsString().equals("setFlagged")) {
                        assertTrue(setter.getParameter(0).isAnnotationPresent(Nullable.class),
                                "Setter parameter must be @Nullable: " + setter.getNameAsString());
                    } else {
                        assertFalse(setter.getParameter(0).isAnnotationPresent(Nullable.class),
                                "Setter parameter must not be @Nullable: " + setter.getNameAsString());
                    }
                });

        assertTrue(result.address.getImports().stream().noneMatch(importDeclaration ->
                importDeclaration.getNameAsString().equals("org.jspecify.annotations.NullMarked")));
        final ClassOrInterfaceDeclaration address = result.address.getClassByName("Address").orElseThrow();
        assertFalse(address.isAnnotationPresent(NullMarked.class));

        address.getFields().forEach(field -> {
            assertTrue(field.isAnnotationPresent(Nullable.class), "Field must be @Nullable: " + field);
        });

        address.getMethods().stream()
                .filter(method -> method.getNameAsString().startsWith("get"))
                .forEach(getter -> {
                    assertTrue(getter.isAnnotationPresent(Nullable.class),
                            "Getter must be @Nullable: " + getter);

                });

        address.getMethods().stream()
                .filter(method -> method.getNameAsString().startsWith("set"))
                .forEach(setter -> {
                    if (setter.getNameAsString().equals("setPersonEntities")) {
                        assertTrue(setter.getParameter(0).isAnnotationPresent(Nullable.class),
                                "Setter parameter must be @Nullable: " + setter.getNameAsString());
                    } else {
                        assertFalse(setter.getParameter(0).isAnnotationPresent(Nullable.class),
                                "Setter parameter must not be @Nullable: " + setter.getNameAsString());
                    }
                });
    }

    @Test
    @DisplayName("JSpecify: no package-info.java")
    @InjectMojo(goal = "reverse-engineer", pom = "classpath:/reverse/pom-jspecify-noPackageInfo.xml")
    void execute_jspecify_noPackageInfo(final ReverseEngineerMojo reverseEngineerMojo) throws Exception {
        final ExecuteResult result = executeImpl(reverseEngineerMojo);

        final ClassOrInterfaceDeclaration person = result.person.getClassByName("PersonEntity").orElseThrow();
        assertTrue(person.isAnnotationPresent(NullMarked.class));

        person.getFields().forEach(field -> {
            final String fieldName = field.getVariable(0).getNameAsString();

            if (fieldName.equals("age")) {
                // Note that "age" is nullable on a database level, but the SQL type mapping for the entity specifies a primitive int=
                assertFalse(field.isAnnotationPresent(Nullable.class), "Field must not be @Nullable: " + field);
            } else {
                assertTrue(field.isAnnotationPresent(Nullable.class), "Field must be @Nullable: " + field);
            }
        });

        person.getMethods().stream()
                .filter(method -> method.getNameAsString().startsWith("get"))
                .forEach(getter -> {
                    switch (getter.getNameAsString()) {
                        case "getAge" -> assertFalse(getter.isAnnotationPresent(Nullable.class),
                                "Getter must not be @Nullable: " + getter.getNameAsString());
                        default -> assertTrue(getter.isAnnotationPresent(Nullable.class),
                                "Getter must be @Nullable: " + getter.getNameAsString());
                    }
                });

        person.getMethods().stream()
                .filter(method -> method.getNameAsString().startsWith("set"))
                .forEach(setter -> {
                    switch (setter.getNameAsString()) {
                        case "setSurname", "setEyeColour", "setAccounts", "setAddresses" ->
                                assertTrue(setter.getParameter(0).isAnnotationPresent(Nullable.class),
                                        "Setter parameter must be @Nullable: " + setter.getNameAsString());
                        default -> assertFalse(setter.getParameter(0).isAnnotationPresent(Nullable.class),
                                "Setter parameter must not be @Nullable: " + setter.getNameAsString());
                    }
                });

        final ClassOrInterfaceDeclaration account = result.account.getClassByName("Account").orElseThrow();
        assertTrue(account.isAnnotationPresent(NullMarked.class));

        account.getFields().forEach(field -> {
            final String fieldName = field.getVariable(0).getNameAsString();

            if (fieldName.equals("active")) {
                assertFalse(field.isAnnotationPresent(Nullable.class), "Field must not be @Nullable: " + field);
            } else {
                assertTrue(field.isAnnotationPresent(Nullable.class), "Field must be @Nullable: " + field);
            }
        });

        account.getMethods().stream()
                .filter(method -> method.getNameAsString().startsWith("get"))
                .forEach(getter -> {
                    if (getter.getNameAsString().equals("getActive")) {
                        assertFalse(getter.isAnnotationPresent(Nullable.class),
                                "Getter must not be @Nullable: " + getter);
                    } else {
                        assertTrue(getter.isAnnotationPresent(Nullable.class),
                                "Getter must be @Nullable: " + getter);
                    }
                });

        account.getMethods().stream()
                .filter(method -> method.getNameAsString().startsWith("set"))
                .forEach(setter -> {
                    if (setter.getNameAsString().equals("setFlagged")) {
                        assertTrue(setter.getParameter(0).isAnnotationPresent(Nullable.class),
                                "Setter parameter must be @Nullable: " + setter.getNameAsString());
                    } else {
                        assertFalse(setter.getParameter(0).isAnnotationPresent(Nullable.class),
                                "Setter parameter must not be @Nullable: " + setter.getNameAsString());
                    }
                });

        assertTrue(result.address.getImports().stream().anyMatch(importDeclaration ->
                importDeclaration.getNameAsString().equals("org.jspecify.annotations.NullMarked")));
        final ClassOrInterfaceDeclaration address = result.address.getClassByName("Address").orElseThrow();
        assertTrue(address.isAnnotationPresent(NullMarked.class));

        address.getFields().forEach(field -> {
            assertTrue(field.isAnnotationPresent(Nullable.class), "Field must be @Nullable: " + field);
        });

        address.getMethods().stream()
                .filter(method -> method.getNameAsString().startsWith("get"))
                .forEach(getter -> {
                    assertTrue(getter.isAnnotationPresent(Nullable.class),
                            "Getter must be @Nullable: " + getter);

                });

        address.getMethods().stream()
                .filter(method -> method.getNameAsString().startsWith("set"))
                .forEach(setter -> {
                    if (setter.getNameAsString().equals("setPersonEntities")) {
                        assertTrue(setter.getParameter(0).isAnnotationPresent(Nullable.class),
                                "Setter parameter must be @Nullable: " + setter.getNameAsString());
                    } else {
                        assertFalse(setter.getParameter(0).isAnnotationPresent(Nullable.class),
                                "Setter parameter must not be @Nullable: " + setter.getNameAsString());
                    }
                });
    }

    @Test
    @DisplayName("JSpecify: use database NULLABLE attribute")
    @InjectMojo(goal = "reverse-engineer", pom = "classpath:/reverse/pom-jspecify-databaseNullable.xml")
    void execute_jspecify_databaseNullable(final ReverseEngineerMojo reverseEngineerMojo) throws Exception {
        final ExecuteResult result = executeImpl(reverseEngineerMojo);

        final ClassOrInterfaceDeclaration person = result.person.getClassByName("PersonEntity").orElseThrow();
        assertFalse(person.isAnnotationPresent(NullMarked.class));

        person.getFields().forEach(field -> {
            final String fieldName = field.getVariable(0).getNameAsString();

            switch (fieldName) {
                // Note that "age" is nullable on a database level, but the SQL type mapping for the entity specifies a primitive int=
                case "surname", "eyeColour", "accounts", "addresses" ->
                        assertTrue(field.isAnnotationPresent(Nullable.class),
                                "Field must be @Nullable: " + field);
                default -> assertFalse(field.isAnnotationPresent(Nullable.class),
                        "Field must not be @Nullable: " + field);
            }
        });

        person.getMethods().stream()
                .filter(method -> method.getNameAsString().startsWith("get"))
                .forEach(getter -> {
                    switch (getter.getNameAsString()) {
                        case "getSurname", "getEyeColour", "getAccounts", "getAddresses" ->
                                assertTrue(getter.isAnnotationPresent(Nullable.class),
                                        "Getter must be @Nullable: " + getter.getNameAsString());
                        default -> assertFalse(getter.isAnnotationPresent(Nullable.class),
                                "Getter must not be @Nullable: " + getter.getNameAsString());
                    }
                });

        person.getMethods().stream()
                .filter(method -> method.getNameAsString().startsWith("set"))
                .forEach(setter -> {
                    switch (setter.getNameAsString()) {
                        case "setSurname", "setEyeColour", "setAccounts", "setAddresses" ->
                                assertTrue(setter.getParameter(0).isAnnotationPresent(Nullable.class),
                                        "Setter parameter must be @Nullable: " + setter.getNameAsString());
                        default -> assertFalse(setter.getParameter(0).isAnnotationPresent(Nullable.class),
                                "Setter parameter must not be @Nullable: " + setter.getNameAsString());
                    }
                });

        final ClassOrInterfaceDeclaration account = result.account.getClassByName("Account").orElseThrow();
        assertFalse(account.isAnnotationPresent(NullMarked.class));

        account.getFields().forEach(field -> {
            final String fieldName = field.getVariable(0).getNameAsString();

            if (fieldName.equals("flagged")) {
                assertTrue(field.isAnnotationPresent(Nullable.class),
                        "Field must be @Nullable: " + field);
            } else {
                assertFalse(field.isAnnotationPresent(Nullable.class),
                        "Field must not be @Nullable: " + field);
            }
        });

        account.getMethods().stream()
                .filter(method -> method.getNameAsString().startsWith("get"))
                .forEach(getter -> {
                    if (getter.getNameAsString().equals("getFlagged")) {
                        assertTrue(getter.isAnnotationPresent(Nullable.class),
                                "Getter must be @Nullable: " + getter.getNameAsString());
                    } else {
                        assertFalse(getter.isAnnotationPresent(Nullable.class),
                                "Getter must not be @Nullable: " + getter.getNameAsString());
                    }
                });

        account.getMethods().stream()
                .filter(method -> method.getNameAsString().startsWith("set"))
                .forEach(setter -> {
                    if (setter.getNameAsString().equals("setFlagged")) {
                        assertTrue(setter.getParameter(0).isAnnotationPresent(Nullable.class),
                                "Setter parameter must be @Nullable: " + setter.getNameAsString());
                    } else {
                        assertFalse(setter.getParameter(0).isAnnotationPresent(Nullable.class),
                                "Setter parameter must not be @Nullable: " + setter.getNameAsString());
                    }
                });

        assertTrue(result.address.getImports().stream().noneMatch(importDeclaration ->
                importDeclaration.getNameAsString().equals("org.jspecify.annotations.NullMarked")));
        final ClassOrInterfaceDeclaration address = result.address.getClassByName("Address").orElseThrow();
        assertFalse(address.isAnnotationPresent(NullMarked.class));

        address.getFields().forEach(field -> {
            final String fieldName = field.getVariable(0).getNameAsString();

            if (fieldName.equals("addressId") || fieldName.equals("address")) {
                assertFalse(field.isAnnotationPresent(Nullable.class), "Field must not be @Nullable: " + field);
            } else {
                assertTrue(field.isAnnotationPresent(Nullable.class), "Field must be @Nullable: " + field);
            }
        });

        address.getMethods().stream()
                .filter(method -> method.getNameAsString().startsWith("get"))
                .forEach(getter -> {
                    if (getter.getNameAsString().equals("getAddressId") || getter.getNameAsString().equals("getAddress")) {
                        assertFalse(getter.isAnnotationPresent(Nullable.class),
                                "Getter must not be @Nullable: " + getter);
                    } else {
                        assertTrue(getter.isAnnotationPresent(Nullable.class),
                                "Getter must be @Nullable: " + getter);
                    }

                });

        address.getMethods().stream()
                .filter(method -> method.getNameAsString().startsWith("set"))
                .forEach(setter -> {
                    if (setter.getNameAsString().equals("setPersonEntities")) {
                        assertTrue(setter.getParameter(0).isAnnotationPresent(Nullable.class),
                                "Setter parameter must be @Nullable: " + setter.getNameAsString());
                    } else {
                        assertFalse(setter.getParameter(0).isAnnotationPresent(Nullable.class),
                                "Setter parameter must not be @Nullable: " + setter.getNameAsString());
                    }
                });
    }

    private ExecuteResult executeImpl(final ReverseEngineerMojo reverseEngineerMojo) throws MojoExecutionException, IOException {
        // Given
        setupH2();

        // When
        reverseEngineerMojo.execute();

        // Then
        final Path personEntityFile = Paths.get("target/generated-sources/java/com/example/generated/PersonEntity.java");
        assertTrue(personEntityFile.toFile().exists());
        final CompilationUnit personCompilationUnit = StaticJavaParser.parse(personEntityFile);
        assertEquals("PersonEntity", personCompilationUnit.getPrimaryTypeName().orElseThrow());
        final List<FieldDeclaration> personFields = personCompilationUnit.findAll(FieldDeclaration.class, fieldDeclaration ->
                fieldDeclaration.isPrivate()
                        && !fieldDeclaration.isStatic()
                        && !fieldDeclaration.isFinal());
        assertEquals(7, personFields.size());

        for (FieldDeclaration fieldDeclaration : personFields) {
            switch (fieldDeclaration.getVariable(0).getNameAsString()) {
                case "id" -> assertEquals("Long", fieldDeclaration.getVariable(0).getType().toString());
                case "age" -> assertEquals("int", fieldDeclaration.getVariable(0).getType().toString());
                case "firstName", "surname", "eyeColour" ->
                        assertEquals("String", fieldDeclaration.getVariable(0).getType().toString());
                case "accounts" -> assertEquals("List<Account>", fieldDeclaration.getVariable(0).getType().toString());
                case "addresses" -> assertEquals("List<Address>", fieldDeclaration.getVariable(0).getType().toString());
                default -> fail("Unknown generated field: " + fieldDeclaration);
            }
        }

        final Path accountEntityFile = Paths.get("target/generated-sources/java/com/example/generated/Account.java");
        assertTrue(accountEntityFile.toFile().exists());
        final CompilationUnit accountCompilationUnit = StaticJavaParser.parse(accountEntityFile);
        assertEquals("Account", accountCompilationUnit.getPrimaryTypeName().orElseThrow());
        final List<FieldDeclaration> accountFields = accountCompilationUnit.findAll(FieldDeclaration.class, fieldDeclaration ->
                fieldDeclaration.isPrivate()
                        && !fieldDeclaration.isStatic()
                        && !fieldDeclaration.isFinal());
        assertEquals(6, accountFields.size());

        for (FieldDeclaration fieldDeclaration : accountFields) {
            switch (fieldDeclaration.getVariable(0).getNameAsString()) {
                case "id" -> assertEquals("Long", fieldDeclaration.getVariable(0).getType().toString());
                case "active" -> assertEquals("boolean", fieldDeclaration.getVariable(0).getType().toString());
                case "flagged" -> assertEquals("Boolean", fieldDeclaration.getVariable(0).getType().toString());
                case "balance" -> assertEquals("BigDecimal", fieldDeclaration.getVariable(0).getType().toString());
                case "owner" -> assertEquals("PersonEntity", fieldDeclaration.getVariable(0).getType().toString());
                case "accountName" -> assertEquals("String", fieldDeclaration.getVariable(0).getType().toString());
                default -> fail("Unknown generated field: " + fieldDeclaration);
            }
        }

        final Path addressEntityFile = Paths.get("target/generated-sources/java/com/example/generated/Address.java");
        assertTrue(addressEntityFile.toFile().exists());
        final CompilationUnit adressCompilationUnit = StaticJavaParser.parse(addressEntityFile);
        assertEquals("Address", adressCompilationUnit.getPrimaryTypeName().orElseThrow());
        final List<FieldDeclaration> addressFields = adressCompilationUnit.findAll(FieldDeclaration.class, fieldDeclaration ->
                fieldDeclaration.isPrivate()
                        && !fieldDeclaration.isStatic()
                        && !fieldDeclaration.isFinal());
        assertEquals(3, addressFields.size());

        for (FieldDeclaration fieldDeclaration : addressFields) {
            switch (fieldDeclaration.getVariable(0).getNameAsString()) {
                case "addressId" -> assertEquals("Long", fieldDeclaration.getVariable(0).getType().toString());
                case "address" -> assertEquals("String", fieldDeclaration.getVariable(0).getType().toString());
                case "personEntities" ->
                        assertEquals("List<PersonEntity>", fieldDeclaration.getVariable(0).getType().toString());
                default -> fail("Unknown generated field: " + fieldDeclaration);
            }
        }

        return new ExecuteResult(personCompilationUnit, accountCompilationUnit, adressCompilationUnit);
    }

    @Test
    void testExecute_Skip() throws Exception {
        ReverseEngineerMojo mojo = new ReverseEngineerMojo();
        Log log = mock(Log.class);
        mojo.setLog(log);
        setField(mojo, "skip", true);

        mojo.execute();

        verify(log).debug(argThat((CharSequence s) -> String.valueOf(s).contains("Skipping")));
    }

    @Test
    void testExecute_MandatoryConfigMissing() throws Exception {
        ReverseEngineerMojo mojo = new ReverseEngineerMojo();
        mojo.setLog(new DebugMojoLog(ReverseEngineerMojo.class));

        // database is null
        assertThrows(MojoExecutionException.class, mojo::execute);

        org.litebridge.maven.config.reverse.DatabaseConfig db = new org.litebridge.maven.config.reverse.DatabaseConfig();
        setField(mojo, "database", db);

        // input is null
        assertThrows(MojoExecutionException.class, mojo::execute);

        org.litebridge.maven.config.reverse.RevEngInputConfig input = new org.litebridge.maven.config.reverse.RevEngInputConfig();
        setField(mojo, "input", input);

        // output is null
        assertThrows(MojoExecutionException.class, mojo::execute);
    }

    @Test
    void testExecute_InvalidDatabaseProviderClass() throws Exception {
        ReverseEngineerMojo mojo = new ReverseEngineerMojo();
        mojo.setLog(new DebugMojoLog(ReverseEngineerMojo.class));

        org.litebridge.maven.config.reverse.DatabaseConfig db = new org.litebridge.maven.config.reverse.DatabaseConfig();
        db.setDatabaseProviderClass("java.lang.String");
        db.setUrl("jdbc:h2:mem:test_invalid");
        setField(mojo, "database", db);

        org.litebridge.maven.config.reverse.RevEngInputConfig input = new org.litebridge.maven.config.reverse.RevEngInputConfig();
        input.setTables(List.of("test"));
        setField(mojo, "input", input);

        org.litebridge.maven.config.reverse.RevEngOutputConfig output = new org.litebridge.maven.config.reverse.RevEngOutputConfig();
        setField(mojo, "output", output);

        assertThrows(IllegalArgumentException.class, mojo::execute);
    }

    @Test
    void testExecute_DatabaseProviderClassNotFound() throws Exception {
        ReverseEngineerMojo mojo = new ReverseEngineerMojo();
        mojo.setLog(new DebugMojoLog(ReverseEngineerMojo.class));

        org.litebridge.maven.config.reverse.DatabaseConfig db = new org.litebridge.maven.config.reverse.DatabaseConfig();
        db.setDatabaseProviderClass("com.nonexistent.Provider");
        setField(mojo, "database", db);

        org.litebridge.maven.config.reverse.RevEngInputConfig input = new org.litebridge.maven.config.reverse.RevEngInputConfig();
        setField(mojo, "input", input);

        org.litebridge.maven.config.reverse.RevEngOutputConfig output = new org.litebridge.maven.config.reverse.RevEngOutputConfig();
        setField(mojo, "output", output);

        assertThrows(IllegalArgumentException.class, mojo::execute);
    }

    /**
     * Setup H2 in-memory database
     */
    private void setupH2() {
        //
        final String url = "jdbc:h2:mem:lb;DB_CLOSE_DELAY=-1";
        final String user = "sa";
        final String password = "";
        configureDatabase(url, user, password);
    }

    private static void configureDatabase(final String url, final String user, final String password) {
        // Configure Flyway
        final Flyway flyway = Flyway.configure()
                .dataSource(url, user, password)
                .locations("classpath:db/migration")
                .load();

        // Run the migration
        flyway.migrate();
    }

    private record ExecuteResult(CompilationUnit person, CompilationUnit account, CompilationUnit address) {
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
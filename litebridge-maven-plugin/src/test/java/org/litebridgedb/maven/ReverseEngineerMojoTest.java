package org.litebridgedb.maven;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import org.apache.maven.api.di.Provides;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.plugin.logging.Log;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
    @InjectMojo(goal = "reverse-engineer", pom = "classpath:/reverse-engineer-pom.xml")
    void execute(final ReverseEngineerMojo reverseEngineerMojo) throws Exception {
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
        assertEquals(5, personFields.size());

        for (FieldDeclaration fieldDeclaration : personFields) {
            switch (fieldDeclaration.getVariable(0).getNameAsString()) {
                case "id" -> assertEquals("Long", fieldDeclaration.getVariable(0).getType().toString());
                case "age" -> assertEquals("int", fieldDeclaration.getVariable(0).getType().toString());
                case "firstName", "surname", "eyeColour" ->
                        assertEquals("String", fieldDeclaration.getVariable(0).getType().toString());
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
                case "owner" -> assertEquals("Long", fieldDeclaration.getVariable(0).getType().toString());
                case "accountName" -> assertEquals("String", fieldDeclaration.getVariable(0).getType().toString());
                default -> fail("Unknown generated field: " + fieldDeclaration);
            }
        }
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

    private void runFlywayMigration(final DataSource dataSource) {
        // Configure and run Flyway migration
        if (flyway == null) {
            flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .cleanDisabled(false)
                    .load();
        }

        flyway.migrate();
    }
}
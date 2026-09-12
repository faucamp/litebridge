package org.litebridge.db.oracle.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.oracle.OracleColumnIdentifierGenerator;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.UpdateColumn;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class OracleInsertSqlGeneratorTest {

    private OracleInsertSqlGenerator insertSqlGenerator;

    @BeforeEach
    void setUp() {
        final OracleColumnIdentifierGenerator columnIdentifierGenerator = new OracleColumnIdentifierGenerator();
        final OracleMathOperationGenerator mathOperationGenerator = new OracleMathOperationGenerator(columnIdentifierGenerator);
        insertSqlGenerator = new OracleInsertSqlGenerator(
                columnIdentifierGenerator,
                mathOperationGenerator,
                (table, connectionProvider) -> mock(TableMetaData.class));
    }

    @Test
    void generateSql_singleRow_withGeneratedValueAndBindPlaceholder() {
        // Given
        final Table table = new Table("TEST_TABLE");
        final Insert insert = new Insert(
                table,
                List.of(new UpdateColumn("ID", () -> "DEFAULT", null), new UpdateColumn("NAME")),
                1,
                false);

        // When
        final String result = insertSqlGenerator.generateSql(insert, mock(ConnectionProvider.class));

        // Then
        assertEquals("INSERT INTO TEST_TABLE (ID, NAME) VALUES (DEFAULT, ?)", result);
    }

    @Test
    void generateSql_multipleRows_restrictsToOneRowForBatchedInserts() {
        // Given
        final Table table = new Table("TEST_TABLE");
        final Insert insert = new Insert(
                table,
                List.of(new UpdateColumn("ID")),
                3,
                false);

        // When
        final String result = insertSqlGenerator.generateSql(insert, mock(ConnectionProvider.class));

        // Then
        assertEquals("INSERT INTO TEST_TABLE (ID) VALUES (?)", result);
    }

    @Test
    void createInsertAllClause_throwsWhenReturnGeneratedKeysIsTrue() {
        // Given
        final Table table = new Table("TEST_TABLE");
        final Insert insert = new Insert(
                table,
                List.of(new UpdateColumn("ID")),
                1,
                true);

        // When / Then
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> insertSqlGenerator.createInsertAllClause(List.of(insert)));
        assertEquals("INSERT ALL cannot return generated keys", exception.getMessage());
    }

    @Test
    void createInsertAllClause_singleInsertSingleRow() {
        // Given
        final Table table = new Table("ACCOUNT");
        final Insert insert = new Insert(
                table,
                List.of(new UpdateColumn("ACCOUNT_ID"), new UpdateColumn("NAME")),
                1,
                false);

        // When
        final String sql = insertSqlGenerator.createInsertAllClause(List.of(insert));

        // Then
        assertEquals("INSERT ALL INTO ACCOUNT (ACCOUNT_ID, NAME) VALUES (?, ?) SELECT * FROM DUAL", sql);
    }

    @Test
    void createInsertAllClause_multipleRowsAndMultipleInserts() {
        // Given
        final Table tableAccount = new Table("ACCOUNT");
        final Insert insertAccount = new Insert(
                tableAccount,
                List.of(new UpdateColumn("ACCOUNT_ID", () -> "SEQ.NEXTVAL", null), new UpdateColumn("NAME")),
                2,
                false);

        final Table tableAudit = new Table("AUDIT_LOG");
        final Insert insertAudit = new Insert(
                tableAudit,
                List.of(new UpdateColumn("LOG_ID")),
                1,
                false);

        // When
        final String sql = insertSqlGenerator.createInsertAllClause(List.of(insertAccount, insertAudit));

        // Then
        assertEquals(
                "INSERT ALL INTO ACCOUNT (ACCOUNT_ID, NAME) VALUES (SEQ.NEXTVAL, ?) " +
                        "INTO ACCOUNT (ACCOUNT_ID, NAME) VALUES (SEQ.NEXTVAL, ?) " +
                        "INTO AUDIT_LOG (LOG_ID) VALUES (?) " +
                        "SELECT * FROM DUAL",
                sql);
    }
}

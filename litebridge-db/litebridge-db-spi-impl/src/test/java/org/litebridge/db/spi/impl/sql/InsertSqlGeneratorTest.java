package org.litebridge.db.spi.impl.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.UpdateColumn;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.litebridge.db.spi.impl.sql.TestUtil.createTestTable;
import static org.mockito.Mockito.mock;

class InsertSqlGeneratorTest {

    private InsertSqlGenerator insertSqlGenerator;

    @BeforeEach
    void beforeEach() {
        insertSqlGenerator = new InsertSqlGenerator(new ColumnIdentifierGenerator(), (table, connectionProvider) -> mock(TableMetaData.class));
    }

    @Test
    void prepareSql_generatedValueAndBindPlaceholder() {
        // Given
        final Insert insert = new Insert(
                createTestTable(),
                List.of(new UpdateColumn("TEST_ID", "DEFAULT"), new UpdateColumn("TEST_COLUMN")),
                1,
                false);

        // When
        final String result = insertSqlGenerator.prepareSql(insert, mock(ConnectionProvider.class));

        // Then
        assertEquals("INSERT INTO TEST_SCHEMA.TEST_TABLE (TEST_ID, TEST_COLUMN) VALUES (DEFAULT, ?)", result);
    }

    @Test
    void prepareSql_multipleRows() {
        // Given
        final Insert insert = new Insert(
                createTestTable(),
                List.of(new UpdateColumn("TEST_COLUMN")),
                2,
                false);

        // When
        final String result = insertSqlGenerator.prepareSql(insert, mock(ConnectionProvider.class));

        // Then
        assertEquals("INSERT INTO TEST_SCHEMA.TEST_TABLE (TEST_COLUMN) VALUES (?), (?)", result);
    }
}
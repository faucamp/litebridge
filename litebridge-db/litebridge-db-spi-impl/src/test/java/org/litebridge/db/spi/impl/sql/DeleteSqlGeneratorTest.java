package org.litebridge.db.spi.impl.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.function.SelectColumn;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.LogicCondition;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Delete;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.litebridge.db.spi.impl.sql.TestUtil.createTestColumn;
import static org.litebridge.db.spi.impl.sql.TestUtil.createTestTable;
import static org.mockito.Mockito.mock;

class DeleteSqlGeneratorTest {

    private DeleteSqlGenerator deleteSqlGenerator;

    @BeforeEach
    void beforeEach() {
        deleteSqlGenerator = new DeleteSqlGenerator(new ColumnIdentifierGenerator(), (table, connectionProvider) -> mock(TableMetaData.class));
    }

    @Test
    void prepareSql_emptyWhere() {
        // Given
        final Delete delete = new Delete(createTestTable(), new ConditionGroup(List.of()));

        // When
        final String result = deleteSqlGenerator.prepareSql(delete, mock(ConnectionProvider.class));

        // Then
        assertEquals("DELETE FROM TEST_SCHEMA.TEST_TABLE", result);
    }

    @Test
    void prepareSql_nonEmptyWhere() {
        // Given
        final LogicCondition condition = new LogicCondition(
                new SelectColumn(createTestColumn(), deleteSqlGenerator.columnIdentifierGenerator),
                Operator.EQ,
                "value");
        final Delete delete = new Delete(createTestTable(), new ConditionGroup(condition));

        // When
        final String result = deleteSqlGenerator.prepareSql(delete, mock(ConnectionProvider.class));

        // Then
        assertEquals("DELETE FROM TEST_SCHEMA.TEST_TABLE WHERE TEST_TABLE.TEST_COLUMN = ?", result);
    }
}
package org.litebridge.db.spi.impl.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.LogicCondition;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Delete;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.litebridge.db.spi.impl.sql.TestUtil.createLiteralExpression;
import static org.litebridge.db.spi.impl.sql.TestUtil.createSelectColumn;
import static org.litebridge.db.spi.impl.sql.TestUtil.createTestTable;
import static org.mockito.Mockito.mock;

class DeleteSqlGeneratorTest {

    private DeleteSqlGenerator deleteSqlGenerator;

    @BeforeEach
    void beforeEach() {
        final LabelGenerator labelGenerator = new LabelGenerator();
        final MathOperationGenerator mathOperationGenerator = new MathOperationGenerator(labelGenerator);
        deleteSqlGenerator = new DeleteSqlGenerator(labelGenerator, mathOperationGenerator, (table, connectionProvider) -> mock(TableMetaData.class));
    }

    @Test
    void generateSql_emptyWhere() {
        // Given
        final Delete delete = new Delete(createTestTable(), new ConditionGroup(List.of()));

        // When
        final String result = deleteSqlGenerator.generateSql(delete, mock(ConnectionProvider.class));

        // Then
        assertEquals("DELETE FROM TEST_SCHEMA.TEST_TABLE", result);
    }

    @Test
    void generateSql_nonEmptyWhere() {
        // Given
        final LogicCondition condition = new LogicCondition(
                createSelectColumn(),
                Operator.EQ,
                createLiteralExpression("value"));
        final Delete delete = new Delete(createTestTable(), new ConditionGroup(condition));

        // When
        final String result = deleteSqlGenerator.generateSql(delete, mock(ConnectionProvider.class));

        // Then
        assertEquals("DELETE FROM TEST_SCHEMA.TEST_TABLE WHERE TEST_TABLE.TEST_COLUMN = ?", result);
    }
}
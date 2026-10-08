package org.litebridge.db.spi.impl.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.impl.expression.SelectColumn;
import org.litebridge.db.spi.math.MathOperator;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.LogicCondition;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Update;
import org.litebridge.db.spi.update.UpdateColumn;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.litebridge.db.spi.impl.sql.TestUtil.createTestColumn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UpdateSqlGeneratorTest {

    @Mock
    private BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData;
    private UpdateSqlGenerator updateSqlGenerator;

    @BeforeEach
    void beforeEach() {
        final LabelGenerator labelGenerator = new LabelGenerator();
        final MathOperationGenerator mathOperationGenerator = new MathOperationGenerator(labelGenerator);
        updateSqlGenerator = new UpdateSqlGenerator(labelGenerator, mathOperationGenerator, ensureTableMetaData);
    }

    @Test
    void createMathOperation() {
        // Given
        final Column column = createTestColumn();
        final MathOperator mathOperator = MathOperator.ADD;

        // When
        final String result = updateSqlGenerator.createMathOperation(column.name(), mathOperator);

        // Then
        assertEquals("TEST_COLUMN + ?", result);
    }

    @Test
    void generateSql_withWhereClause() {
        // Given
        final Table table = new Table(null, "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "NAME");
        final SelectColumn selectCol = TestUtil.createSelectColumn(column);
        final LogicCondition whereCondition = new LogicCondition(
                selectCol,
                Operator.EQ,
                TestUtil.createLiteralExpression("Alice"));
        final ConditionGroup whereGroup = new ConditionGroup(List.of(whereCondition));

        final Update update = new Update(
                table,
                List.of(
                        new UpdateColumn("NAME"),
                        new UpdateColumn("AGE", null, MathOperator.ADD)),
                whereGroup);

        // When
        final String sql = updateSqlGenerator.generateSql(update, mock(ConnectionProvider.class));

        // Then
        assertEquals("UPDATE TEST_SCHEMA.TEST_TABLE SET NAME = ?, AGE = AGE + ? WHERE TEST_TABLE.NAME = ?", sql);
    }

    @Test
    void generateSql_withoutWhereClause() {
        // Given
        final Table table = new Table("TEST_TABLE");
        final Update update = new org.litebridge.db.spi.update.Update(
                table,
                List.of(new UpdateColumn("STATUS")),
                new ConditionGroup(List.of()));

        // When
        final String sql = updateSqlGenerator.generateSql(update, mock(ConnectionProvider.class));

        // Then
        assertEquals("UPDATE TEST_TABLE SET STATUS = ?", sql);
    }
}
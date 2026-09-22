package org.litebridge.db.spi.impl.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.LogicCondition;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Merge;
import org.litebridge.db.spi.update.UpdateColumn;

import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.litebridge.db.spi.impl.sql.TestUtil.createLiteralExpression;
import static org.litebridge.db.spi.impl.sql.TestUtil.createSelectColumn;
import static org.litebridge.db.spi.impl.sql.TestUtil.createTestColumn;
import static org.litebridge.db.spi.impl.sql.TestUtil.createTestTable;
import static org.mockito.Mockito.mock;

class MergeSqlGeneratorTest {

    private MergeSqlGenerator mergeSqlGenerator;

    @BeforeEach
    void beforeEach() {
        final LabelGenerator labelGenerator = new LabelGenerator();
        final MathOperationGenerator mathOperationGenerator = new MathOperationGenerator(labelGenerator);
        final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData = (table, connectionProvider) -> mock(TableMetaData.class);
        final SelectSqlGenerator selectSqlGenerator = new SelectSqlGenerator(labelGenerator, mathOperationGenerator, ensureTableMetaData);
        mergeSqlGenerator = new MergeSqlGenerator(selectSqlGenerator,
                labelGenerator,
                mathOperationGenerator,
                ensureTableMetaData);
    }

    @Test
    void generateSql_matchedUpdateAndDelete() {
        // Given
        final ConditionGroup on = new ConditionGroup(new LogicCondition(
                createSelectColumn(createTestColumn("TEST_ID")),
                Operator.EQ,
                createLiteralExpression(1)));
        final Merge merge = new Merge(
                createTestTable(),
                new Table("SOURCE_TABLE"),
                on,
                List.of(
                        new Merge.WhenMatched<>(null, new Merge.MergeUpdate(List.of(new UpdateColumn("TEST_COLUMN")))),
                        new Merge.WhenMatched<>(null, new Merge.MergeDelete())),
                null);

        // When
        final String result = mergeSqlGenerator.generateSql(merge, mock(ConnectionProvider.class));

        // Then
        assertEquals("MERGE INTO TEST_SCHEMA.TEST_TABLE USING SOURCE_TABLE ON (TEST_TABLE.TEST_ID = ?) WHEN MATCHED THEN UPDATE SET TEST_COLUMN = ? WHEN MATCHED THEN DELETE", result);
    }

    @Test
    void generateSql_notMatchedInsertGeneratedValueAndMultipleRows() {
        // Given
        final ConditionGroup on = new ConditionGroup(new LogicCondition(
                createSelectColumn(createTestColumn("TEST_ID")),
                Operator.EQ,
                createLiteralExpression(1)));
        final Merge.MergeInsert insert = new Merge.MergeInsert(
                List.of(new UpdateColumn("TEST_ID", () -> "DEFAULT", null), new UpdateColumn("TEST_COLUMN")),
                2);
        final Merge merge = new Merge(createTestTable(), new Table("SOURCE_TABLE"), on, null, List.of(new Merge.WhenMatched<>(null, insert)));

        // When
        final String result = mergeSqlGenerator.generateSql(merge, mock(ConnectionProvider.class));

        // Then
        assertEquals("MERGE INTO TEST_SCHEMA.TEST_TABLE USING SOURCE_TABLE ON (TEST_TABLE.TEST_ID = ?) WHEN NOT MATCHED THEN INSERT (TEST_ID, TEST_COLUMN) VALUES (DEFAULT, ?), (DEFAULT, ?)", result);
    }
}
package org.litebridge.db.spi.impl.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnType;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.alias.AliasedQuery;
import org.litebridge.db.spi.alias.AliasedTable;
import org.litebridge.db.spi.impl.expression.BindValueExpressionImpl;
import org.litebridge.db.spi.impl.expression.LiteralExpressionImpl;
import org.litebridge.db.spi.math.MathOperator;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.LogicCondition;
import org.litebridge.db.spi.query.LogicConditionGroup;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.query.Values;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Merge;
import org.litebridge.db.spi.update.UpdateColumn;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.litebridge.db.spi.impl.sql.TestUtil.createLiteralExpression;
import static org.litebridge.db.spi.impl.sql.TestUtil.createSelectColumn;
import static org.litebridge.db.spi.impl.sql.TestUtil.createTestColumn;
import static org.litebridge.db.spi.impl.sql.TestUtil.createTestTable;
import static org.mockito.Mockito.mock;

class MergeSqlGeneratorTest {

    private final LabelGenerator labelGenerator = new LabelGenerator();
    private MergeSqlGenerator mergeSqlGenerator;

    @BeforeEach
    void beforeEach() {
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

    @Test
    void generateSql_whenMatchedWithAndCondition_andDifferentTargets() {
        // Given
        final ConditionGroup on = new ConditionGroup(new LogicCondition(
                createSelectColumn(createTestColumn("TEST_ID")),
                Operator.EQ,
                createLiteralExpression(1)));

        final ConditionGroup andCondition = new ConditionGroup(new LogicCondition(
                createSelectColumn(createTestColumn("STATUS")),
                Operator.EQ,
                createLiteralExpression("ACTIVE")));

        final Values values = new Values(List.of(new LiteralExpressionImpl(1, "VAL1", labelGenerator)), "v");

        final Merge merge = new Merge(
                createTestTable(),
                values,
                on,
                List.of(new Merge.WhenMatched<>(andCondition, new Merge.MergeUpdate(List.of(
                        new UpdateColumn("COUNT", null, MathOperator.ADD),
                        new UpdateColumn("GEN_COL", () -> "NOW()", null)
                )))),
                null);

        // When
        final String result = mergeSqlGenerator.generateSql(merge, mock(ConnectionProvider.class));

        // Then
        assertEquals("MERGE INTO TEST_SCHEMA.TEST_TABLE USING (VALUES (?)) AS \"v\" (\"VAL1\") ON (TEST_TABLE.TEST_ID = ?) WHEN MATCHED AND TEST_TABLE.STATUS = ? THEN UPDATE SET COUNT = COUNT + ?, GEN_COL = NOW()", result);
    }

    @Test
    void generateSql_withAliasedTableAndAliasedQueryAndSubselectTargets() {
        // Given
        final ConditionGroup on = new ConditionGroup(List.of());
        final ConnectionProvider cp = mock(ConnectionProvider.class);

        final AliasedTable aliasedTable = new AliasedTable("src", new Table("SOURCE"));
        final Merge mergeTable = new Merge(createTestTable(), aliasedTable, on, List.of(new Merge.WhenMatched<>(null, new Merge.MergeDelete())), null);
        assertEquals("MERGE INTO TEST_SCHEMA.TEST_TABLE USING SOURCE AS \"src\" ON () WHEN MATCHED THEN DELETE", mergeSqlGenerator.generateSql(mergeTable, cp));

        final Select subselect = new Select(
                new Table("SRC_SUB"), List.of(), List.of(), null, List.of(), null, List.of(), null);
        final AliasedQuery aliasedQuery = new AliasedQuery("aq", subselect);
        final Merge mergeQuery = new Merge(createTestTable(), aliasedQuery, on, List.of(new Merge.WhenMatched<>(null, new Merge.MergeDelete())), null);
        assertEquals("MERGE INTO TEST_SCHEMA.TEST_TABLE USING (SELECT * FROM SRC_SUB) AS \"aq\" ON () WHEN MATCHED THEN DELETE", mergeSqlGenerator.generateSql(mergeQuery, cp));

        final Merge mergeDirectSub = new Merge(createTestTable(), subselect, on, List.of(new Merge.WhenMatched<>(null, new Merge.MergeDelete())), null);
        assertEquals("MERGE INTO TEST_SCHEMA.TEST_TABLE USING (SELECT * FROM SRC_SUB) ON () WHEN MATCHED THEN DELETE", mergeSqlGenerator.generateSql(mergeDirectSub, cp));

        final Merge mergeVoid = new Merge(createTestTable(), new org.litebridge.db.spi.query.SelectTarget.Void(), on, List.of(new Merge.WhenMatched<>(null, new Merge.MergeDelete())), null);
        assertEquals("MERGE INTO TEST_SCHEMA.TEST_TABLE USING  ON () WHEN MATCHED THEN DELETE", mergeSqlGenerator.generateSql(mergeVoid, cp));
    }

    @Test
    void generateSql_unsupportedOperation_throwsIllegalArgumentException() {
        // Given
        final Merge.WhenMatchedOperation unknownOp = mock(Merge.WhenMatchedOperation.class);
        final Merge merge = new Merge(createTestTable(), new Table("SRC"), new ConditionGroup(List.of()),
                List.of(new Merge.WhenMatched<>(null, unknownOp)), null);

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> mergeSqlGenerator.generateSql(merge, mock(ConnectionProvider.class)));
    }

    @Test
    void collectConditionGroupIndices_collectsIndices() {
        // Given
        final ColumnType colType = new ColumnType(Types.INTEGER, null);
        final BindValueExpressionImpl bve1 = new BindValueExpressionImpl(1, 2, colType, null, new LabelGenerator());
        final BindValueExpressionImpl bve2 = new BindValueExpressionImpl(3, 1, colType, null, new LabelGenerator());

        final LogicCondition cond1 = new LogicCondition(createSelectColumn(), Operator.EQ, bve1);
        final LogicCondition cond2 = new LogicCondition(createSelectColumn(), Operator.EQ, bve2);

        final ConditionGroup subGroup = new ConditionGroup(List.of(cond2));
        final LogicConditionGroup logicSubGroup = new LogicConditionGroup(LogicOperator.AND, subGroup);

        final ConditionGroup rootGroup = new ConditionGroup(List.of(cond1), List.of(logicSubGroup));

        final List<Integer> indices = new ArrayList<>();

        // When
        mergeSqlGenerator.collectConditionGroupIndices(rootGroup, indices);
        mergeSqlGenerator.collectConditionGroupIndices(null, indices); // covers null branch

        // Then
        assertEquals(List.of(1, 2, 3), indices);
    }
}

package org.litebridge.db.oracle.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.oracle.OracleColumnIdentifierGenerator;
import org.litebridge.db.oracle.engine.OracleExecutionEngine;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.expression.BindValueExpression;
import org.litebridge.db.spi.impl.function.SelectColumn;
import org.litebridge.db.spi.impl.function.SelectReferenceImpl;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.LogicCondition;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Merge;
import org.litebridge.db.spi.update.UpdateColumn;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class OracleMergeSqlGeneratorTest {

    private OracleMergeSqlGenerator generator;
    private OracleColumnIdentifierGenerator columnIdentifierGenerator;
    private final Table targetTable = new Table("ACCOUNT");
    private final Table sourceTable = new Table("PERSON");

    @BeforeEach
    void setUp() {
        OracleExecutionEngine.clearParameterPermutations();

        columnIdentifierGenerator = new OracleColumnIdentifierGenerator();
        final OracleMathOperationGenerator mathOperationGenerator = new OracleMathOperationGenerator(columnIdentifierGenerator);

        generator = new OracleMergeSqlGenerator(
                columnIdentifierGenerator,
                mathOperationGenerator,
                (t, c) -> mock(TableMetaData.class));
    }

    @Test
    void generateSql_updateWithWhere_recordsPermutation() {
        // ON (ACCOUNT.ACCOUNT_ID = PERSON.PERSON_ID)
        final Condition onCondition = new Condition(
                new SelectColumn(new Column(targetTable, "ACCOUNT_ID"), columnIdentifierGenerator),
                Operator.EQ,
                new SelectReferenceImpl(new Column(sourceTable, "PERSON_ID"), columnIdentifierGenerator));
        final ConditionGroup on = new ConditionGroup(new LogicCondition(LogicOperator.NOOP, onCondition));

        // WHEN MATCHED UPDATE SET BALANCE = ? WHERE ACCOUNT.ACCOUNT_ID < ?
        final Condition whereCondition = new Condition(
                new SelectColumn(new Column(targetTable, "ACCOUNT_ID"), columnIdentifierGenerator),
                Operator.LT,
                new BindValueExpression(0, 1));
        final ConditionGroup andGroup = new ConditionGroup(new LogicCondition(LogicOperator.NOOP, whereCondition));

        final Merge.WhenMatched<Merge.WhenMatchedOperation> updateMatched = new Merge.WhenMatched<>(
                andGroup,
                new Merge.MergeUpdate(List.of(new UpdateColumn("BALANCE", null, null, 1))));

        // WHEN NOT MATCHED INSERT (ACCOUNT_ID, NAME) VALUES (?, ?)
        final Merge.WhenMatched<Merge.MergeInsert> insertMatched = new Merge.WhenMatched<>(
                null,
                new Merge.MergeInsert(List.of(
                        new UpdateColumn("ACCOUNT_ID", null, null, 2),
                        new UpdateColumn("NAME", null, null, 3)), 1));

        final Merge merge = new Merge(targetTable, sourceTable, null, on, List.of(updateMatched), List.of(insertMatched));

        // When
        final String sql = generator.generateSql(merge, mock(ConnectionProvider.class));

        // Then
        assertEquals("MERGE INTO ACCOUNT USING PERSON ON (ACCOUNT.ACCOUNT_ID = PERSON.PERSON_ID) WHEN MATCHED THEN UPDATE SET BALANCE = ? WHERE ACCOUNT.ACCOUNT_ID < ? WHEN NOT MATCHED THEN INSERT (ACCOUNT_ID, NAME) VALUES (?, ?)", sql);

        final int[] expectedPermutation = new int[]{1, 0, 2, 3};
        assertArrayEquals(expectedPermutation, generator.computeParameterPermutation(merge));
        assertArrayEquals(expectedPermutation, OracleExecutionEngine.getParameterPermutation(sql));
    }

    @Test
    void generateSql_identityPermutation_doesNotRegisterPermutation() {
        // ON (ACCOUNT.ACCOUNT_ID = PERSON.PERSON_ID)
        final Condition onCondition = new Condition(
                new SelectColumn(new Column(targetTable, "ACCOUNT_ID"), columnIdentifierGenerator),
                Operator.EQ,
                new SelectReferenceImpl(new Column(sourceTable, "PERSON_ID"), columnIdentifierGenerator));
        final ConditionGroup on = new ConditionGroup(new LogicCondition(LogicOperator.NOOP, onCondition));

        // WHEN MATCHED UPDATE SET BALANCE = ? (no WHERE clause)
        final Merge.WhenMatched<Merge.WhenMatchedOperation> updateMatched = new Merge.WhenMatched<>(
                null,
                new Merge.MergeUpdate(List.of(new UpdateColumn("BALANCE", null, null, 0))));

        final Merge merge = new Merge(targetTable, sourceTable, null, on, List.of(updateMatched), null);

        // When
        final String sql = generator.generateSql(merge, mock(ConnectionProvider.class));

        // Then
        assertEquals("MERGE INTO ACCOUNT USING PERSON ON (ACCOUNT.ACCOUNT_ID = PERSON.PERSON_ID) WHEN MATCHED THEN UPDATE SET BALANCE = ?", sql);
        assertArrayEquals(new int[]{0}, generator.computeParameterPermutation(merge));
        assertNull(OracleExecutionEngine.getParameterPermutation(sql));
    }

    @Test
    void generateSql_updateAndDeleteWithWhere_recordsPermutation() {
        // ON (ACCOUNT.ACCOUNT_ID = PERSON.PERSON_ID)
        final Condition onCondition = new Condition(
                new SelectColumn(new Column(targetTable, "ACCOUNT_ID"), columnIdentifierGenerator),
                Operator.EQ,
                new SelectReferenceImpl(new Column(sourceTable, "PERSON_ID"), columnIdentifierGenerator));
        final ConditionGroup on = new ConditionGroup(new LogicCondition(LogicOperator.NOOP, onCondition));

        // WHEN MATCHED UPDATE SET BALANCE = ? WHERE ACCOUNT.ACCOUNT_ID < ?
        final Condition updateWhere = new Condition(
                new SelectColumn(new Column(targetTable, "ACCOUNT_ID"), columnIdentifierGenerator),
                Operator.LT,
                new BindValueExpression(0, 1));
        final Merge.WhenMatched<Merge.WhenMatchedOperation> updateMatched = new Merge.WhenMatched<>(
                new ConditionGroup(new LogicCondition(LogicOperator.NOOP, updateWhere)),
                new Merge.MergeUpdate(List.of(new UpdateColumn("BALANCE", null, null, 1))));

        // DELETE WHERE ACCOUNT.ACCOUNT_ID >= ?
        final Condition deleteWhere = new Condition(
                new SelectColumn(new Column(targetTable, "ACCOUNT_ID"), columnIdentifierGenerator),
                Operator.GTE,
                new BindValueExpression(2, 1));
        final Merge.WhenMatched<Merge.WhenMatchedOperation> deleteMatched = new Merge.WhenMatched<>(
                new ConditionGroup(new LogicCondition(LogicOperator.NOOP, deleteWhere)),
                new Merge.MergeDelete());

        // INSERT (ACCOUNT_ID) VALUES (?)
        final Merge.WhenMatched<Merge.MergeInsert> insertMatched = new Merge.WhenMatched<>(
                null,
                new Merge.MergeInsert(List.of(new UpdateColumn("ACCOUNT_ID", null, null, 3)), 1));

        final Merge merge = new Merge(targetTable, sourceTable, null, on, List.of(updateMatched, deleteMatched), List.of(insertMatched));

        // When
        final String sql = generator.generateSql(merge, mock(ConnectionProvider.class));

        // Then
        assertEquals("MERGE INTO ACCOUNT USING PERSON ON (ACCOUNT.ACCOUNT_ID = PERSON.PERSON_ID) WHEN MATCHED THEN UPDATE SET BALANCE = ? WHERE ACCOUNT.ACCOUNT_ID < ? DELETE WHERE ACCOUNT.ACCOUNT_ID >= ? WHEN NOT MATCHED THEN INSERT (ACCOUNT_ID) VALUES (?)", sql);

        final int[] expectedPermutation = new int[]{1, 0, 2, 3};
        assertArrayEquals(expectedPermutation, generator.computeParameterPermutation(merge));
        assertArrayEquals(expectedPermutation, OracleExecutionEngine.getParameterPermutation(sql));
    }
}

package org.litebridge.db.oracle.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.impl.engine.DefaultMetaDataEngine;
import org.litebridge.db.spi.impl.engine.MetaDataEngine;
import org.litebridge.db.spi.impl.expression.LiteralExpressionImpl;
import org.litebridge.db.spi.impl.expression.SelectColumn;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.LogicCondition;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Delete;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.Merge;
import org.litebridge.db.spi.update.Update;
import org.litebridge.db.spi.update.UpdateColumn;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

class OracleSqlGeneratorTest {

    private OracleSqlGenerator sqlGenerator;

    @BeforeEach
    void setUp() {
        final MetaDataEngine metaDataEngine = new DefaultMetaDataEngine();
        final LabelGenerator labelGenerator = new LabelGenerator();
        final OracleMathOperationGenerator mathOperationGenerator = new OracleMathOperationGenerator(labelGenerator);
        sqlGenerator = new OracleSqlGenerator(metaDataEngine, labelGenerator, mathOperationGenerator);
    }

    @Test
    void generatorGetters_returnOracleSpecializedInstances() {
        // When
        final SelectSqlGenerator selectGen = sqlGenerator.selectSqlGenerator();
        final OracleInsertSqlGenerator oracleInsertGen = sqlGenerator.oracleInsertSqlGenerator();

        // Then
        assertInstanceOf(OracleSelectSqlGenerator.class, selectGen);
        assertInstanceOf(OracleInsertSqlGenerator.class, oracleInsertGen);
    }

    @Test
    void generateSql_dispatchesAllOperationTypes() {
        // Given
        final Table table = new Table("ACCOUNT");
        final ConnectionProvider connectionProvider = mock(ConnectionProvider.class);
        final LabelGenerator labelGenerator = new LabelGenerator();

        final ConditionGroup where = new ConditionGroup(new LogicCondition(
                new SelectColumn(new Column(table, "ID"), null, null, labelGenerator),
                Operator.EQ,
                new LiteralExpressionImpl(1, labelGenerator)));

        final Select select = new Select(table, List.of(), List.of(), null, List.of(), null, List.of(), null);
        final Insert insert = new Insert(table, List.of(new UpdateColumn("ID")), 1, false);
        final Update update = new Update(table, List.of(new UpdateColumn("NAME")), where);
        final Delete delete = new Delete(table, where);

        final Merge.WhenMatched<Merge.WhenMatchedOperation> whenMatched = new Merge.WhenMatched<>(
                null,
                new Merge.MergeUpdate(List.of(new UpdateColumn("NAME", null, null, 0))));
        final Merge merge = new Merge(table, new Table("SOURCE"), where, List.of(whenMatched), null);

        // When / Then
        assertEquals("SELECT * FROM ACCOUNT", sqlGenerator.generateSql(select, connectionProvider));
        assertEquals("INSERT INTO ACCOUNT (ID) VALUES (?)", sqlGenerator.generateSql(insert, connectionProvider));
        assertEquals("UPDATE ACCOUNT SET NAME = ? WHERE ACCOUNT.ID = ?", sqlGenerator.generateSql(update, connectionProvider));
        assertEquals("DELETE FROM ACCOUNT WHERE ACCOUNT.ID = ?", sqlGenerator.generateSql(delete, connectionProvider));
        assertEquals("MERGE INTO ACCOUNT USING SOURCE ON (ACCOUNT.ID = ?) WHEN MATCHED THEN UPDATE SET NAME = ?", sqlGenerator.generateSql(merge, connectionProvider));
    }
}

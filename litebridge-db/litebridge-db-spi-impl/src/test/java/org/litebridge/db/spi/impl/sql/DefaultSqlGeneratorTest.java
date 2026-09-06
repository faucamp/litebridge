package org.litebridge.db.spi.impl.sql;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.impl.function.SelectColumn;
import org.litebridge.db.spi.impl.engine.MetaDataEngine;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.litebridge.db.spi.impl.sql.TestUtil.createTestColumn;
import static org.litebridge.db.spi.impl.sql.TestUtil.createTestTable;
import static org.mockito.Mockito.mock;

class DefaultSqlGeneratorTest {

    @Test
    void metaDataEngine_returnsConfiguredEngine() {
        // Given
        final MetaDataEngine expected = mock(MetaDataEngine.class);
        final DefaultSqlGenerator sqlGenerator = new DefaultSqlGenerator(expected);

        // When
        final MetaDataEngine result = sqlGenerator.metaDataEngine();

        // Then
        assertSame(expected, result);
    }

    @Test
    void generateSql_dispatchesEveryOperationVariant() {
        // Given
        final DefaultSqlGenerator sqlGenerator = new DefaultSqlGenerator();
        final ConditionGroup where = new ConditionGroup(new LogicCondition(
                new SelectColumn(createTestColumn(), sqlGenerator.selectSqlGenerator().columnIdentifierGenerator),
                Operator.EQ,
                "value"));
        final Insert insert = new Insert(createTestTable(), List.of(new UpdateColumn("TEST_COLUMN")), 1, false);
        final Update update = new Update(createTestTable(), List.of(new UpdateColumn("TEST_COLUMN")), where);
        final Delete delete = new Delete(createTestTable(), where);
        final Merge merge = new Merge(createTestTable(), null, null, where, null, null);
        final Select select = new Select(createTestTable(), List.of(), List.of(), null, List.of(), null, List.of(), null);
        final ConnectionProvider connectionProvider = mock(ConnectionProvider.class);

        // When / Then
        assertEquals("SELECT * FROM TEST_SCHEMA.TEST_TABLE", sqlGenerator.generateSql(select, connectionProvider));
        assertEquals("INSERT INTO TEST_SCHEMA.TEST_TABLE (TEST_COLUMN) VALUES (?)", sqlGenerator.generateSql(insert, connectionProvider));
        assertEquals("UPDATE TEST_SCHEMA.TEST_TABLE SET TEST_COLUMN = ? WHERE TEST_TABLE.TEST_COLUMN = ?", sqlGenerator.generateSql(update, connectionProvider));
        assertEquals("DELETE FROM TEST_SCHEMA.TEST_TABLE WHERE TEST_TABLE.TEST_COLUMN = ?", sqlGenerator.generateSql(delete, connectionProvider));
        assertEquals("MERGE INTO TEST_SCHEMA.TEST_TABLE USING () ON TEST_TABLE.TEST_COLUMN = ?", sqlGenerator.generateSql(merge, connectionProvider));
    }
}
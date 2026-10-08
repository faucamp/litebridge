package org.litebridge.db.sqlite.sql;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.impl.engine.MetaDataEngine;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.impl.sql.MathOperationGenerator;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

class SQLiteSqlGeneratorTest {

    @Test
    void createSelectSqlGenerator() {
        // Given
        final MetaDataEngine metaDataEngine = mock(MetaDataEngine.class);
        final LabelGenerator labelGenerator = new LabelGenerator();
        final MathOperationGenerator mathOperationGenerator = new MathOperationGenerator(labelGenerator);
        final SQLiteSqlGenerator generator = new SQLiteSqlGenerator(metaDataEngine, labelGenerator, mathOperationGenerator);

        // When
        final SelectSqlGenerator result = generator.createSelectSqlGenerator();

        // Then
        assertInstanceOf(SQLiteSelectSqlGenerator.class, result);
    }
}
package org.litebridge.db.sqlite.sql;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.impl.sql.MathOperationGenerator;
import org.litebridge.db.spi.query.Limit;
import org.litebridge.db.spi.tx.ConnectionProvider;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SQLiteSelectSqlGeneratorTest {

    @Test
    void appendLimitClause() {
        // Given
        final LabelGenerator labelGenerator = new LabelGenerator();
        final MathOperationGenerator mathOperationGenerator = new MathOperationGenerator(labelGenerator);
        final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData = (table, connectionProvider) -> null;
        final SQLiteSelectSqlGenerator generator = new SQLiteSelectSqlGenerator(labelGenerator, mathOperationGenerator, ensureTableMetaData);
        final StringBuilder sql = new StringBuilder();
        final Limit limit = new Limit(10, 5);

        // When
        generator.appendLimitClause(limit, sql);

        // Then
        final String result = sql.toString();
        assertEquals(" LIMIT 10 OFFSET 5", result);
    }

    @Test
    void appendLimitClause_nullLimit() {
        // Given
        final LabelGenerator labelGenerator = new LabelGenerator();
        final MathOperationGenerator mathOperationGenerator = new MathOperationGenerator(labelGenerator);
        final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData = (table, connectionProvider) -> null;
        final SQLiteSelectSqlGenerator generator = new SQLiteSelectSqlGenerator(labelGenerator, mathOperationGenerator, ensureTableMetaData);
        final StringBuilder sql = new StringBuilder();
        final Limit limit = new Limit(null, 5);

        // When
        generator.appendLimitClause(limit, sql);

        // Then
        final String result = sql.toString();
        assertEquals(" LIMIT -1 OFFSET 5", result);
    }
}
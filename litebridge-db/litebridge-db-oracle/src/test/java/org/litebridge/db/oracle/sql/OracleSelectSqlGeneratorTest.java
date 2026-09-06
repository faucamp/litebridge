package org.litebridge.db.oracle.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.oracle.OracleColumnIdentifierGenerator;
import org.litebridge.db.oracle.OracleDatabaseProvider;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.query.Limit;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.mockito.Mock;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OracleSelectSqlGeneratorTest {

    @Mock
    private TypeConverter typeConverter;
    @Mock
    private BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData;
    private OracleSelectSqlGenerator oracleSelectSqlGenerator;

    @BeforeEach
    void beforeEach() {
        oracleSelectSqlGenerator = new OracleSelectSqlGenerator(new OracleColumnIdentifierGenerator(), ensureTableMetaData);
    }

    @Test
    void appendLimitClause_withOffsetAndLimit() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final Limit limit = new Limit(10, 5);
        final StringBuilder sql = new StringBuilder("SELECT * FROM TEST_TABLE");

        // When
        oracleSelectSqlGenerator.appendLimitClause(limit, sql);

        // Then
        assertEquals("SELECT * FROM TEST_TABLE OFFSET 5 ROWS FETCH FIRST 10 ROWS ONLY", sql.toString());
    }

    @Test
    void appendLimitClause_withOffsetOnly() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final Limit limit = new Limit(null, 5);
        final StringBuilder sql = new StringBuilder("SELECT * FROM TEST_TABLE");

        // When
        oracleSelectSqlGenerator.appendLimitClause(limit, sql);

        // Then
        assertEquals("SELECT * FROM TEST_TABLE OFFSET 5 ROWS", sql.toString());
    }

    @Test
    void appendLimitClause_withLimitOnly() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final Limit limit = new Limit(10, null);
        final StringBuilder sql = new StringBuilder("SELECT * FROM TEST_TABLE");

        // When
        oracleSelectSqlGenerator.appendLimitClause(limit, sql);

        // Then
        assertEquals("SELECT * FROM TEST_TABLE FETCH FIRST 10 ROWS ONLY", sql.toString());
    }
}
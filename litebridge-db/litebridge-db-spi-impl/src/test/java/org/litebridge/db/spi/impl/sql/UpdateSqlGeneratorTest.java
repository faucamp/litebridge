package org.litebridge.db.spi.impl.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.math.MathOperation;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Types;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.litebridge.db.spi.impl.sql.TestUtil.createTestColumn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateSqlGeneratorTest {

    @Mock
    private TypeConverter typeConverter;
    @Mock
    private BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData;
    private UpdateSqlGenerator updateSqlGenerator;

    @BeforeEach
    void beforeEach() {
        updateSqlGenerator = new UpdateSqlGenerator(typeConverter, new ColumnIdentifierGenerator(), ensureTableMetaData);
    }

    @Test
    void createMathOperation() {
        // Given
        final Column column = createTestColumn();
        final MathOperation mathOperation = new MathOperation(MathOperation.Operator.ADD, 10);

        // When
        final String result = updateSqlGenerator.createMathOperation(column.name(), mathOperation);

        // Then
        assertEquals("TEST_COLUMN + ?", result);
    }
}
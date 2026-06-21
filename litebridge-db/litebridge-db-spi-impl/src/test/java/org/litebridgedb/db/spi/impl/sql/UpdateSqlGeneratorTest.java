package org.litebridgedb.db.spi.impl.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.convert.TypeConverter;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.math.MathOperation;
import org.litebridgedb.db.spi.tx.ConnectionProvider;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Types;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.litebridgedb.db.spi.impl.sql.TestUtil.createTestColumn;
import static org.mockito.Mockito.mock;
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
        final int dataType = Types.NUMERIC;

        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(columnMetaData.name()).thenReturn(column.name());
        when(columnMetaData.getDataType()).thenReturn(dataType);

        when(typeConverter.convert(10, dataType)).thenReturn(10);

        // When
        final String result = updateSqlGenerator.createMathOperation(columnMetaData, mathOperation);

        // Then
        assertEquals("TEST_COLUMN + 10", result);
    }
}
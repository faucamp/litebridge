package org.litebridge.db.spi.query;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeConversionMetaDataTest {

    @Test
    void constructor_2args() {
        // Given
        final ColumnMetaData cmd = new ColumnMetaData(new Table("T"), "C", false, 1);
        final Map<String, ColumnMetaData> columnMap = Map.of("C", cmd);
        final Class<?>[] overrides = new Class<?>[]{String.class};

        // When
        final TypeConversionMetaData metaData = new TypeConversionMetaData(columnMap, overrides);

        // Then
        assertEquals(columnMap, metaData.columnLabelsToColumnMetaData());
        assertTrue(metaData.columnLabelsToTableAliases().isEmpty());
        assertArrayEquals(overrides, metaData.typeOverrides());
    }

    @Test
    void constructor_3args_andEquals() {
        // Given
        final ColumnMetaData cmd = new ColumnMetaData(new Table("T"), "C", false, 1);
        final Map<String, ColumnMetaData> columnMap = Map.of("C", cmd);
        final Map<String, String> tableAliases = Map.of("C", "A");
        final Class<?>[] overrides = new Class<?>[]{String.class};

        // When
        final TypeConversionMetaData metaData1 = new TypeConversionMetaData(columnMap, tableAliases, overrides);
        final TypeConversionMetaData metaData2 = new TypeConversionMetaData(columnMap, tableAliases, overrides);
        final TypeConversionMetaData metaDataDiff = new TypeConversionMetaData(columnMap, Map.of("C", "B"), overrides);

        // Then
        assertEquals(columnMap, metaData1.columnLabelsToColumnMetaData());
        assertEquals(tableAliases, metaData1.columnLabelsToTableAliases());
        assertArrayEquals(overrides, metaData1.typeOverrides());

        assertEquals(metaData1, metaData2);
        assertNotEquals(metaData1, metaDataDiff);
    }
}

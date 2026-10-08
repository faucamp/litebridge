package org.litebridge.db.spi.query;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateMetaDataTest {

    @Test
    void recordProperties() {
        // Given
        final ColumnMetaData genCol = new ColumnMetaData(new Table("T"), "ID", false, 1);
        final List<ColumnMetaData> genKeys = List.of(genCol);
        final String[] genKeyNames = new String[]{"ID"};

        final UpdateMetaData metaData1 = new UpdateMetaData(true, genKeys, genKeyNames, 5, 2);
        final UpdateMetaData metaData2 = new UpdateMetaData(true, genKeys, genKeyNames, 5, 2);
        final UpdateMetaData metaDataDiff = new UpdateMetaData(false, null, null, 1, 0);

        // When & Then
        assertTrue(metaData1.returnGeneratedKeys());
        assertEquals(genKeys, metaData1.generatedKeys());
        assertArrayEquals(genKeyNames, metaData1.generatedKeyNames());
        assertEquals(5, metaData1.rows());
        assertEquals(2, metaData1.bindValueColumns());

        assertEquals(metaData1, metaData2);
        assertNotEquals(metaData1, metaDataDiff);
    }
}

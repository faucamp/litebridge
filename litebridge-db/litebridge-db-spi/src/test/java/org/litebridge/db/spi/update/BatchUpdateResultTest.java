package org.litebridge.db.spi.update;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class BatchUpdateResultTest {

    @Test
    void recordProperties() {
        // Given
        final int[] rows = new int[]{1, 2, 3};
        final BatchUpdateResult res1 = new BatchUpdateResult(rows);
        final BatchUpdateResult res2 = new BatchUpdateResult(rows);
        final BatchUpdateResult resDiff = new BatchUpdateResult(new int[]{4});

        // When & Then
        assertArrayEquals(rows, res1.rowsAffected());
        assertEquals(res1, res2);
        assertEquals(res1.hashCode(), res2.hashCode());
        assertNotEquals(res1, resDiff);
    }
}

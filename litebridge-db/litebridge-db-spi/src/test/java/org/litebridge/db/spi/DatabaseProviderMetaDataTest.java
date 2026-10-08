package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseProviderMetaDataTest {

    @Test
    void recordProperties_andEnums() {
        // Given
        final DatabaseProviderMetaData metaData = new DatabaseProviderMetaData(
                true,
                DatabaseProviderMetaData.MergeCapability.USING_VALUES,
                DatabaseProviderMetaData.InsertCapability.NATIVE_MULTIROW
        );

        // When & Then
        assertTrue(metaData.supportsSequenceColumnValueGenerator());
        assertEquals(DatabaseProviderMetaData.MergeCapability.USING_VALUES, metaData.mergeCapability());
        assertEquals(DatabaseProviderMetaData.InsertCapability.NATIVE_MULTIROW, metaData.insertCapability());

        final DatabaseProviderMetaData sameMetaData = new DatabaseProviderMetaData(
                true,
                DatabaseProviderMetaData.MergeCapability.USING_VALUES,
                DatabaseProviderMetaData.InsertCapability.NATIVE_MULTIROW
        );
        assertEquals(metaData, sameMetaData);
        assertEquals(metaData.hashCode(), sameMetaData.hashCode());

        final DatabaseProviderMetaData diffMetaData = new DatabaseProviderMetaData(
                false,
                DatabaseProviderMetaData.MergeCapability.NOT_SUPPORTED,
                DatabaseProviderMetaData.InsertCapability.BATCHED_INSERTS
        );
        assertNotEquals(metaData, diffMetaData);

        // Test enum values
        assertEquals(2, DatabaseProviderMetaData.InsertCapability.values().length);
        assertEquals(DatabaseProviderMetaData.InsertCapability.NATIVE_MULTIROW, DatabaseProviderMetaData.InsertCapability.valueOf("NATIVE_MULTIROW"));
        assertEquals(DatabaseProviderMetaData.InsertCapability.BATCHED_INSERTS, DatabaseProviderMetaData.InsertCapability.valueOf("BATCHED_INSERTS"));

        assertEquals(3, DatabaseProviderMetaData.MergeCapability.values().length);
        assertEquals(DatabaseProviderMetaData.MergeCapability.NOT_SUPPORTED, DatabaseProviderMetaData.MergeCapability.valueOf("NOT_SUPPORTED"));
        assertEquals(DatabaseProviderMetaData.MergeCapability.USING_VALUES, DatabaseProviderMetaData.MergeCapability.valueOf("USING_VALUES"));
        assertEquals(DatabaseProviderMetaData.MergeCapability.USING_VALUES_SUBQUERY, DatabaseProviderMetaData.MergeCapability.valueOf("USING_VALUES_SUBQUERY"));
    }
}

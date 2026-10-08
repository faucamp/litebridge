package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class DatabaseMetaDataTest {

    @Test
    void recordProperties() {
        // Given
        final DatabaseMetaData.Component database = new DatabaseMetaData.Component("H2", "2.2.224", 2, 2);
        final DatabaseMetaData.Component driver = new DatabaseMetaData.Component("H2 JDBC Driver", "2.2.224", 2, 2);
        final DatabaseMetaData metaData = new DatabaseMetaData(database, driver);

        // When & Then
        assertEquals(database, metaData.database());
        assertEquals(driver, metaData.driver());

        assertEquals("H2", database.name());
        assertEquals("2.2.224", database.version());
        assertEquals(2, database.majorVersion());
        assertEquals(2, database.minorVersion());

        final DatabaseMetaData sameMetaData = new DatabaseMetaData(database, driver);
        assertEquals(metaData, sameMetaData);
        assertEquals(metaData.hashCode(), sameMetaData.hashCode());

        final DatabaseMetaData differentMetaData = new DatabaseMetaData(
                new DatabaseMetaData.Component("PostgreSQL", "16.0", 16, 0),
                driver
        );
        assertNotEquals(metaData, differentMetaData);
    }
}

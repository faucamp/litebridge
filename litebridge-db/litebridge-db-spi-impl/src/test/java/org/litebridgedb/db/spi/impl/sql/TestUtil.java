package org.litebridgedb.db.spi.impl.sql;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;

final class TestUtil {

    private TestUtil() {
    }

    public static Column createTestColumn() {
        return createTestColumn("TEST_COLUMN");
    }

    public static Column createTestColumn(final String name) {
        return new Column(createTestTable(), name);
    }

    public static Column createTestColumn(final String name, final Table table) {
        return new Column(table, name);
    }

    public static Table createTestTable() {
        return new Table("TEST_SCHEMA.TEST_TABLE");
    }
}

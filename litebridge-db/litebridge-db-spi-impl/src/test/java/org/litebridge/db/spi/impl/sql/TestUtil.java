package org.litebridge.db.spi.impl.sql;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.function.SelectColumn;

final class TestUtil {

    private TestUtil() {
    }

    public static Column createTestColumn() {
        return createTestColumn("TEST_COLUMN");
    }

    public static SelectColumn createSelectColumn(final ColumnIdentifierGenerator columnIdentifierGenerator) {
        return new SelectColumn(createTestColumn("TEST_COLUMN"), columnIdentifierGenerator);
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

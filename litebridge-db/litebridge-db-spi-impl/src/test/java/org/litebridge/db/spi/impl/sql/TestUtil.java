package org.litebridge.db.spi.impl.sql;

import org.junit.jupiter.params.shadow.de.siegmar.fastcsv.util.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.impl.expression.LiteralExpressionImpl;
import org.litebridge.db.spi.impl.expression.SelectColumn;

final class TestUtil {

    private static final LabelGenerator labelGenerator = new LabelGenerator();

    private TestUtil() {
    }

    public static Column createTestColumn() {
        return createTestColumn("TEST_COLUMN");
    }

    public static SelectColumn createSelectColumn() {
        return new SelectColumn(createTestColumn("TEST_COLUMN"), null, null, labelGenerator);
    }

    public static SelectColumn createSelectColumnWithTableAlias(final String tableAlias) {
        return createSelectColumn(createTestColumn(), null, tableAlias);
    }

    public static SelectColumn createSelectColumn(final Column column) {
        return createSelectColumn(column, null);
    }

    public static SelectColumn createSelectColumn(final Column column, final String alias) {
        return createSelectColumn(column, alias, null);
    }

    public static SelectColumn createSelectColumn(final Column column, final @Nullable String alias, final String tableAlias) {
        return new SelectColumn(column, alias, tableAlias, labelGenerator);
    }

    public static LiteralExpressionImpl createLiteralExpression(final @Nullable Object value) {
        return new LiteralExpressionImpl(value, labelGenerator);
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

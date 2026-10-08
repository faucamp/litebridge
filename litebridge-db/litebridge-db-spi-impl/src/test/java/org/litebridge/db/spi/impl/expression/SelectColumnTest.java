package org.litebridge.db.spi.impl.expression;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.query.Select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SelectColumnTest {

    private final LabelGenerator labelGenerator = new LabelGenerator();

    @Test
    void column_returnsColumn() {
        // Given
        final Column column = new Column(new Table("users"), "id");
        final SelectColumn selectColumn = new SelectColumn(column, "user_id", "u", labelGenerator);

        // When / Then
        assertSame(column, selectColumn.column());
        assertEquals("user_id", selectColumn.alias());
        assertEquals("u", selectColumn.tableAlias());
    }

    @Test
    void toSql_withTableAliasAndColumnAlias() {
        // Given
        final Column column = new Column(new Table("users"), "name");
        final SelectColumn selectColumn = new SelectColumn(column, "user_name", "u", labelGenerator);
        final Select select = mock(Select.class);

        // When
        final String sql = selectColumn.toSql(select, ClauseType.SELECT, null);

        // Then
        assertEquals("\"u\".name AS \"user_name\"", sql);
    }

    @Test
    void toSql_withTableAndNoTableAlias_andNoColumnAlias() {
        // Given
        final Column column = new Column(new Table("users"), "email");
        final SelectColumn selectColumn = new SelectColumn(column, null, null, labelGenerator);
        final Select select = mock(Select.class);

        // When
        final String sql = selectColumn.toSql(select, ClauseType.SELECT, null);

        // Then
        assertEquals("users.email", sql);
    }

    @Test
    void toSql_withVirtualTable() {
        // Given
        final Column column = new Column("count");
        final SelectColumn selectColumn = new SelectColumn(column, "cnt", null, labelGenerator);
        final Select select = mock(Select.class);

        // When
        final String sql = selectColumn.toSql(select, ClauseType.SELECT, null);

        // Then
        assertEquals("count AS \"cnt\"", sql);
    }

    @Test
    void toSql_nonSelectClause_doesNotAddAlias() {
        // Given
        final Column column = new Column(new Table("users"), "age");
        final SelectColumn selectColumn = new SelectColumn(column, "user_age", "u", labelGenerator);
        final Select select = mock(Select.class);

        // When
        final String sql = selectColumn.toSql(select, ClauseType.WHERE, null);

        // Then
        assertEquals("\"u\".age", sql);
    }

    @Test
    void equalsAndHashCode() {
        // Given
        final Column col1 = new Column(new Table("users"), "id");
        final Column col2 = new Column(new Table("users"), "id");
        final Column col3 = new Column(new Table("users"), "name");

        final SelectColumn sc1 = new SelectColumn(col1, "alias1", "t1", labelGenerator);
        final SelectColumn sc2 = new SelectColumn(col2, "alias1", "t1", labelGenerator);
        final SelectColumn scDifferentCol = new SelectColumn(col3, "alias1", "t1", labelGenerator);
        final SelectColumn scDifferentAlias = new SelectColumn(col1, "alias2", "t1", labelGenerator);
        final SelectColumn scDifferentTableAlias = new SelectColumn(col1, "alias1", "t2", labelGenerator);

        // Then
        assertEquals(sc1, sc1);
        assertEquals(sc1, sc2);
        assertNotEquals(sc1, scDifferentCol);
        assertNotEquals(sc1, scDifferentAlias);
        assertNotEquals(sc1, scDifferentTableAlias);
        assertNotEquals(sc1, null);
        assertNotEquals(sc1, "other");

        assertEquals(sc1.hashCode(), sc2.hashCode());
    }

    @Test
    void testToString() {
        // Given
        final Column column = new Column(new Table("users"), "id");
        final SelectColumn selectColumn = new SelectColumn(column, "my_id", "u", labelGenerator);

        // When
        final String str = selectColumn.toString();

        // Then
        assertTrue(str.contains("SelectColumn"));
        assertTrue(str.contains("alias='my_id'"));
        assertTrue(str.contains("tableAlias='u'"));
    }
}

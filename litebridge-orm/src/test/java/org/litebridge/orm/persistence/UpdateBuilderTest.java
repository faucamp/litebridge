package org.litebridge.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.update.ColumnValue;
import org.litebridge.db.spi.update.Update;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.ClassFieldAccessorCache;

import java.lang.invoke.MethodHandles;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class UpdateBuilderTest {

    @Test
    void getColumnValues_empty() {
        // Given
        final UpdateBuilder updateBuilder = new UpdateBuilder(ormTable());

        // When
        final List<ColumnValue> result = updateBuilder.getColumnValues();

        // Then
        assertEquals(List.of(), result);
    }

    @Test
    void setColumnValues() {
        // Given
        final UpdateBuilder updateBuilder = new UpdateBuilder(ormTable());
        final ColumnValue columnValue = new ColumnValue(column("test_table", "name", Types.VARCHAR).toColumn(), "test");

        // When
        final UpdateBuilder result = updateBuilder.setColumnValues(List.of(columnValue));

        // Then
        assertSame(updateBuilder, result);
        assertEquals(List.of(columnValue), updateBuilder.getColumnValues());
    }

    @Test
    void where() {
        // Given
        final UpdateBuilder updateBuilder = new UpdateBuilder(ormTable());
        final ColumnValue columnValue = new ColumnValue(column("test_table", "name", Types.VARCHAR).toColumn(), "test");
        final ConditionGroup conditionGroup = mock(ConditionGroup.class);
        updateBuilder.setColumnValues(List.of(columnValue));

        // When
        final UpdateBuilder result = updateBuilder.where(conditionGroup);

        // Then
        assertSame(updateBuilder, result);
        assertEquals(List.of(columnValue), updateBuilder.getColumnValues());
    }

    @Test
    void build() {
        // Given
        final OrmTable ormTable = ormTable();
        final UpdateBuilder updateBuilder = new UpdateBuilder(ormTable);

        final ColumnValue columnValue = new ColumnValue(column("test_table", "name", Types.VARCHAR).toColumn(), "test");
        final ConditionGroup conditionGroup = mock(ConditionGroup.class);

        updateBuilder.setColumnValues(List.of(columnValue));
        updateBuilder.where(conditionGroup);

        // When
        final Update result = updateBuilder.build();

        // Then
        assertEquals(ormTable.getMetaData().toTable(), result.table());
        assertEquals(List.of(columnValue), result.columnValues());
        assertEquals(conditionGroup, result.where());
    }

    @Test
    void build_noColumnValues() {
        // Given
        final UpdateBuilder updateBuilder = new UpdateBuilder(ormTable());

        // When/Then
        assertThrows(IllegalArgumentException.class, updateBuilder::build);
    }

    @Test
    void build_emptyColumnValues() {
        // Given
        final UpdateBuilder updateBuilder = new UpdateBuilder(ormTable());
        updateBuilder.setColumnValues(List.of());

        // When/Then
        assertThrows(IllegalArgumentException.class, updateBuilder::build);
    }

    @Test
    void build_noConditions() {
        // Given
        final UpdateBuilder updateBuilder = new UpdateBuilder(ormTable());
        updateBuilder.setColumnValues(List.of(new ColumnValue(column("test_table", "name", Types.VARCHAR).toColumn(), "test")));

        // When/Then
        assertThrows(NullPointerException.class, updateBuilder::build);
    }

    private static OrmTable ormTable() {
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final Table table = new Table("", "public", "test_table");
        final ColumnMetaData idColumn = new ColumnMetaData(table, "id", false, Types.BIGINT);
        final TableMetaData tableMetaData = new TableMetaData(table, List.of("id"), List.of(idColumn));
        return new OrmTable(TestDto.class, tableMetaData, Map.of(), changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
    }

    private static ColumnMetaData column(final String tableName, final String columnName, final int type) {
        return new ColumnMetaData(new Table("", "public", tableName), columnName, false, type);
    }

    private static class TestDto {
        private Long id;
    }
}
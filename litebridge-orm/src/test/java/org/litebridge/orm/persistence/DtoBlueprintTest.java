package org.litebridge.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.api.dto.DtoJoinSpec;
import org.litebridge.orm.api.dto.DtoSelectSpec;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.ClassFieldAccessorCache;

import java.lang.invoke.MethodHandles;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DtoBlueprintTest {

    @Test
    void constructor() {
        // Given
        final DtoSelectSpec dtoSelectSpec = new DtoSelectSpec(TestDto.class, ormTable(TestDto.class, "test_table"), new AliasGenerator());
        final List<Object> primaryKey = List.of(123L);
        final Row row = new Row().withColumn(new Column(new Table("", "public", "test_table"), "id"), 123L);

        // When
        final DtoBlueprint dtoBlueprint = new DtoBlueprint(dtoSelectSpec, primaryKey, row);

        // Then
        assertSame(dtoSelectSpec, dtoBlueprint.dtoData().spec());
        assertSame(primaryKey, dtoBlueprint.dtoData().primaryKey());
        assertSame(row, dtoBlueprint.dtoData().row());
        assertEquals(TestDto.class, dtoBlueprint.dtoData().dtoClass());
        assertEquals(List.of(), dtoBlueprint.joinedDtoData());
    }

    @Test
    void addJoinedDtoData() {
        // Given
        final DtoSelectSpec dtoSelectSpec = new DtoSelectSpec(TestDto.class, ormTable(TestDto.class, "test_table"), new AliasGenerator());
        final List<Object> primaryKey = List.of(123L);
        final Row row = new Row().withColumn(new Column(new Table("", "public", "test_table"), "id"), 123L);
        final DtoBlueprint dtoBlueprint = new DtoBlueprint(dtoSelectSpec, primaryKey, row);

        final Table joinTable = new Table("", "public", "joined_test_table");
        final DtoJoinSpec dtoJoinSpec = new DtoJoinSpec(JoinedTestDto.class, ormTable(JoinedTestDto.class, "joined_test_table"), joinTable);
        final List<Object> joinPrimaryKey = List.of(456L);
        final Row joinRow = new Row().withColumn(new Column(joinTable, "id"), 456L);

        // When
        dtoBlueprint.addJoinedDtoData(dtoJoinSpec, joinPrimaryKey, joinRow);

        // Then
        assertEquals(1, dtoBlueprint.joinedDtoData().size());
        assertSame(dtoJoinSpec, dtoBlueprint.joinedDtoData().getFirst().spec());
        assertSame(joinPrimaryKey, dtoBlueprint.joinedDtoData().getFirst().primaryKey());
        assertSame(joinRow, dtoBlueprint.joinedDtoData().getFirst().row());
        assertEquals(JoinedTestDto.class, dtoBlueprint.joinedDtoData().getFirst().dtoClass());
    }

    private static OrmTable ormTable(final Class<?> dtoClass, final String tableName) {
        final Table table = new Table("", "public", tableName);
        final ColumnMetaData idColumn = new ColumnMetaData(table, "id", false, Types.BIGINT);
        final TableMetaData tableMetaData = new TableMetaData(table, List.of("id"), List.of(idColumn));
        return new OrmTable(dtoClass, tableMetaData, Map.of(), new ChangeTracker(MethodHandles.lookup()), new ClassFieldAccessorCache(MethodHandles.lookup()));
    }

    private static class TestDto {
        private String myVar;
    }

    private static class JoinedTestDto {
        private String myVar;
    }
}
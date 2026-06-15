package org.litebridgedb.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.alias.DefaultAliasTransformer;
import org.litebridgedb.db.spi.function.SqlFunctionRegistry;
import org.litebridgedb.orm.api.dto.DtoJoinSpec;
import org.litebridgedb.orm.api.dto.DtoSelectSpec;
import org.litebridgedb.orm.function.SelectField;
import org.litebridgedb.orm.persistence.alias.DefaultAliasGenerator;
import org.litebridgedb.tracking.ChangeTracker;
import org.litebridgedb.tracking.ClassFieldAccessorCache;
import org.litebridgedb.tracking.FieldAccessor;

import java.lang.invoke.MethodHandles;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DtoBlueprintTest {

    @Test
    void constructor() {
        // Given
        final DtoSelectSpec dtoSelectSpec = new DtoSelectSpec(TestDto.class, createOrmTable(TestDto.class, "test_table"), new DefaultAliasGenerator(new DefaultAliasTransformer()), mock(SqlFunctionRegistry.class));
        final List<Object> primaryKey = List.of(123L);
        final Row row = new Row().withColumn(new Column(new Table("", "public", "test_table"), "id"), 123L);
        final FieldAccessor fieldAccessor = mock(FieldAccessor.class);
        when(fieldAccessor.name()).thenReturn("id");
        dtoSelectSpec.setExpressions(List.of(new SelectField(fieldAccessor, new Column(new Table("", "public", "test_table"), "id"))));

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
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final OrmTable ormTable = createOrmTable(TestDto.class, "test_table");
        final DtoSelectSpec dtoSelectSpec = new DtoSelectSpec(TestDto.class, ormTable, new DefaultAliasGenerator(new DefaultAliasTransformer()), mock(SqlFunctionRegistry.class));
        final FieldAccessor fieldAccessor = mock(FieldAccessor.class);
        when(fieldAccessor.name()).thenReturn("id");
        dtoSelectSpec.setExpressions(List.of(new SelectField(fieldAccessor, new Column(new Table(ormTable.getMetaData().catalog(), ormTable.getMetaData().schema(), ormTable.getMetaData().name()), "id"))));
        final List<Object> primaryKey = List.of(123L);
        final Row row = new Row().withColumn(new Column(ormTable.getMetaData().toTable(), "id"), 123L);
        final DtoBlueprint dtoBlueprint = new DtoBlueprint(dtoSelectSpec, primaryKey, row);

        final Table joinTable = new Table("", "public", "joined_test_table");
        final OrmTable joinOrmTable = createOrmTable(JoinedTestDto.class, joinTable.name());
        final DtoJoinSpec dtoJoinSpec = new DtoJoinSpec(JoinedTestDto.class, ormTable, joinTable);
        dtoJoinSpec.setFieldColumns(List.of(new DtoSelectSpec.FieldColumn(null, new Column(joinTable, "id"))));
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

    private static OrmTable createOrmTable(final Class<?> dtoClass, final String tableName) {
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
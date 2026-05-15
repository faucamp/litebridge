package org.litebridgedb.orm.api.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.Join;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.persistence.OrmTable;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoJoinSpecTest {

    private final List<DtoSelectSpec.FieldColumn> fieldColumns = List.of();
    private final OrmTable ormTable = Mockito.mock(OrmTable.class);
    private final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
    private final DtoJoinSpec dtoJoinSpec = new DtoJoinSpec(TestDto.class, ormTable, table);

    @BeforeEach
    void beforeEach() {
        dtoJoinSpec.setFieldColumns(fieldColumns);
    }

    @Test
    void dtoClass() {
        // When
        final Class<?> result = dtoJoinSpec.dtoClass();

        // Then
        assertEquals(TestDto.class, result);
    }

    @Test
    void dtoTable() {
        // When
        final OrmTable result = dtoJoinSpec.dtoTable();

        // Then
        assertSame(ormTable, result);
    }

    @Test
    void fieldColumns() {
        // When
        final List<DtoSelectSpec.FieldColumn> result = dtoJoinSpec.getFieldColumns();

        // Then
        assertSame(fieldColumns, result);
    }


    @Test
    void table() {
        // When
        final Table result = dtoJoinSpec.table();

        // Then
        assertSame(table, result);
    }

    @Test
    void conditions() {
        // When
        final ConditionSpec conditionSpec = dtoJoinSpec.newCondition(new Column(table, "TEST_COLUMN", "c1"));

        // Then
        assertNotNull(conditionSpec);
        assertNotNull(dtoJoinSpec.conditions());
        assertEquals(1, dtoJoinSpec.conditions().size());
    }

    @Test
    void toJoin() {
        // When
        final Join result = dtoJoinSpec.toJoin();

        // Then
        assertNotNull(result);
    }

    private static class TestDto {
    }
}
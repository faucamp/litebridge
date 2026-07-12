package org.litebridge.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.dto.DtoJoinSpec;
import org.litebridge.orm.api.dto.DtoSelectSpec;
import org.litebridge.orm.expression.DelegateExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.expression.select.SelectFieldSpec;
import org.litebridge.tracking.FieldAccessor;

import org.litebridge.orm.expression.function.scalar.LowerSpec;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DtoBlueprintTest {

    @Test
    void testDtoBlueprint() {
        final DtoSelectSpec selectSpec = mock(DtoSelectSpec.class);
        final List<Object> pk = List.of(1L);
        final Row row = mock(Row.class);
        final Table table = new Table("TEST");
        when(selectSpec.getTable()).thenReturn(table);
        when(selectSpec.dtoClass()).thenReturn((Class) Object.class);

        final SelectFieldSpec fieldSpec = mock(SelectFieldSpec.class);
        final Column column = mock(Column.class);
        final FieldAccessor fieldAccessor = mock(FieldAccessor.class);
        when(fieldAccessor.name()).thenReturn("field");
        when(fieldSpec.getColumn()).thenReturn(column);
        when(column.table()).thenReturn(table);
        when(fieldSpec.field()).thenReturn(fieldAccessor);
        when(selectSpec.getExpressions()).thenReturn(List.of(fieldSpec));

        final DtoBlueprint blueprint = new DtoBlueprint(selectSpec, pk, row);
        
        assertEquals(selectSpec, blueprint.dtoData().spec());
        assertEquals(pk, blueprint.dtoData().primaryKey());
        assertEquals(row, blueprint.dtoData().row());
        assertEquals(Object.class, blueprint.dtoData().dtoClass());
        assertEquals(1, blueprint.dtoData().fieldColumns().size());
        assertEquals("field", blueprint.dtoData().fieldColumns().get(0).fieldAccessor().name());

        final DtoJoinSpec joinSpec = mock(DtoJoinSpec.class);
        final List<Object> joinPk = List.of(2L);
        final Row joinRow = mock(Row.class);
        final List<DtoSelectSpec.FieldColumn> joinFields = Collections.emptyList();
        when(joinSpec.getFieldColumns()).thenReturn(joinFields);
        when(joinSpec.dtoClass()).thenReturn((Class) String.class);

        blueprint.addJoinedDtoData(joinSpec, joinPk, joinRow);
        assertEquals(1, blueprint.joinedDtoData().size());
        final DtoBlueprint.JoinDtoData joinData = blueprint.joinedDtoData().get(0);
        assertEquals(joinSpec, joinData.spec());
        assertEquals(joinPk, joinData.primaryKey());
        assertEquals(joinRow, joinData.row());
        assertEquals(String.class, joinData.dtoClass());
        assertSame(joinFields, joinData.fieldColumns());
    }

    @Test
    void testSelectDtoDataWithDelegate() {
        final DtoSelectSpec selectSpec = mock(DtoSelectSpec.class);
        final Table table = new Table("TEST");
        when(selectSpec.getTable()).thenReturn(table);

        final SelectFieldSpec fieldSpec = mock(SelectFieldSpec.class);
        final Column column = mock(Column.class);
        final FieldAccessor fieldAccessor = mock(FieldAccessor.class);
        when(fieldAccessor.name()).thenReturn("field");
        when(fieldSpec.getColumn()).thenReturn(column);
        when(column.table()).thenReturn(table);
        when(fieldSpec.field()).thenReturn(fieldAccessor);

        final LowerSpec delegate = new LowerSpec(fieldSpec);

        when(selectSpec.getExpressions()).thenReturn(List.of(delegate));

        final DtoBlueprint.SelectDtoData data = new DtoBlueprint.SelectDtoData(selectSpec, List.of(), mock(Row.class));
        assertEquals(1, data.fieldColumns().size());
        assertEquals("field", data.fieldColumns().get(0).fieldAccessor().name());
    }
}

package org.litebridge.orm.api.sql;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.persistence.TableRegistry;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class SqlSelectorTest {

    @Mock
    private DatabaseProvider databaseProvider;

    @Mock
    private TableRegistry tableRegistry;

    @InjectMocks
    private SqlSelector sqlSelector;

    @Test
    void select_basic_columnNames() throws Exception {
        // When
        final SelectTerminal<Map<String, Object>> result = sqlSelector.select("COL1", "COL2")
                .from("TABLE")
                .where("COL1").eq(123);

        // Then
        final Field selectSpecField = ReflectionSupport.streamFields(result.getClass(),
                        field -> field.getType() == SelectSpec.class,
                        HierarchyTraversalMode.BOTTOM_UP)
                .findFirst().orElseThrow();
        ReflectionSupport.makeAccessible(selectSpecField);
        final SelectSpec selectSpec = (SelectSpec) ReflectionSupport.tryToReadFieldValue(selectSpecField, result).get();

        assertNotNull(selectSpec);
        assertNotNull(selectSpec.getTable());
        assertEquals("TABLE", selectSpec.getTable().getTable());

        assertNotNull(selectSpec.getColumns());
        assertEquals(2, selectSpec.getColumns().size());
        assertEquals("COL1", selectSpec.getColumns().get(0).name());
        assertNull(selectSpec.getColumns().get(0).alias());
        assertEquals("COL2", selectSpec.getColumns().get(1).name());
        assertNull(selectSpec.getColumns().get(1).alias());

        assertNotNull(selectSpec.whereConditions());
        assertEquals(1, selectSpec.whereConditions().size());
        assertEquals("COL1", selectSpec.whereConditions().get(0).getColumn());
        assertEquals(Operator.EQ, selectSpec.whereConditions().get(0).getOperator());
        assertEquals(123, selectSpec.whereConditions().get(0).getValue());
    }

    @Test
    void select_basic_selectFields() {
    }

    @Test
    void toDto() {
    }
}
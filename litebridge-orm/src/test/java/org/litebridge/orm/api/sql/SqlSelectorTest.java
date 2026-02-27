package org.litebridge.orm.api.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.litebridge.db.spi.Aliased;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.persistence.TableRegistry;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class SqlSelectorTest {

    @Mock
    private DatabaseProvider databaseProvider;

    private TableRegistry tableRegistry;

    private SqlSelector sqlSelector;

    @BeforeEach
    void beforeEach() {
        tableRegistry = new TableRegistry();
        sqlSelector = new SqlSelector(databaseProvider, tableRegistry);
    }

    @Test
    void select_basic_columnNames() throws Exception {
        // When
        final SqlWhereConditionClauseTerminal result = sqlSelector.select("COL1", "COL2")
                .from("TABLE")
                .where("COL1").eq(123);

        // Then
        final Field selectSpecField = ReflectionSupport.streamFields(result.getClass(),
                        field -> field.getName().equals("selectSpec"),
                        HierarchyTraversalMode.BOTTOM_UP)
                .findFirst().orElseThrow();
        ReflectionSupport.makeAccessible(selectSpecField);
        final SqlSelectSpec selectSpec = (SqlSelectSpec) ReflectionSupport.tryToReadFieldValue(selectSpecField, result).get();

        assertNotNull(selectSpec);
        assertNotNull(selectSpec.getTable());
        assertEquals("TABLE", selectSpec.getTable().name());

        assertNotNull(selectSpec.getColumns());
        assertEquals(2, selectSpec.getColumns().size());
        assertEquals("COL1", selectSpec.getColumns().get(0).name());
        assertNull(selectSpec.getColumns().get(0).alias());
        assertEquals("COL2", selectSpec.getColumns().get(1).name());
        assertNull(selectSpec.getColumns().get(1).alias());

        assertNotNull(selectSpec.getWhereConditions());
        assertEquals(1, selectSpec.getWhereConditions().size());
        assertEquals("COL1", selectSpec.getWhereConditions().get(0).getColumn().name());
        assertEquals(selectSpec.getTable(), selectSpec.getWhereConditions().get(0).getColumn().table());
        assertEquals(Operator.EQ, selectSpec.getWhereConditions().get(0).getOperator());
        assertEquals(123, selectSpec.getWhereConditions().get(0).getValue());
    }

    @Test
    void select_basic_aliased() throws Exception {
        // When
        final SqlWhereConditionClauseTerminal result = sqlSelector.select(new Aliased("COL1", "col1Alias"), new Aliased("COL2", "col2Alias"))
                .from("TABLE")
                .where("col1Alias").eq(123);

        // Then
        final Field selectSpecField = ReflectionSupport.streamFields(result.getClass(),
                        field -> field.getName().equals("selectSpec"),
                        HierarchyTraversalMode.BOTTOM_UP)
                .findFirst().orElseThrow();
        ReflectionSupport.makeAccessible(selectSpecField);
        final SqlSelectSpec selectSpec = (SqlSelectSpec) ReflectionSupport.tryToReadFieldValue(selectSpecField, result).get();

        assertNotNull(selectSpec);
        assertNotNull(selectSpec.getTable());
        assertEquals("TABLE", selectSpec.getTable().name());

        assertNotNull(selectSpec.getColumns());
        assertEquals(2, selectSpec.getColumns().size());
        assertEquals("COL1", selectSpec.getColumns().get(0).name());
        assertEquals("col1Alias", selectSpec.getColumns().get(0).alias());
        assertEquals("COL2", selectSpec.getColumns().get(1).name());
        assertEquals("col2Alias", selectSpec.getColumns().get(1).alias());

        assertNotNull(selectSpec.getWhereConditions());
        assertEquals(1, selectSpec.getWhereConditions().size());
        assertEquals("col1Alias", selectSpec.getWhereConditions().get(0).getColumn().name());
        assertEquals(selectSpec.getTable(), selectSpec.getWhereConditions().get(0).getColumn().table());
        assertEquals(Operator.EQ, selectSpec.getWhereConditions().get(0).getOperator());
        assertEquals(123, selectSpec.getWhereConditions().get(0).getValue());
    }
}
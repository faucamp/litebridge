package org.litebridgedb.orm.api.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.litebridgedb.orm.expression.Fn.c;
import static org.litebridgedb.orm.expression.Fn.ca;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SqlSelectorTest {

    @Mock
    private TransactionalDatabaseProvider databaseProvider;

    private TableRegistry tableRegistry;

    private SqlSelector sqlSelector;

    @BeforeEach
    void beforeEach() {
        tableRegistry = new TableRegistry();
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        sqlSelector = new SqlSelector(databaseProvider, tableRegistry, mock(LitebridgeContext.class));
    }

    @Test
    void select_basic_columnNames() throws Exception {
        // When
        final SqlWhereConditionClauseTerminal result = sqlSelector.select(c("COL1"), c("COL2"))
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

        assertNotNull(selectSpec.getExpressions());
        assertEquals(2, selectSpec.getExpressions().size());
        assertEquals("COL1", ((SelectColumnSpec) selectSpec.getExpressions().get(0)).column().name());
        assertNull(((SelectColumnSpec) selectSpec.getExpressions().get(0)).column().alias());
        assertEquals("COL2", ((SelectColumnSpec) selectSpec.getExpressions().get(1)).column().name());
        assertNull(((SelectColumnSpec) selectSpec.getExpressions().get(1)).column().alias());

        assertNotNull(selectSpec.getWhereConditions());
        assertEquals(1, selectSpec.getWhereConditions().size());
        assertInstanceOf(ColumnExpressionSpec.class, selectSpec.getWhereConditions().get(0).getLhs());
        final Column column = ((ColumnExpressionSpec) selectSpec.getWhereConditions().get(0).getLhs()).column();
        assertEquals("COL1", column.name());
        assertEquals(selectSpec.getTable(), column.table());
        assertEquals(Operator.EQ, selectSpec.getWhereConditions().get(0).getOperator());
        assertEquals(123, selectSpec.getWhereConditions().get(0).getValue());
    }

    @Test
    void select_basic_aliased() throws Exception {
        // When
        final SqlWhereConditionClauseTerminal result = sqlSelector.select(ca("COL1", "col1Alias"), ca("COL2", "col2Alias"))
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

        assertNotNull(selectSpec.getExpressions());
        assertEquals(2, selectSpec.getExpressions().size());
        assertEquals("COL1", ((SelectColumnSpec) selectSpec.getExpressions().get(0)).column().name());
        assertEquals("col1Alias", ((SelectColumnSpec) selectSpec.getExpressions().get(0)).column().alias());
        assertEquals("COL2", ((SelectColumnSpec) selectSpec.getExpressions().get(1)).column().name());
        assertEquals("col2Alias", ((SelectColumnSpec) selectSpec.getExpressions().get(1)).column().alias());

        assertNotNull(selectSpec.getWhereConditions());
        assertEquals(1, selectSpec.getWhereConditions().size());
        final Column column = ((ColumnExpressionSpec) selectSpec.getWhereConditions().get(0).getLhs()).column();
        assertEquals("col1Alias", column.name());
        assertEquals(selectSpec.getTable(), column.table());
        assertEquals(Operator.EQ, selectSpec.getWhereConditions().get(0).getOperator());
        assertEquals(123, selectSpec.getWhereConditions().get(0).getValue());
    }
}
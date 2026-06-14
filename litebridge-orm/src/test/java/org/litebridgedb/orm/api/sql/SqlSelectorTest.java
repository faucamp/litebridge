package org.litebridgedb.orm.api.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.litebridgedb.db.spi.Aliased;
import org.litebridgedb.db.spi.function.SqlFunctionRegistry;
import org.litebridgedb.db.spi.query.ColumnExpression;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.orm.config.LitebridgeConfig;
import org.litebridgedb.orm.function.TestColumnExpressionFactory;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        when(sqlFunctionRegistry.selectColumnFactory()).thenReturn(new TestColumnExpressionFactory());
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        sqlSelector = new SqlSelector(databaseProvider, tableRegistry, new LitebridgeConfig());
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

        assertNotNull(selectSpec.getExpressions());
        assertEquals(2, selectSpec.getExpressions().size());
        assertEquals("COL1", ((ColumnExpression) selectSpec.getExpressions().get(0)).column().name());
        assertNull(((ColumnExpression) selectSpec.getExpressions().get(0)).column().alias());
        assertEquals("COL2", ((ColumnExpression) selectSpec.getExpressions().get(1)).column().name());
        assertNull(((ColumnExpression) selectSpec.getExpressions().get(1)).column().alias());

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

        assertNotNull(selectSpec.getExpressions());
        assertEquals(2, selectSpec.getExpressions().size());
        assertEquals("COL1", ((ColumnExpression) selectSpec.getExpressions().get(0)).column().name());
        assertEquals("col1Alias", ((ColumnExpression) selectSpec.getExpressions().get(0)).column().alias());
        assertEquals("COL2", ((ColumnExpression) selectSpec.getExpressions().get(1)).column().name());
        assertEquals("col2Alias", ((ColumnExpression) selectSpec.getExpressions().get(1)).column().alias());

        assertNotNull(selectSpec.getWhereConditions());
        assertEquals(1, selectSpec.getWhereConditions().size());
        assertEquals("col1Alias", selectSpec.getWhereConditions().get(0).getColumn().name());
        assertEquals(selectSpec.getTable(), selectSpec.getWhereConditions().get(0).getColumn().table());
        assertEquals(Operator.EQ, selectSpec.getWhereConditions().get(0).getOperator());
        assertEquals(123, selectSpec.getWhereConditions().get(0).getValue());
    }
}
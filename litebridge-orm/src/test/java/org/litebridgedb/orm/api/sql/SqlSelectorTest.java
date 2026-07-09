package org.litebridgedb.orm.api.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.expression.ProtoColumnExpressionSpec;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertEquals("COL1", ((ProtoColumnExpressionSpec) selectSpec.getExpressions().get(0)).column());
        assertNull(((ProtoColumnExpressionSpec) selectSpec.getExpressions().get(0)).alias());
        assertEquals("COL2", ((ProtoColumnExpressionSpec) selectSpec.getExpressions().get(1)).column());
        assertNull(((ProtoColumnExpressionSpec) selectSpec.getExpressions().get(1)).alias());
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
        assertEquals("COL1", ((ProtoColumnExpressionSpec) selectSpec.getExpressions().get(0)).column());
        assertEquals("col1Alias", ((ProtoColumnExpressionSpec) selectSpec.getExpressions().get(0)).alias());
        assertEquals("COL2", ((ProtoColumnExpressionSpec) selectSpec.getExpressions().get(1)).column());
        assertEquals("col2Alias", ((ProtoColumnExpressionSpec) selectSpec.getExpressions().get(1)).alias());
    }
}
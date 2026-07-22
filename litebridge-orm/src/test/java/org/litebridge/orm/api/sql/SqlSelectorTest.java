package org.litebridge.orm.api.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ProtoColumnExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.litebridge.orm.expression.Fn.c;
import static org.litebridge.orm.expression.Fn.ca;
import static org.junit.platform.commons.support.ReflectionSupport.tryToReadFieldValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
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
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
        final org.litebridge.db.spi.expression.ColumnExpressionFactory columnFactory = mock(org.litebridge.db.spi.expression.ColumnExpressionFactory.class);
        final org.litebridge.db.spi.expression.SelectReferenceExpressionFactory referenceFactory = mock(org.litebridge.db.spi.expression.SelectReferenceExpressionFactory.class);
        final org.litebridge.db.spi.expression.LiteralExpressionFactory literalFactory = mock(org.litebridge.db.spi.expression.LiteralExpressionFactory.class);
        lenient().when(context.sqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        lenient().when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
        lenient().when(selectRegistry.column()).thenReturn(columnFactory);
        lenient().when(selectRegistry.reference()).thenReturn(referenceFactory);
        lenient().when(selectRegistry.literal()).thenReturn(literalFactory);
        lenient().when(literalFactory.create(any(), any(boolean.class))).thenAnswer(invocation -> new org.litebridge.db.spi.expression.LiteralExpression(invocation.getArgument(0), invocation.getArgument(1)));
        lenient().when(columnFactory.create(any())).thenAnswer(invocation -> {
            final org.litebridge.db.spi.expression.ColumnExpression ce = mock(org.litebridge.db.spi.expression.ColumnExpression.class);
            lenient().when(ce.column()).thenReturn(invocation.getArgument(0));
            return ce;
        });
        sqlSelector = new SqlSelector(databaseProvider, tableRegistry, context, null);
    }

    @Test
    void select_basic_columnNames() throws Exception {
        // When
        final SqlWhereConditionClauseTerminal result = sqlSelector.select(c("COL1"), c("COL2"))
                .from("TABLE")
                .where("COL1").eq(123);

        // Then
        final Field delegateField = ReflectionSupport.streamFields(result.getClass(),
                        field -> field.getName().equals("delegate"),
                        HierarchyTraversalMode.BOTTOM_UP)
                .findFirst().orElseThrow();
        ReflectionSupport.makeAccessible(delegateField);
        final SqlSelector selector = (SqlSelector) ReflectionSupport.tryToReadFieldValue(delegateField, result).get();
        final SqlSelectSpec selectSpec = selector.compile();

        assertNotNull(selectSpec);
        assertNotNull(selectSpec.getTable());
        assertEquals("TABLE", selectSpec.getTable().name());

        assertNotNull(selectSpec.getExpressions());
        assertEquals(2, selectSpec.getExpressions().size());
        assertEquals("COL1", ((org.litebridge.orm.expression.ProtoColumnExpressionSpec) selectSpec.getExpressions().get(0)).column());
        assertNull(((org.litebridge.orm.expression.ProtoColumnExpressionSpec) selectSpec.getExpressions().get(0)).alias());
        assertEquals("COL2", ((org.litebridge.orm.expression.ProtoColumnExpressionSpec) selectSpec.getExpressions().get(1)).column());
        assertNull(((org.litebridge.orm.expression.ProtoColumnExpressionSpec) selectSpec.getExpressions().get(1)).alias());
    }

    @Test
    void select_basic_aliased() throws Exception {
        // When
        final SqlWhereConditionClauseTerminal result = sqlSelector.select(ca("COL1", "col1Alias"), ca("COL2", "col2Alias"))
                .from("TABLE")
                .where("col1Alias").eq(123);

        // Then
        final Field delegateField = ReflectionSupport.streamFields(result.getClass(),
                        field -> field.getName().equals("delegate"),
                        HierarchyTraversalMode.BOTTOM_UP)
                .findFirst().orElseThrow();
        ReflectionSupport.makeAccessible(delegateField);
        final SqlSelector selector = (SqlSelector) ReflectionSupport.tryToReadFieldValue(delegateField, result).get();
        final SqlSelectSpec selectSpec = selector.compile();

        assertNotNull(selectSpec);
        assertNotNull(selectSpec.getTable());
        assertEquals("TABLE", selectSpec.getTable().name());

        assertNotNull(selectSpec.getExpressions());
        assertEquals(2, selectSpec.getExpressions().size());
        assertEquals("COL1", ((org.litebridge.orm.expression.ProtoColumnExpressionSpec) selectSpec.getExpressions().get(0)).column());
        assertEquals("col1Alias", ((org.litebridge.orm.expression.ProtoColumnExpressionSpec) selectSpec.getExpressions().get(0)).alias());
        assertEquals("COL2", ((org.litebridge.orm.expression.ProtoColumnExpressionSpec) selectSpec.getExpressions().get(1)).column());
        assertEquals("col2Alias", ((org.litebridge.orm.expression.ProtoColumnExpressionSpec) selectSpec.getExpressions().get(1)).alias());
    }
}
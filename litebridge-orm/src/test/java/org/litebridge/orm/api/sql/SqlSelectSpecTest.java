//package org.litebridge.orm.api.sql;
//
//import org.junit.jupiter.api.Test;
//import org.litebridge.db.spi.Column;
//import org.litebridge.db.spi.PreparedOperation;
//import org.litebridge.db.spi.Table;
//import org.litebridge.db.spi.convert.TypeConverter;
//import org.litebridge.db.spi.expression.LiteralExpression;
//import org.litebridge.db.spi.expression.SqlFunctionRegistry;
//import org.litebridge.db.spi.query.LogicOperator;
//import org.litebridge.db.spi.query.Operator;
//import org.litebridge.db.spi.query.Select;
//import org.litebridge.orm.api.select.model.ConditionGroupSpec;
//import org.litebridge.orm.api.select.model.ConditionSpec;
//import org.litebridge.orm.api.select.model.JoinSpec;
//import org.litebridge.orm.api.select.model.LimitSpec;
//import org.litebridge.orm.api.select.model.OrderBySpec;
//import org.litebridge.orm.api.select.model.ProtoExpressionResolver;
//import org.litebridge.orm.engine.LitebridgeContext;
//import org.litebridge.orm.expression.ExpressionSpec;
//import org.litebridge.orm.expression.TestColumnExpression;
//import org.litebridge.orm.expression.TestColumnExpressionFactory;
//import org.litebridge.orm.expression.TestSelectReferenceExpressionFactory;
//import org.litebridge.orm.expression.select.SelectColumnSpec;
//import org.litebridge.orm.persistence.TableMetaDataCache;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertInstanceOf;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertSame;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//class SqlSelectSpecTest {
//
//    @Test
//    void getTable() {
//        // Given
//        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
//        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(LitebridgeContext.class), table);
//
//        // When
//        final Table result = sqlSelectSpec.getTable();
//
//        // Then
//        assertSame(table, result);
//    }
//
//    @Test
//    void setExpressions() {
//        // Given
//        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
//        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(LitebridgeContext.class), table);
//        sqlSelectSpec.setTable(table);
//        final Column column = new Column(table, "TEST_COLUMN");
//
//        // When
//        sqlSelectSpec.setExpressions(List.of(new SelectColumnSpec(column)));
//        final List<ExpressionSpec> result = sqlSelectSpec.getExpressions();
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertInstanceOf(SelectColumnSpec.class, result.getFirst());
//        assertSame(column, ((SelectColumnSpec) result.getFirst()).getColumn());
//    }
//
//    @Test
//    void addExpressions() {
//        // Given
//        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
//        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(LitebridgeContext.class), table);
//        sqlSelectSpec.setTable(table);
//        final Column column = new Column(table, "TEST_COLUMN");
//
//        // When
//        sqlSelectSpec.addExpressions(List.of(new SelectColumnSpec(column)));
//        final List<ExpressionSpec> result = sqlSelectSpec.getExpressions();
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertInstanceOf(SelectColumnSpec.class, result.getFirst());
//        assertSame(column, ((SelectColumnSpec) result.getFirst()).getColumn());
//    }
//
//    @Test
//    void getJoins() {
//        // Given
//        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
//        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(LitebridgeContext.class), table);
//        sqlSelectSpec.setProtoExpressionResolver(mock(ProtoExpressionResolver.class));
//        sqlSelectSpec.setTable(table);
//
//        // When
//        final JoinSpec joinSpec = sqlSelectSpec.newJoinSpec("TEST_SCHEMA.TEST_TABLE");
//        final List<JoinSpec> result = sqlSelectSpec.getJoins();
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertSame(joinSpec, result.getFirst());
//    }
//
//    @Test
//    void newJoinSpec() {
//        // Given
//        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
//        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(LitebridgeContext.class), table);
//        sqlSelectSpec.setProtoExpressionResolver(mock(ProtoExpressionResolver.class));
//        sqlSelectSpec.setTable(table);
//        final Column column = new Column(table, "TEST_COLUMN");
//        sqlSelectSpec.setExpressions(List.of(new SelectColumnSpec(column)));
//
//        // When
//        final JoinSpec result = sqlSelectSpec.newJoinSpec("TEST_SCHEMA.TEST_TABLE2");
//
//        // Then
//        assertNotNull(result);
//        assertNotNull(sqlSelectSpec.getJoins());
//        assertEquals(1, sqlSelectSpec.getJoins().size());
//        assertSame(result, sqlSelectSpec.getJoins().getFirst());
//    }
//
//    @Test
//    void newJoinSpec_noSchema() {
//        // Given
//        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
//        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(LitebridgeContext.class), table);
//        sqlSelectSpec.setProtoExpressionResolver(mock(ProtoExpressionResolver.class));
//        sqlSelectSpec.setTable(table);
//        final Column column = new Column(table, "TEST_COLUMN");
//        sqlSelectSpec.setExpressions(List.of(new SelectColumnSpec(column)));
//
//        // When
//        final JoinSpec result = sqlSelectSpec.newJoinSpec("TEST_TABLE2");
//
//        // Then
//        assertNotNull(result);
//        assertNotNull(sqlSelectSpec.getJoins());
//        assertEquals(1, sqlSelectSpec.getJoins().size());
//        assertSame(result, sqlSelectSpec.getJoins().getFirst());
//    }
//
//    @Test
//    void pushWhereConditionGroup() {
//        // Given
//        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
//        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(LitebridgeContext.class), table);
//        sqlSelectSpec.setTable(table);
//        final Column column = new Column(table, "TEST_COLUMN");
//        sqlSelectSpec.setExpressions(List.of(new SelectColumnSpec(column)));
//        final ConditionSpec conditionSpec = new ConditionSpec();
//        conditionSpec.setLhsExpression(column);
//        conditionSpec.setOperator(Operator.LTE);
//        conditionSpec.setValue(123);
//
//        // When
//        final ConditionGroupSpec result = sqlSelectSpec.pushWhereConditionGroup(LogicOperator.OR);
//
//        // Then
//        assertNotNull(result);
//    }
//
//    @Test
//    void newOrderBy() {
//        // Given
//        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
//        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(LitebridgeContext.class), table);
//        sqlSelectSpec.setTable(table);
//        final Column column = new Column(table, "TEST_COLUMN");
//        sqlSelectSpec.setExpressions(List.of(new SelectColumnSpec(column)));
//        final SelectColumnSpec selectColumnSpec = new SelectColumnSpec(new Column(new Table("TEST_TABLE"), "TEST_COLUMN"));
//
//        // When
//        final OrderBySpec result = sqlSelectSpec.newOrderBy(selectColumnSpec);
//
//        // Then
//        assertNotNull(result);
//        assertNotNull(sqlSelectSpec.getOrderBys());
//        assertEquals(1, sqlSelectSpec.getOrderBys().size());
//        assertSame(result, sqlSelectSpec.getOrderBys().getFirst());
//    }
//
//    @Test
//    void ensureLimit() {
//        // Given
//        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
//        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(LitebridgeContext.class), table);
//        sqlSelectSpec.setTable(table);
//        final Column column = new Column(table, "TEST_COLUMN");
//        sqlSelectSpec.setExpressions(List.of(new SelectColumnSpec(column)));
//        final SelectColumnSpec selectColumnSpec = new SelectColumnSpec(new Column(new Table("TEST_TABLE"), "TEST_COLUMN"));
//        sqlSelectSpec.newOrderBy(selectColumnSpec);
//
//        // When
//        final LimitSpec result = sqlSelectSpec.ensureLimit();
//
//        // Then
//        assertNotNull(result);
//    }
//
//
//    @Test
//    void toSelect() {
//        // Given
//        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
//        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
//        when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
//        when(selectRegistry.column()).thenReturn(new TestColumnExpressionFactory());
//        when(selectRegistry.reference()).thenReturn(new TestSelectReferenceExpressionFactory());
//        when(selectRegistry.literal()).thenReturn(LiteralExpression::new);
//
//        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
//        when(litebridgeContext.sqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
//        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
//        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(litebridgeContext, table);
//        sqlSelectSpec.setProtoExpressionResolver(new SqlProtoExpressionResolver(sqlSelectSpec));
//        sqlSelectSpec.setTable(table);
//        final Column column = new Column(table, "TEST_COLUMN");
//        sqlSelectSpec.setExpressions(List.of(new SelectColumnSpec(column)));
//        final SelectColumnSpec selectColumnSpec = new SelectColumnSpec(new Column(new Table("TEST_TABLE"), "TEST_COLUMN"));
//        sqlSelectSpec.newOrderBy(selectColumnSpec);
//        final LimitSpec limitSpec = sqlSelectSpec.ensureLimit();
//        limitSpec.setOffset(100);
//        limitSpec.setLimit(200);
//
//        // When
//        final PreparedOperation result = sqlSelectSpec.toSelect(mock(TableMetaDataCache.class), mock(TypeConverter.class));
//
//        // Then
//        assertNotNull(result);
//        final Select select = (Select) result.operation();
//        assertNotNull(select);
//        assertEquals(table, select.table());
//        assertNotNull(select.expressions());
//        assertEquals(1, select.expressions().size());
//        assertInstanceOf(TestColumnExpression.class, select.expressions().getFirst());
//        assertEquals(column, ((TestColumnExpression) select.expressions().getFirst()).column());
//    }
//
//    @Test
//    void toSelect_expressionsNotSet() {
//        // Given
//        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
//        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(LitebridgeContext.class), table);
//        sqlSelectSpec.setProtoExpressionResolver(new SqlProtoExpressionResolver(sqlSelectSpec));
//        sqlSelectSpec.setTable(table);
//
//        // When
//        final PreparedOperation result = sqlSelectSpec.toSelect(mock(TableMetaDataCache.class), mock(TypeConverter.class));
//
//        // Then
//        assertNotNull(result);
//        final Select select = (Select) result.operation();
//        assertEquals(table, select.table());
//        assertNotNull(select.expressions());
//        assertTrue(select.expressions().isEmpty());
//    }
//}
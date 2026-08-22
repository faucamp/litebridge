//package org.litebridge.orm.engine;
//
//import org.junit.jupiter.api.Test;
//import org.litebridge.db.spi.Column;
//import org.litebridge.db.spi.math.MathOperation;
//import org.litebridge.db.spi.query.LogicOperator;
//import org.litebridge.db.spi.query.Operator;
//import org.litebridge.orm.api.select.SelectTerminal;
//import org.litebridge.orm.api.select.ast.ConditionGroupNode;
//import org.litebridge.orm.api.select.ast.ConditionNode;
//import org.litebridge.orm.api.select.ast.HavingNode;
//import org.litebridge.orm.api.select.ast.InsertNode;
//import org.litebridge.orm.api.select.ast.JoinNode;
//import org.litebridge.orm.api.select.ast.QueryNode;
//import org.litebridge.orm.api.select.ast.SelectNode;
//import org.litebridge.orm.api.select.ast.SetNode;
//import org.litebridge.orm.api.select.ast.WhereNode;
//import org.litebridge.orm.api.select.impl.SelectTerminalInspector;
//import org.litebridge.orm.expression.ExpressionSpec;
//import org.litebridge.orm.expression.select.SelectColumnSpec;
//import org.mockito.MockedStatic;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.mockStatic;
//
//class QueryBindValueExtractorTest {
//
//    @Test
//    void testExtractBindValuesFromWhereNode() {
//        // Given
//        final ConditionNode condition = new ConditionNode(null, LogicOperator.AND, mock(SelectColumnSpec.class), Operator.EQ, "value1");
//        final WhereNode whereNode = new WhereNode(null, condition);
//
//        // When
//        final List<Object> bindValues = QueryBindValueExtractor.extractBindValues(whereNode);
//
//        // Then
//        assertEquals(List.of("value1"), bindValues);
//    }
//
//    @Test
//    void testExtractBindValuesFromHavingNode() {
//        // Given
//        final ConditionNode condition = new ConditionNode(null, LogicOperator.AND, mock(SelectColumnSpec.class), Operator.GT, 10);
//        final HavingNode havingNode = new HavingNode(null, condition);
//
//        // When
//        final List<Object> bindValues = QueryBindValueExtractor.extractBindValues(havingNode);
//
//        // Then
//        assertEquals(List.of(10), bindValues);
//    }
//
//    @Test
//    void testExtractBindValuesFromJoinNode() {
//        // Given
//        final ConditionNode condition = new ConditionNode(null, LogicOperator.AND, mock(SelectColumnSpec.class), Operator.EQ, "joinValue");
//        final JoinNode joinNode = new JoinNode(null, "INNER", Object.class, null, "table").withCondition(condition);
//
//        // When
//        final List<Object> bindValues = QueryBindValueExtractor.extractBindValues(joinNode);
//
//        // Then
//        assertEquals(List.of("joinValue"), bindValues);
//    }
//
//    @Test
//    void testExtractBindValuesFromJoinNodeNoCondition() {
//        // Given
//        final JoinNode joinNode = new JoinNode(null, "INNER", Object.class, null, "table");
//
//        // When
//        final List<Object> bindValues = QueryBindValueExtractor.extractBindValues(joinNode);
//
//        // Then
//        assertTrue(bindValues.isEmpty());
//    }
//
//    @Test
//    void testExtractBindValuesFromSetNode() {
//        // Given
//        final SetNode setNode1 = new SetNode(null, mock(Column.class), "val1", true);
//        final SetNode setNode2 = new SetNode(setNode1, mock(Column.class), "val2", true);
//        final SetNode setNode3 = new SetNode(setNode2, mock(Column.class), "val3", false);
//
//        // When
//        final List<Object> bindValues = QueryBindValueExtractor.extractBindValues(setNode3);
//
//        // Then
//        assertEquals(Arrays.asList("val1", "val2"), bindValues);
//    }
//
//    @Test
//    void testExtractBindValuesFromSetNodeWithExpressionOrColumn() {
//        // Given
//        final SetNode setNode1 = new SetNode(null, mock(Column.class), mock(Column.class), true);
//        final SetNode setNode2 = new SetNode(setNode1, mock(Column.class), mock(SelectColumnSpec.class), true);
//        final SetNode setNode3 = new SetNode(setNode2, mock(Column.class), mock(MathOperation.class), true);
//        final SetNode setNode4 = new SetNode(setNode3, mock(Column.class), "realValue", true);
//
//        // When
//        final List<Object> bindValues = QueryBindValueExtractor.extractBindValues(setNode4);
//
//        // Then
//        assertEquals(List.of("realValue"), bindValues);
//    }
//
//    @Test
//    void testExtractBindValuesWithConditionOperatorsToSkip() {
//        // Given
//        final ConditionNode c1 = new ConditionNode(null, LogicOperator.AND, mock(SelectColumnSpec.class), Operator.IS_NULL, null);
//        final ConditionNode c2 = new ConditionNode(c1, LogicOperator.AND, mock(SelectColumnSpec.class), Operator.IS_NOT_NULL, null);
//        final ConditionNode c3 = new ConditionNode(c2, LogicOperator.AND, mock(SelectColumnSpec.class), Operator.USING, "col");
//        final WhereNode whereNode = new WhereNode(null, c3);
//
//        // When
//        final List<Object> bindValues = QueryBindValueExtractor.extractBindValues(whereNode);
//
//        // Then
//        assertTrue(bindValues.isEmpty());
//    }
//
//    @Test
//    void testExtractBindValuesWithCollectionRhs() {
//        // Given
//        final ConditionNode condition = new ConditionNode(null, LogicOperator.AND, mock(SelectColumnSpec.class), Operator.IN, List.of("a", "b", "c"));
//        final WhereNode whereNode = new WhereNode(null, condition);
//
//        // When
//        final List<Object> bindValues = QueryBindValueExtractor.extractBindValues(whereNode);
//
//        // Then
//        assertEquals(Arrays.asList("a", "b", "c"), bindValues);
//    }
//
//    @Test
//    void testExtractBindValuesWithColumnOrExpressionRhs() {
//        // Given
//        final ConditionNode c1 = new ConditionNode(null, LogicOperator.AND, mock(SelectColumnSpec.class), Operator.EQ, mock(Column.class));
//        final ConditionNode c2 = new ConditionNode(c1, LogicOperator.AND, mock(SelectColumnSpec.class), Operator.EQ, mock(SelectColumnSpec.class));
//        final WhereNode whereNode = new WhereNode(null, c2);
//
//        // When
//        final List<Object> bindValues = QueryBindValueExtractor.extractBindValues(whereNode);
//
//        // Then
//        assertTrue(bindValues.isEmpty());
//    }
//
//    @Test
//    @SuppressWarnings("unchecked")
//    void testExtractBindValuesWithSelectTerminalRhs() {
//        // Given
//        final SelectTerminal<?> st = mock(SelectTerminal.class);
//        final ConditionNode innerCondition = new ConditionNode(null, LogicOperator.AND, mock(SelectColumnSpec.class), Operator.EQ, "innerValue");
//        final QueryNode innerNode = new WhereNode(null, innerCondition);
//
//        try (MockedStatic<SelectTerminalInspector> inspector = mockStatic(SelectTerminalInspector.class)) {
//            inspector.when(() -> SelectTerminalInspector.getNode(st)).thenReturn(innerNode);
//
//            final ConditionNode condition = new ConditionNode(null, LogicOperator.AND, mock(SelectColumnSpec.class), Operator.EQ, st);
//            final WhereNode whereNode = new WhereNode(null, condition);
//
//            // When
//            final List<Object> bindValues = QueryBindValueExtractor.extractBindValues(whereNode);
//
//            // Then
//            assertEquals(List.of("innerValue"), bindValues);
//        }
//    }
//
//    @Test
//    void testExtractBindValuesWithQueryNodeRhs() {
//        // Given
//        final ConditionNode innerCondition = new ConditionNode(null, LogicOperator.AND, mock(SelectColumnSpec.class), Operator.EQ, "innerValue");
//        final QueryNode innerNode = new WhereNode(null, innerCondition);
//
//        final ConditionNode condition = new ConditionNode(null, LogicOperator.AND, mock(SelectColumnSpec.class), Operator.EQ, innerNode);
//        final WhereNode whereNode = new WhereNode(null, condition);
//
//        // When
//        final List<Object> bindValues = QueryBindValueExtractor.extractBindValues(whereNode);
//
//        // Then
//        assertEquals(List.of("innerValue"), bindValues);
//    }
//
//    @Test
//    void testExtractBindValuesWithConditionGroup() {
//        // Given
//        final ConditionNode c1 = new ConditionNode(null, LogicOperator.AND, mock(SelectColumnSpec.class), Operator.EQ, "v1");
//
//        final ConditionNode g1c1 = new ConditionNode(null, LogicOperator.AND, mock(SelectColumnSpec.class), Operator.EQ, "gv1");
//        final ConditionGroupNode groupNode = new ConditionGroupNode(c1, LogicOperator.OR, g1c1);
//
//        final ConditionNode c2 = new ConditionNode(groupNode, LogicOperator.AND, mock(SelectColumnSpec.class), Operator.EQ, "v2");
//
//        final WhereNode whereNode = new WhereNode(null, c2);
//
//        // When
//        final List<Object> bindValues = QueryBindValueExtractor.extractBindValues(whereNode);
//
//        // Then
//        assertEquals(Arrays.asList("v1", "v2", "gv1"), bindValues);
//    }
//
//    @Test
//    void testExtractBindValuesWithInsertNodeAndOtherNodes() {
//        // Given
//        final SelectNode selectNode = new SelectNode(null, new ExpressionSpec[0], null);
//        final InsertNode insertNode = new InsertNode(selectNode, "TEST_TABLE", new String[0]);
//        final SetNode setNode = new SetNode(insertNode, mock(Column.class), "val", true);
//
//        // When
//        final List<Object> bindValues = QueryBindValueExtractor.extractBindValues(setNode);
//
//        // Then
//        assertEquals(List.of("val"), bindValues);
//    }
//}

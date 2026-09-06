package org.litebridge.orm.engine;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.math.MathOperation;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.ConditionWithIdNode;
import org.litebridge.orm.engine.ast.HavingNode;
import org.litebridge.orm.engine.ast.InsertNode;
import org.litebridge.orm.engine.ast.InsertValuesNode;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.SetNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class QueryBindValueExtractorTest {

    @Test
    void extractBindValues_whereValuesInConditionOrder() {
        // Given
        final ConditionNode first = new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, "first");
        final ConditionNode last = new ConditionNode(first, LogicOperator.AND, null, null, Operator.GT, 10);

        // When
        final List<Object> result = QueryBindValueExtractor.extractBindValues(new WhereNode(null, last));

        // Then
        assertEquals(List.of("first", 10), result);
    }

    @Test
    void extractBindValues_havingAndJoinValues() {
        // Given
        final ConditionNode havingCondition = new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, "having");
        final HavingNode having = new HavingNode(null, havingCondition);
        final JoinNode joinWithoutCondition = new JoinNode(having, "INNER", Object.class, "other");
        final ConditionNode joinCondition = new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, "join");
        final JoinNode join = new JoinNode(joinWithoutCondition, "INNER", Object.class, "other").withCondition(joinCondition);

        // When
        final List<Object> result = QueryBindValueExtractor.extractBindValues(join);

        // Then
        assertEquals(List.of("having", "join"), result);
    }

    @Test
    void extractBindValues_skipNonBindableSetValues() {
        // Given
        final SetNode column = new SetNode(null, "column", mock(Column.class));
        final SetNode expression = new SetNode(column, "column", mock(SelectColumnSpec.class));
        final SetNode math = new SetNode(expression, "column", mock(MathOperation.class));
        final SetNode value = new SetNode(math, "column", "bound");

        // When
        final List<Object> result = QueryBindValueExtractor.extractBindValues(value);

        // Then
        assertEquals(List.of("bound"), result);
    }

    @Test
    void extractBindValues_skipUnboundSetValues() {
        // Given
        final SetNode set = new SetNode(null, "column", null, "not-bound", false);

        // When
        final List<Object> result = QueryBindValueExtractor.extractBindValues(set);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void extractsCollectionsAndConditionIdsButSkipsOperatorsAndExpressions() {
        // Given
        final ConditionNode skippedNull = new ConditionNode(null, LogicOperator.AND, null, null, Operator.IS_NULL, null);
        final ConditionNode skippedNotNull = new ConditionNode(skippedNull, LogicOperator.AND, null, null, Operator.IS_NOT_NULL, null);
        final ConditionNode skippedUsing = new ConditionNode(skippedNotNull, LogicOperator.AND, null, null, Operator.USING, "column");
        final ConditionNode expression = new ConditionNode(skippedUsing, LogicOperator.AND, null, null, Operator.EQ, mock(Column.class));
        final ConditionNode values = new ConditionNode(expression, LogicOperator.AND, null, null, Operator.IN, List.of("a", "b"));
        final ConditionWithIdNode id = new ConditionWithIdNode(values, LogicOperator.AND, Operator.EQ, 42L);

        // When
        final List<Object> result = QueryBindValueExtractor.extractBindValues(new WhereNode(null, id));

        // Then
        assertEquals(List.of("a", "b", 42L), result);
    }

    @Test
    void extractBindValues_valuesFromNestedQueryNodesAndSelectTerminals() {
        // Given
        final ConditionNode nestedCondition = new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, "nested");
        final WhereNode nestedQuery = new WhereNode(null, nestedCondition);
        final ConditionNode queryRhs = new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, nestedQuery);
        assertEquals(List.of("nested"), QueryBindValueExtractor.extractBindValues(new WhereNode(null, queryRhs)));

        final SelectTerminal<?> terminal = mock(SelectTerminal.class);
        try (MockedStatic<SelectTerminalInspector> inspector = mockStatic(SelectTerminalInspector.class)) {
            inspector.when(() -> SelectTerminalInspector.getNode(terminal)).thenReturn(nestedQuery);
            final ConditionNode terminalRhs = new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, terminal);

            // When
            final List<Object> result = QueryBindValueExtractor.extractBindValues(new WhereNode(null, terminalRhs));

            // Then
            assertEquals(List.of("nested"), result);
        }
    }

    @Test
    void extractBindValues_conditionsBeforeNestedGroupsAndInsertRows() {
        // Given
        final ConditionNode first = new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, "first");
        final ConditionNode nested = new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, "nested");
        final ConditionGroupNode group = new ConditionGroupNode(first, LogicOperator.OR, nested);
        final ConditionNode last = new ConditionNode(group, LogicOperator.AND, null, null, Operator.EQ, "last");
        final WhereNode where = new WhereNode(null, last);
        assertEquals(List.of("first", "last", "nested"), QueryBindValueExtractor.extractBindValues(where));

        final InsertNode insert = new InsertNode("table", Object.class, new String[]{"a", "b"});
        final InsertValuesNode values = new InsertValuesNode(insert, new Object[]{"row-a", null});

        // When
        final List<Object> result = QueryBindValueExtractor.extractBindValues(values);

        // Then
        assertEquals(Arrays.asList("row-a", null), result);
    }
}

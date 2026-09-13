package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ConditionWithIdNodeTest {

    @Test
    void recordComponentsAndGetters() {
        // Given
        final QueryNode previous = new DeleteNode(null, "p1", null);
        final LogicOperator logicOp = LogicOperator.AND;
        final Operator op = Operator.EQ;
        final Object id = 100L;

        // When
        final ConditionWithIdNode node = new ConditionWithIdNode(previous, logicOp, op, id);

        // Then
        assertSame(previous, node.previous());
        assertEquals(logicOp, node.logicOperator());
        assertEquals(op, node.operator());
        assertEquals(id, node.id());
    }

    @Test
    void equals_hashCode() {
        // Given
        final QueryNode prev1 = new DeleteNode(null, "p1", null);
        final QueryNode prev2 = new DeleteNode(null, "p2", null);

        final ConditionWithIdNode base = new ConditionWithIdNode(prev1, LogicOperator.AND, Operator.EQ, 100L);
        final ConditionWithIdNode same = new ConditionWithIdNode(prev1, LogicOperator.AND, Operator.EQ, 100L);

        final ConditionWithIdNode diffPrev = new ConditionWithIdNode(prev2, LogicOperator.AND, Operator.EQ, 100L);
        final ConditionWithIdNode diffLogic = new ConditionWithIdNode(prev1, LogicOperator.OR, Operator.EQ, 100L);
        final ConditionWithIdNode diffOp = new ConditionWithIdNode(prev1, LogicOperator.AND, Operator.NEQ, 100L);

        // Scalars both produce structural key 1
        final ConditionWithIdNode sameScalarId = new ConditionWithIdNode(prev1, LogicOperator.AND, Operator.EQ, 200L);

        // Collections of different sizes produce different structural keys
        final ConditionWithIdNode col1 = new ConditionWithIdNode(prev1, LogicOperator.AND, Operator.IN, List.of(1));
        final ConditionWithIdNode col2 = new ConditionWithIdNode(prev1, LogicOperator.AND, Operator.IN, List.of(1, 2));

        // When / Then
        assertEquals(base, base);
        assertEquals(base, same);
        assertEquals(base.hashCode(), same.hashCode());

        assertEquals(base, sameScalarId);
        assertEquals(base.hashCode(), sameScalarId.hashCode());

        assertNotEquals(base, diffPrev);
        assertNotEquals(base, diffLogic);
        assertNotEquals(base, diffOp);

        assertNotEquals(col1, col2);
        assertNotEquals(col1.hashCode(), col2.hashCode());

        assertNotEquals(base, null);
        assertNotEquals(base, "other");
    }
}

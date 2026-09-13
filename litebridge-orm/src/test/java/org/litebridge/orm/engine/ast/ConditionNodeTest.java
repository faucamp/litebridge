package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ConditionNodeTest {

    @Test
    void constructorWithoutRhsColumnAndGetters() {
        // Given
        final QueryNode previous = new DeleteNode(null, "p1", null);
        final LogicOperator logicOp = LogicOperator.AND;
        final String lhsCol = "age";
        final ExpressionSpec lhsExpr = new SelectColumnSpec(new Column(new Table("t"), "col1"));
        final Operator op = Operator.GTE;
        final Object rhs = 18;

        // When
        final ConditionNode node = new ConditionNode(previous, logicOp, lhsCol, lhsExpr, op, rhs);

        // Then
        assertSame(previous, node.previous());
        assertEquals(logicOp, node.logicOperator());
        assertEquals(lhsCol, node.lhsColumn());
        assertSame(lhsExpr, node.lhsExpression());
        assertEquals(op, node.operator());
        assertEquals(rhs, node.rhs());
        assertNull(node.rhsColumn());
    }

    @Test
    void fullConstructorAndGetters() {
        // Given
        final QueryNode previous = new DeleteNode(null, "p1", null);
        final LogicOperator logicOp = LogicOperator.OR;
        final String lhsCol = "status";
        final ExpressionSpec lhsExpr = new SelectColumnSpec(new Column(new Table("t"), "col1"));
        final Operator op = Operator.EQ;
        final Object rhs = "ACTIVE";
        final String rhsCol = "other_status";

        // When
        final ConditionNode node = new ConditionNode(previous, logicOp, lhsCol, lhsExpr, op, rhs, rhsCol);

        // Then
        assertSame(previous, node.previous());
        assertEquals(logicOp, node.logicOperator());
        assertEquals(lhsCol, node.lhsColumn());
        assertSame(lhsExpr, node.lhsExpression());
        assertEquals(op, node.operator());
        assertEquals(rhs, node.rhs());
        assertEquals(rhsCol, node.rhsColumn());
    }

    @Test
    void equals_hashCode() {
        // Given
        final QueryNode prev1 = new DeleteNode(null, "p1", null);
        final QueryNode prev2 = new DeleteNode(null, "p2", null);
        final ExpressionSpec expr1 = new SelectColumnSpec(new Column(new Table("t"), "col1"));
        final ExpressionSpec expr2 = new SelectColumnSpec(new Column(new Table("t"), "col2"));

        final ConditionNode base = new ConditionNode(prev1, LogicOperator.AND, "col1", expr1, Operator.EQ, "val1", "rhs1");
        final ConditionNode same = new ConditionNode(prev1, LogicOperator.AND, "col1", expr1, Operator.EQ, "val1", "rhs1");

        final ConditionNode diffPrev = new ConditionNode(prev2, LogicOperator.AND, "col1", expr1, Operator.EQ, "val1", "rhs1");
        final ConditionNode diffLogic = new ConditionNode(prev1, LogicOperator.OR, "col1", expr1, Operator.EQ, "val1", "rhs1");
        final ConditionNode diffLhsCol = new ConditionNode(prev1, LogicOperator.AND, "col2", expr1, Operator.EQ, "val1", "rhs1");
        final ConditionNode diffLhsExpr = new ConditionNode(prev1, LogicOperator.AND, "col1", expr2, Operator.EQ, "val1", "rhs1");
        final ConditionNode diffOp = new ConditionNode(prev1, LogicOperator.AND, "col1", expr1, Operator.NEQ, "val1", "rhs1");
        final ConditionNode diffRhsCol = new ConditionNode(prev1, LogicOperator.AND, "col1", expr1, Operator.EQ, "val1", "rhs2");

        // valueStructuralKey: "val1" and "val2" are both scalar default -> both yield 1
        final ConditionNode sameScalarRhs = new ConditionNode(prev1, LogicOperator.AND, "col1", expr1, Operator.EQ, "val2", "rhs1");

        // collections with different sizes yield different structural keys
        final ConditionNode colSize1 = new ConditionNode(prev1, LogicOperator.AND, "col1", expr1, Operator.IN, List.of(1), "rhs1");
        final ConditionNode colSize2 = new ConditionNode(prev1, LogicOperator.AND, "col1", expr1, Operator.IN, List.of(1, 2), "rhs1");

        // When / Then
        assertEquals(base, base);
        assertEquals(base, same);
        assertEquals(base.hashCode(), same.hashCode());

        // Same structural key for two scalar string values
        assertEquals(base, sameScalarRhs);
        assertEquals(base.hashCode(), sameScalarRhs.hashCode());

        assertNotEquals(base, diffPrev);
        assertNotEquals(base, diffLogic);
        assertNotEquals(base, diffLhsCol);
        assertNotEquals(base, diffLhsExpr);
        assertNotEquals(base, diffOp);
        assertNotEquals(base, diffRhsCol);

        assertNotEquals(colSize1, colSize2);
        assertNotEquals(colSize1.hashCode(), colSize2.hashCode());

        assertNotEquals(base, null);
        assertNotEquals(base, "other");
    }
}
